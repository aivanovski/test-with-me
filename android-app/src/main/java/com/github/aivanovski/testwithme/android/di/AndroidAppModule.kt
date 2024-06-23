package com.github.aivanovski.testwithme.android.di

import com.github.aivanovski.testwithme.android.data.repository.FlowRepository
import com.github.aivanovski.testwithme.android.data.repository.FlowRepositoryImpl
import com.github.aivanovski.testwithme.android.data.Settings
import com.github.aivanovski.testwithme.android.data.SettingsImpl
import com.github.aivanovski.testwithme.android.data.api.ApiClient
import com.github.aivanovski.testwithme.android.data.api.HttpRequestExecutor
import com.github.aivanovski.testwithme.android.data.db.AppDatabase
import com.github.aivanovski.testwithme.android.data.db.dao.ExecutionDataDao
import com.github.aivanovski.testwithme.android.data.db.dao.FlowEntryDao
import com.github.aivanovski.testwithme.android.data.db.dao.JobDao
import com.github.aivanovski.testwithme.android.data.db.dao.StepEntryDao
import com.github.aivanovski.testwithme.android.data.repository.ExecutionDataRepository
import com.github.aivanovski.testwithme.android.data.repository.JobRepository
import com.github.aivanovski.testwithme.android.domain.ErrorInteractor
import com.github.aivanovski.testwithme.android.domain.TestInteractor
import com.github.aivanovski.testwithme.android.domain.resources.ResourceProvider
import com.github.aivanovski.testwithme.android.domain.resources.ResourceProviderImpl
import com.github.aivanovski.testwithme.android.domain.usecases.GetCurrentJobUseCase
import com.github.aivanovski.testwithme.android.domain.usecases.ParseFlowFileUseCase
import com.github.aivanovski.testwithme.android.presentation.core.navigation.Router
import com.github.aivanovski.testwithme.android.presentation.screens.flow.FlowInteractor
import com.github.aivanovski.testwithme.android.presentation.screens.flow.FlowViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.FlowListInteractor
import com.github.aivanovski.testwithme.android.presentation.screens.flowList.FlowListViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.login.LoginInteractor
import com.github.aivanovski.testwithme.android.presentation.screens.login.LoginViewModel
import com.github.aivanovski.testwithme.android.presentation.screens.root.RootViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import org.koin.dsl.module
import timber.log.Timber

object AndroidAppModule {

    val module = module {
        single<Settings> { SettingsImpl(get()) }
        single<ResourceProvider> { ResourceProviderImpl(get()) }

        // Database
        single { AppDatabase.buildDatabase(get()) }
        single { provideStepEntryDao(get()) }
        single { provideFlowEntryDao(get()) }
        single { provideRunnerEntryDao(get()) }
        single { provideExecutionEntryDao(get()) }

        // Network
        single { provideHttpRequestExecutor() }
        single { ApiClient(get(), get()) }

        // Repositories
        single<FlowRepository> { FlowRepositoryImpl(get(), get(), get(), get()) }
        single { JobRepository(get()) }
        single { ExecutionDataRepository(get()) }

        // UseCases
        single { ParseFlowFileUseCase() }
        single { GetCurrentJobUseCase(get()) }

        // Interactors
        single { ErrorInteractor(get()) }
        single { TestInteractor(get(), get(), get(), get(), get(), get()) }
        single { LoginInteractor(get(), get()) }
        single { FlowListInteractor(get()) }
        single { FlowInteractor() }

        // ViewModels
        factory { (router: Router) -> LoginViewModel(get(), get(), router) }
        factory { (router: Router) -> FlowListViewModel(get(), get(), router) }
        factory { (vm: RootViewModel, router: Router) -> FlowViewModel(get(), vm, router) }
    }

    private fun provideStepEntryDao(db: AppDatabase): StepEntryDao = db.stepEntryDao

    private fun provideFlowEntryDao(db: AppDatabase): FlowEntryDao = db.flowEntryDao

    private fun provideRunnerEntryDao(db: AppDatabase): JobDao = db.runnerEntryDao

    private fun provideExecutionEntryDao(db: AppDatabase): ExecutionDataDao =
        db.executionDataDao

    private fun provideHttpRequestExecutor(): HttpRequestExecutor {
        return HttpRequestExecutor(
            client = HttpClient(OkHttp) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Timber.d(message)
                        }
                    }
                    level = LogLevel.BODY
                }
            }
        )
    }
}