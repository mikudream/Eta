package io.github.mangi.eta.agent.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantRunBackgroundPolicyTest {
    @Test
    fun closingTheSurfaceHandsTheRunningTaskToTheRuntime() {
        val ownership = AssistantRunBackgroundPolicy.ownershipOnSurfaceExit(hasActiveRun = true)
        assertEquals(AssistantRunBackgroundPolicy.RunOwnership.HANDED_TO_RUNTIME, ownership)
        assertFalse(AssistantRunBackgroundPolicy.canCancelRuntimeRun(ownership))
    }

    @Test
    fun idleSurfaceKeepsNoTask() {
        val ownership = AssistantRunBackgroundPolicy.ownershipOnSurfaceExit(hasActiveRun = false)
        assertEquals(AssistantRunBackgroundPolicy.RunOwnership.RETAINED, ownership)
        assertTrue(AssistantRunBackgroundPolicy.canCancelRuntimeRun(ownership))
    }
}
