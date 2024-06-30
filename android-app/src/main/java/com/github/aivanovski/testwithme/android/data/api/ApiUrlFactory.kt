package com.github.aivanovski.testwithme.android.data.api

import com.github.aivanovski.testwithme.web.api.Endpoints.FLOR_RUN
import com.github.aivanovski.testwithme.web.api.Endpoints.FLOW
import com.github.aivanovski.testwithme.web.api.Endpoints.LOGIN
import com.github.aivanovski.testwithme.web.api.Endpoints.PROJECT
import com.github.aivanovski.testwithme.web.api.Endpoints.USER

// TODO: move to web-api module
class ApiUrlFactory {

    fun flow(flowUid: String): String = "$SERVER_URL/$FLOW/$flowUid"

    fun flows(): String = "$SERVER_URL/$FLOW"

    fun projects(): String = "$SERVER_URL/$PROJECT"

    fun flowRuns(): String = "$SERVER_URL/$FLOR_RUN"

    fun users(): String = "$SERVER_URL/$USER"

    fun login(): String = "$SERVER_URL/$LOGIN"

    companion object {
        private const val SERVER_URL = "http://10.0.2.2:8080"
    }
}