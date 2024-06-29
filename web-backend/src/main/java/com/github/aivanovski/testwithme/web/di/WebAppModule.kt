package com.github.aivanovski.testwithme.web.di

import com.github.aivanovski.testwithme.web.data.repository.FlowRepositoryImpl
import com.github.aivanovski.testwithme.web.data.repository.UserRepositoryImpl
import com.github.aivanovski.testwithme.web.data.repository.FlowRepository
import com.github.aivanovski.testwithme.web.data.repository.UserRepository
import com.github.aivanovski.testwithme.web.presentation.controller.LoginController
import com.github.aivanovski.testwithme.web.domain.service.AuthService
import com.github.aivanovski.testwithme.web.presentation.controller.FlowController
import com.github.aivanovski.testwithme.web.presentation.controller.ProjectController
import com.github.aivanovski.testwithme.data.resources.ResourceProviderImpl
import com.github.aivanovski.testwithme.web.data.repository.ProjectRepositoryImpl
import com.github.aivanovski.testwithme.web.data.repository.ProjectRepository
import com.github.aivanovski.testwithme.data.resources.ResourceProvider
import com.github.aivanovski.testwithme.web.data.database.FlowRunDao
import com.github.aivanovski.testwithme.web.domain.usecases.InitializeDefaultDataUseCase
import com.github.aivanovski.testwithme.web.data.database.FlowDao
import com.github.aivanovski.testwithme.web.data.database.ProjectDao
import com.github.aivanovski.testwithme.web.data.database.UserDao
import com.github.aivanovski.testwithme.web.data.file.FlowContentProvider
import com.github.aivanovski.testwithme.web.data.repository.FlowRunRepository
import com.github.aivanovski.testwithme.web.presentation.controller.FlowRunController
import com.github.aivanovski.testwithme.web.presentation.controller.UserController
import org.koin.dsl.module

object WebAppModule {

    val module = module {
        // core
        single<ResourceProvider> { ResourceProviderImpl(WebAppModule::class) }
        single { FlowContentProvider() }

        // Database
        single { UserDao() }
        single { ProjectDao() }
        single { FlowDao() }
        single { FlowRunDao() }

        // Repositories
        single<UserRepository> { UserRepositoryImpl(get()) }
        single<FlowRepository> { FlowRepositoryImpl(get(), get()) }
        single<ProjectRepository> { ProjectRepositoryImpl(get()) }
        single { FlowRunRepository(get(), get()) }

        // UseCases
        single { InitializeDefaultDataUseCase(get(), get(), get()) }

        // Services
        single { AuthService(get()) }

        // Controllers
        single { LoginController(get()) }
        single { FlowController(get(), get(), get()) }
        single { ProjectController(get()) }
        single { FlowRunController(get(), get(), get()) }
        single { UserController(get()) }
    }
}