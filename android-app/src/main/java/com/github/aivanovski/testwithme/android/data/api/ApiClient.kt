package com.github.aivanovski.testwithme.android.data.api

import arrow.core.Either
import arrow.core.raise.either
import com.github.aivanovski.testwithme.android.data.Settings
import com.github.aivanovski.testwithme.android.entity.FlowRun
import com.github.aivanovski.testwithme.android.entity.Project
import com.github.aivanovski.testwithme.android.entity.exception.ApiException
import com.github.aivanovski.testwithme.android.entity.exception.InvalidHttpStatusCodeException
import com.github.aivanovski.testwithme.android.utils.DateUtils
import com.github.aivanovski.testwithme.web.api.common.ApiDateFormat
import com.github.aivanovski.testwithme.web.api.request.LoginRequest
import com.github.aivanovski.testwithme.web.api.response.FlowRunsResponse
import com.github.aivanovski.testwithme.web.api.response.FlowResponse
import com.github.aivanovski.testwithme.web.api.response.FlowsResponse
import com.github.aivanovski.testwithme.web.api.response.LoginResponse
import com.github.aivanovski.testwithme.web.api.response.ProjectsResponse
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

    suspend fun getFlowRuns(): Either<ApiException, List<FlowRun>> = either {
        val body = get(urlFactory.getFlowRuns()).bind()
        val response = parseJson<FlowRunsResponse>(body).bind()

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
        val body = get(urlFactory.getProjects()).bind()
        val response = parseJson<ProjectsResponse>(body).bind()

        response.projects.map { project ->
            Project(
                uid = project.uid,
                name = project.name
            )
        }
    }

    suspend fun getFlows(): Either<ApiException, FlowsResponse> = either {
        val body = get(urlFactory.getFlows()).bind()
        parseJson<FlowsResponse>(body).bind()
    }

    suspend fun getFlow(
        flowUid: String
    ): Either<ApiException, FlowResponse> = either {
        val body = get(urlFactory.getFlow(flowUid)).bind()
        parseJson<FlowResponse>(body).bind()
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

    private suspend fun get(url: String): Either<ApiException, String> = either {
        // Get token if necessary
        val token = if (settings.authToken == null) {
            val response = login("admin", "abc123").bind() // TODO: fix

            response.token
        } else {
            settings.authToken.orEmpty()
        }

        settings.authToken = token

        val builder: HttpRequestBuilder.() -> Unit = {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            contentType(ContentType.Application.Json)
        }

        // Do request
        val response = executor.get(url, block = builder).bind()

        if (response.status == HttpStatusCode.OK) {
            return@either response.bodyAsText()
        }

        // Authenticate was unsuccessful, retry request
        if (response.status == HttpStatusCode.Unauthorized) {
            val loginResponse = login("admin", "abc123").bind() // TODO: fix

            settings.authToken = loginResponse.token

            // Do request
            val retryResponse = executor.get(url, block = builder).bind()

            if (retryResponse.status != HttpStatusCode.OK) {
                raise(InvalidHttpStatusCodeException(response.status))
            }

            retryResponse.bodyAsText()
        } else {
            raise(InvalidHttpStatusCodeException(response.status))
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