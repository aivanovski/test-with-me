package com.github.aivanovski.testwithme.android.data.api

import com.github.aivanovski.testwithme.web.api.Endpoints.FLOR_RUN
import com.github.aivanovski.testwithme.web.api.Endpoints.FLOW
import com.github.aivanovski.testwithme.web.api.Endpoints.LOGIN
import com.github.aivanovski.testwithme.web.api.Endpoints.PROJECT

// TODO: move to web-api module
class ApiUrlFactory {

    fun getFlow(flowUid: String): String = "$SERVER_URL/$FLOW/$flowUid"

    fun getFlows(): String = "$SERVER_URL/$FLOW"

    fun getProjects(): String = "$SERVER_URL/$PROJECT"

    fun getFlowRuns(): String = "$SERVER_URL/$FLOR_RUN"

    fun login(): String = "$SERVER_URL/$LOGIN"

    companion object {
        private const val SERVER_URL = "http://10.0.2.2:8080"
    }
}