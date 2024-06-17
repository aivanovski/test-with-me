package com.github.aivanovski.testwithme.web.di

import com.github.aivanovski.testwithme.web.data.repository.FakeFlowRepository
import com.github.aivanovski.testwithme.web.data.repository.FakeUserRepository
import com.github.aivanovski.testwithme.web.data.repository.FlowRepository
import com.github.aivanovski.testwithme.web.data.repository.UserRepository
import com.github.aivanovski.testwithme.web.presentation.controller.LoginController
import com.github.aivanovski.testwithme.web.domain.service.AuthService
import com.github.aivanovski.testwithme.web.presentation.controller.FlowController
import com.github.aivanovski.testwithme.web.presentation.controller.ProjectController
import com.github.aivanovski.testwithme.data.resources.ResourceProviderImpl
import com.github.aivanovski.testwithme.web.data.repository.FakeProjectRepository
import com.github.aivanovski.testwithme.web.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.data.resources.ResourceProvider
import org.koin.dsl.module

object WebAppModule {

    val module = module {
        // core
        single<ResourceProvider> { ResourceProviderImpl(WebAppModule::class) }

        // Repositories
        single<UserRepository> { com.github.aivanovski.testwithme.web.data.repository.FakeUserRepository() }
        single<FlowRepository> { com.github.aivanovski.testwithme.web.data.repository.FakeFlowRepository() }
        single<ProjectRepository> { com.github.aivanovski.testwithme.web.data.repository.FakeProjectRepository() }

        // Services
        single { AuthService(get()) }

        // Controllers
        single { LoginController(get()) }
        single { FlowController(get(), get()) }
        single { ProjectController(get()) }
    }
}