package io.github.mangi.eta.agent.voice

/**
 * 语音入口浮窗与后台任务之间的归属策略。
 *
 * 浮窗只是任务的前台窗口，不是任务的宿主：关闭浮窗、重新唤起入口或服务被销毁
 * 都只影响界面，不应该终止已经在执行的任务；Runtime 侧本来就支持客户端断开后
 * 继续执行（见 AgentRuntimeService.onUnbind），因此任务可以直接留在后台跑完。
 * 只有用户在浮窗里显式点击「停止」才会取消任务。
 */
internal object AssistantRunBackgroundPolicy {
    /** 浮窗退出前台后，正在执行的任务归谁所有。 */
    enum class RunOwnership {
        /** 浮窗持有任务：可以展示进度，也可以在用户点击停止时取消它。 */
        RETAINED,

        /** 任务转交 Runtime：浮窗不再跟踪进度，也不能再取消它。 */
        HANDED_TO_RUNTIME,
    }

    /** 浮窗退出（关闭、重新唤起入口、服务销毁）时任务的归属。 */
    fun ownershipOnSurfaceExit(hasActiveRun: Boolean): RunOwnership =
        if (hasActiveRun) RunOwnership.HANDED_TO_RUNTIME else RunOwnership.RETAINED

    /** 任务转交 Runtime 之后，浮窗不能再向 Runtime 请求取消它。 */
    fun canCancelRuntimeRun(ownership: RunOwnership): Boolean =
        ownership == RunOwnership.RETAINED
}
