package st235.com.github.pomodoro

internal class PomodoroStateMachine(
    private val activeTimeInMinutes: Long,
    private val restTimeInMinutes: Long,
    private val breakTimeInMinutes: Long
) {

    private companion object {
        const val ACTIVE_STATES_BEFORE_BREAK = 3
    }

    sealed interface State {
        /**
         * Initial state before any state is ready.
         */
        object Idling: State
        class Active(val timeInMinutes: Long): State
        class Rest(val timeInMinutes: Long): State

        /**
         * Break should be several times longer than rest.
         */
        class Break(val timeInMinutes: Long): State
    }

    private var state: State = State.Idling
    private var activeStatesCounter: Int = 0

    fun next(): State {
        when (state) {
            is State.Idling, is State.Break, is State.Rest -> {
                activeStatesCounter += 1
                state = State.Active(timeInMinutes = activeTimeInMinutes)
            }
            is State.Active -> {
                if (activeStatesCounter == ACTIVE_STATES_BEFORE_BREAK) {
                    activeStatesCounter = 0
                    state = State.Break(timeInMinutes = breakTimeInMinutes)
                } else {
                    state = State.Rest(timeInMinutes = restTimeInMinutes)
                }
            }
        }

        return state
    }

}