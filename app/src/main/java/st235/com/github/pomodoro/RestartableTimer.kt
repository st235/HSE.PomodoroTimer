package st235.com.github.pomodoro

import android.os.CountDownTimer
import java.util.concurrent.TimeUnit

class RestartableTimer(
    private val timeInMinutes: Long
) {

    private companion object {
        const val TICK_INTERVAL = 1000L
    }

    public enum class State {
        IDLE,
        RUNNING,
        STOPPED,
        FINISHED
    }

    var onTimeChangedListener: (() -> Unit)? = null
    var onStateChangedListener: ((state: State) -> Unit)? = null

    private var timer: CountDownTimer? = null

    var currentTimeInMillis: Long = TimeUnit.MINUTES.toMillis(timeInMinutes)
        private set

    private val overallTimeInMillis = TimeUnit.MINUTES.toMillis(timeInMinutes)

    val progress: Float
        get() {
            return (currentTimeInMillis.toDouble() / overallTimeInMillis).toFloat()
        }

    var state: State = State.IDLE
    private set(newValue) {
        field = newValue
        onStateChangedListener?.invoke(field)
    }

    fun start() {
        if (timer != null) {
            throw IllegalStateException("Timer has been already started.")
        }

        state = State.RUNNING

        timer = object : CountDownTimer(currentTimeInMillis, TICK_INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                currentTimeInMillis = millisUntilFinished
                onTimeChangedListener?.invoke()
            }

            override fun onFinish() {
                currentTimeInMillis = 0L
                onTimeChangedListener?.invoke()
                state = State.FINISHED
            }
        }

        timer?.start()
    }

    fun stop() {
        state = State.STOPPED
        currentTimeInMillis = TimeUnit.MINUTES.toMillis(timeInMinutes)
        timer?.cancel()
        timer = null
    }

}