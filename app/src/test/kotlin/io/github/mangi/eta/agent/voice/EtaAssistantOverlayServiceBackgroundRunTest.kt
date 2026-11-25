package io.github.mangi.eta.agent.voice

import android.app.Application
import io.github.mangi.eta.data.datastore.SettingsDataStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

/**
 * 语音入口浮窗只是任务的前台窗口：关闭浮窗或服务销毁都不应该终止正在执行的任务。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = Application::class)
class EtaAssistantOverlayServiceBackgroundRunTest {

    @Test
    fun closingTheSurfaceKeepsTheRunningTaskInTheRuntime() {
        val controller = createService()
        try {
            val service = controller.get()
            writeRunId(service, "voice-run")
            callPrivate(service, "dismissAndStop")
            assertEquals("voice-run", readRunId(service))
            assertEquals(
                AssistantRunBackgroundPolicy.RunOwnership.HANDED_TO_RUNTIME,
                readRunOwnership(service),
            )
            assertFalse(AssistantRunBackgroundPolicy.canCancelRuntimeRun(readRunOwnership(service)))
        } finally {
            controller.destroy()
        }
    }

    @Test
    fun startingANewEntryReleasesThePreviousTaskWithoutCancellingIt() {
        val controller = createService()
        try {
            val service = controller.get()
            writeRunId(service, "voice-run")
            callPrivate(service, "releaseRunToRuntime")
            assertNull(readRunId(service))
            assertEquals(
                AssistantRunBackgroundPolicy.RunOwnership.RETAINED,
                readRunOwnership(service),
            )
        } finally {
            controller.destroy()
        }
    }

    @Test
    fun destroyingTheServiceDoesNotCancelTheRunningTask() {
        val controller = createService()
        val service = controller.get()
        writeRunId(service, "voice-run")
        controller.destroy()
        assertEquals(
            AssistantRunBackgroundPolicy.RunOwnership.HANDED_TO_RUNTIME,
            readRunOwnership(service),
        )
    }

    private fun createService(): ServiceController<EtaAssistantOverlayService> {
        SettingsDataStore.init(RuntimeEnvironment.getApplication())
        return Robolectric.buildService(EtaAssistantOverlayService::class.java).create()
    }

    private fun writeRunId(service: EtaAssistantOverlayService, runId: String) {
        EtaAssistantOverlayService::class.java.getDeclaredField("activeRunId").apply { isAccessible = true }
            .set(service, runId)
    }

    private fun readRunId(service: EtaAssistantOverlayService): String? =
        EtaAssistantOverlayService::class.java.getDeclaredField("activeRunId").apply { isAccessible = true }
            .get(service) as String?

    private fun readRunOwnership(
        service: EtaAssistantOverlayService,
    ): AssistantRunBackgroundPolicy.RunOwnership =
        EtaAssistantOverlayService::class.java.getDeclaredField("runOwnership").apply { isAccessible = true }
            .get(service) as AssistantRunBackgroundPolicy.RunOwnership

    private fun callPrivate(service: EtaAssistantOverlayService, name: String) {
        EtaAssistantOverlayService::class.java.getDeclaredMethod(name).apply { isAccessible = true }
            .invoke(service)
    }
}
