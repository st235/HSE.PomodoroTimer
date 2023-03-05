package st235.com.github.pomodoro

import java.util.concurrent.TimeUnit

class PomodoroTimer(
    activeTimeInMinutes: Long,
    restTimeInMinutes: Long,
    breakTimeInMinutes: Long
) {

    private val pomodoroStateMachine = PomodoroStateMachine(
        activeTimeInMinutes,
        restTimeInMinutes,
        breakTimeInMinutes
    )

    enum class PomodoroPhase {
        ACTIVE,
        BREAK
    }

    enum class State {
        AWAITING_START,
        RUNNING
    }

    var state: State = State.AWAITING_START
    private set(newValue) {
        field = newValue
        onStateChangedListener?.invoke(field)
    }

    val phase: PomodoroPhase
    get() {
        val lastKnownState = lastKnownStateMachineState
        if (lastKnownState == null) {
            throw IllegalStateException("last known state machine state was null")
        }
        return lastKnownState.asPomodoroPhase()
    }

    val timeForCurrentPhase: Long
        get() {
            val lastKnownState = lastKnownStateMachineState
            if (lastKnownState == null) {
                throw IllegalStateException("last known state machine state was null")
            }
            return TimeUnit.MINUTES.toMillis(lastKnownState.extractTime())
        }

    private var restartableTimer: RestartableTimer? = null
    private var lastKnownStateMachineState: PomodoroStateMachine.State? = null

    var onStateChangedListener: ((state: State) -> Unit)? = null
    var onPhaseChangedListener: ((phase: PomodoroPhase, timeInMillis: Long) -> Unit)? = null
    var onTickListener: ((progress: Float, timeLeftInMillis: Long) -> Unit)? = null

    init {
        onNewStateMachineState(pomodoroStateMachine.next())
    }

    fun start() {
        restartableTimer?.start()
        state = State.RUNNING
    }

    fun stop() {
        cancelTimer()
        onNewStateMachineState(pomodoroStateMachine.next())
    }

    private fun cancelTimer() {
        restartableTimer?.stop()
        restartableTimer = null
    }

    private fun onNewStateMachineState(stateMachineState: PomodoroStateMachine.State) {
        lastKnownStateMachineState = stateMachineState

        this.state = State.AWAITING_START
        val timeInMinutes = stateMachineState.extractTime()

        cancelTimer()
        restartableTimer = RestartableTimer(timeInMinutes)

        restartableTimer?.onTimeChangedListener = {
            val timer = restartableTimer
            if (timer != null) {
                onTickListener?.invoke(timer.progress, timer.currentTimeInMillis)
            }
        }

        restartableTimer?.onStateChangedListener = { timerState ->
            if (timerState == RestartableTimer.State.FINISHED) {
                stop()
            }
        }

        onPhaseChangedListener?.invoke(stateMachineState.asPomodoroPhase(), TimeUnit.MINUTES.toMillis(timeInMinutes))
    }

    private fun PomodoroStateMachine.State.asPomodoroPhase(): PomodoroPhase {
        return when (this) {
            is PomodoroStateMachine.State.Idling ->
                throw IllegalStateException("Idling is not a valid timer state")
            is PomodoroStateMachine.State.Active ->
                PomodoroPhase.ACTIVE
            is PomodoroStateMachine.State.Rest,
            is PomodoroStateMachine.State.Break ->
                PomodoroPhase.BREAK
        }
    }

    private fun PomodoroStateMachine.State.extractTime(): Long {
        return when (this) {
            is PomodoroStateMachine.State.Idling ->
                throw IllegalStateException("Idling does not have any associated timer")
            is PomodoroStateMachine.State.Active ->
                this.timeInMinutes
            is PomodoroStateMachine.State.Rest ->
                this.timeInMinutes
            is PomodoroStateMachine.State.Break ->
                this.timeInMinutes
        }
    }

}