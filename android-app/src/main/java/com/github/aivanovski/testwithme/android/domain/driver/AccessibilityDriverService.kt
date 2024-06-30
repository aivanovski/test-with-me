package com.github.aivanovski.testwithme.android.domain.driver

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.accessibility.AccessibilityEvent
import com.github.aivanovski.testwithme.android.NotificationService
import com.github.aivanovski.testwithme.android.data.Settings
import com.github.aivanovski.testwithme.android.di.GlobalInjector.inject
import com.github.aivanovski.testwithme.android.domain.driver.model.DriverState
import com.github.aivanovski.testwithme.android.domain.flow.FlowRunnerInteractor
import com.github.aivanovski.testwithme.android.domain.flow.AccessibilityDriverImpl
import com.github.aivanovski.testwithme.android.domain.flow.FlowRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

class AccessibilityDriverService : AccessibilityService() {

    private val settings: Settings by inject()
    private val interactor: FlowRunnerInteractor by inject()

    private val driver = AccessibilityDriverImpl(this, this)
    private val runner = FlowRunner(this, settings, interactor, driver)
    private var serviceConnection: ServiceConnection? = null
    private var timerJob: Job? = null
    private val scopeJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + scopeJob)

    // TODO: refactor

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand:")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate:")

        state.set(DriverState.RUNNING)

        val serviceConnection = object : ServiceConnection {

            private var service: NotificationService.LocalBinder? = null

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Timber.d("onServiceConnected: ")
                this.service = (service as NotificationService.LocalBinder)
                    .apply {
                        connect()
                    }

                startFlowIfNeed()
                startCheckTimer()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Timber.d("onServiceDisconnected: ")

                service?.connect()
                stopFlowIfNeed()
                cancelCheckTimer()
                serviceConnection = null
            }
        }

        val intent = NotificationService.newConnectIntent(this)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        this.serviceConnection = serviceConnection
    }

    private fun startFlowIfNeed() {
        val startId = settings.startJobUid
        val isNotificationServiceConnected = (serviceConnection != null)

        Timber.d(
            "startTestIfNeed: lastStartId=%s, isConnected=%s, isRunning=%s",
            startId,
            isNotificationServiceConnected,
            runner.isRunning(),
        )

        if (!isNotificationServiceConnected) {
            return
        }

        runner.runNextFlow()
    }

    private fun stopFlowIfNeed() {
        runner.stop()
    }

    private fun startCheckTimer() {
        timerJob = scope.launch {
            while (true) {
                delay(10_000)

                if (settings.startJobUid != null) {
                    Timber.d("Check from timer")
                    startFlowIfNeed()
                }
            }
        }
    }

    private fun cancelCheckTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy:")
        state.set(DriverState.STOPPED)
        runner.stop()
        scopeJob.cancel()

        serviceConnection?.let { unbindService(it) }
    }

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    companion object {

        private val state = AtomicReference<DriverState>(DriverState.STOPPED)

        fun getState(): DriverState = state.get()
    }
}