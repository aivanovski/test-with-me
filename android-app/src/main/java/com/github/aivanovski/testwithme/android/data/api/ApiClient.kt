package com.github.aivanovski.testwithme.android.data.api

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.data.Settings
import com.github.aivanovski.testwithme.android.entity.FlowRun
import com.github.aivanovski.testwithme.android.entity.FlowSourceType
import com.github.aivanovski.testwithme.android.entity.Project
import com.github.aivanovski.testwithme.android.entity.User
import com.github.aivanovski.testwithme.android.entity.db.FlowEntry
import com.github.aivanovski.testwithme.android.entity.exception.ApiException
import com.github.aivanovski.testwithme.android.entity.exception.InvalidHttpStatusCodeException
import com.github.aivanovski.testwithme.android.utils.DateUtils
import com.github.aivanovski.testwithme.extensions.unwrap
import com.github.aivanovski.testwithme.extensions.unwrapError
import com.github.aivanovski.testwithme.web.api.common.ApiDateFormat
import com.github.aivanovski.testwithme.web.api.request.LoginRequest
import com.github.aivanovski.testwithme.web.api.request.PostFlowRunRequest
import com.github.aivanovski.testwithme.web.api.response.FlowRunsResponse
import com.github.aivanovski.testwithme.web.api.response.FlowResponse
import com.github.aivanovski.testwithme.web.api.response.FlowsResponse
import com.github.aivanovski.testwithme.web.api.response.LoginResponse
import com.github.aivanovski.testwithme.web.api.response.PostFlowRunResponse
import com.github.aivanovski.testwithme.web.api.response.ProjectsResponse
import com.github.aivanovski.testwithme.web.api.response.UsersResponse
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

class ApiClient(
    private val executor: HttpRequestExecutor,
    private val settings: Settings
) {
    private val urlFactory = ApiUrlFactory()

    suspend fun getUsers(): Either<ApiException, List<User>> = either {
        val response = getAndParse<UsersResponse>(urlFactory.users()).bind()

        response.users.map { item ->
            User(
                uid = item.id,
                name = item.name
            )
        }
    }

    suspend fun postFlowRun(
        request: PostFlowRunRequest
    ): Either<ApiException, PostFlowRunResponse> = either {
        val body = Json.encodeToString(request)

        // TODO: should be retried if 401
        val response = executor.post(urlFactory.flowRuns()) {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${settings.authToken}")
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bind()

        if (response.status != HttpStatusCode.OK) {
            raise(InvalidHttpStatusCodeException(response.status))
        }

        return parseJson(response.bodyAsText())
    }

    suspend fun getFlowRuns(): Either<ApiException, List<FlowRun>> = either {
        val response = getAndParse<FlowRunsResponse>(urlFactory.flowRuns()).bind()

        response.stats.mapNotNull { item ->
            // TODO: time could be str+long
            val time = DateUtils.parseOrNull(ApiDateFormat.DATE_TIME_FORMAT, item.finishedAt)
                ?: return@mapNotNull null

            FlowRun(
                flowUid = item.flowUid,
                userUid = item.userUid,
                executionTime = time,
                isSuccess = item.isSuccess
            )
        }
    }

    suspend fun getProjects(): Either<ApiException, List<Project>> = either {
        val response = getAndParse<ProjectsResponse>(urlFactory.projects()).bind()

        response.projects.map { project ->
            Project(
                uid = project.uid,
                name = project.name
            )
        }
    }

    suspend fun getFlows(): Either<ApiException, List<FlowEntry>> = either {
        val response = getAndParse<FlowsResponse>(urlFactory.flows()).bind()

        response.flows.map { flow ->
            FlowEntry(
                id = null,
                uid = flow.uid,
                projectUid = flow.projectUid,
                name = flow.name,
                sourceType = FlowSourceType.REMOTE
            )
        }
    }

    suspend fun getFlow(
        flowUid: String
    ): Either<ApiException, FlowResponse> = either {
        getAndParse<FlowResponse>(urlFactory.flow(flowUid)).bind()
    }

    suspend fun login(
        username: String,
        password: String
    ): Either<ApiException, LoginResponse> = either {
        val body = Json.encodeToString(
            LoginRequest(
                username = username,
                password = password
            )
        )

        val response = executor.post(urlFactory.login()) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bind()

        if (response.status != HttpStatusCode.OK) {
            raise(InvalidHttpStatusCodeException(response.status))
        }

        return parseJson(response.bodyAsText())
    }

    private suspend inline fun <reified T> getAndParse(url: String): Either<ApiException, T> {
        val body = get(url)
        if (body.isLeft()) {
            return Either.Left(body.unwrapError())
        }

        return parseJson<T>(body.unwrap())
    }

    private suspend fun get(url: String): Either<ApiException, String> = either {
        // TODO: check if ktor could automatically retry request

        // Get token if necessary
        loadOrRequestAuthToken().bind()

        val requestBuilder: HttpRequestBuilder.() -> Unit = {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${settings.authToken}")
            }
            contentType(ContentType.Application.Json)
        }

        // Do request
        val response = executor.get(url, block = requestBuilder).bind()
        if (response.status == HttpStatusCode.OK) {
            return@either response.bodyAsText()
        }

        // Authenticate was unsuccessful, retry request
        if (response.status == HttpStatusCode.Unauthorized) {
            settings.authToken = null
            loadOrRequestAuthToken().bind()

            // Do request
            val retryResponse = executor.get(url, block = requestBuilder).bind()
            if (retryResponse.status != HttpStatusCode.OK) {
                raise(InvalidHttpStatusCodeException(response.status))
            }

            retryResponse.bodyAsText()
        } else {
            raise(InvalidHttpStatusCodeException(response.status))
        }
    }

    private suspend fun loadOrRequestAuthToken(): Either<ApiException, String> = either {
        if (settings.authToken == null) {
            val token = login("admin", "abc123").bind().token // TODO: store credentials
            settings.authToken = token
            token
        } else {
            settings.authToken.orEmpty()
        }
    }

    private inline fun <reified T> parseJson(
        body: String
    ): Either<ApiException, T> = either {
        try {
            Json.decodeFromString<T>(body)
        } catch (exception: SerializationException) {
            Timber.d(exception)
            raise(ApiException(cause = exception))
        }
    }
}