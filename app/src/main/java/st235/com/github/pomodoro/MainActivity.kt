package st235.com.github.pomodoro

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.github.st235.circularprogressbar.CircularProgressBar
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private companion object {
        const val TIMER_FORMAT = "%1\$02d:%2\$02d"
    }

    private val pomodoroTimer = PomodoroTimer(
        activeTimeInMinutes = 25L,
        restTimeInMinutes = 5L,
        breakTimeInMinutes = 15L
    )

    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var timerTextView: TextView
    private lateinit var playbackControlButton: AppCompatImageView
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        circularProgressBar = findViewById(R.id.progress_bar)
        timerTextView = findViewById(R.id.timer_text_view)
        playbackControlButton = findViewById(R.id.playback_control_button)
        statusTextView = findViewById(R.id.status_text_view)

        updatePlaybackButtonState(pomodoroTimer.state)

        pomodoroTimer.onStateChangedListener = { state ->
            updatePlaybackButtonState(state)
        }

        pomodoroTimer.onPhaseChangedListener = {  phase, timeInMillis ->
            updateTheme(phase)
            updateTimer(timeInMillis, 1.0f)
        }

        pomodoroTimer.onTickListener = { progress, timeLeftInMillis ->
            updateTimer(timeLeftInMillis, progress)
        }

        playbackControlButton.setOnClickListener {
            when (pomodoroTimer.state) {
                PomodoroTimer.State.AWAITING_START -> pomodoroTimer.start()
                PomodoroTimer.State.RUNNING -> pomodoroTimer.stop()
            }
        }

        updateTheme(pomodoroTimer.phase)
        updateTimer(pomodoroTimer.timeForCurrentPhase, 1.0f)
    }

    override fun onDestroy() {
        super.onDestroy()
        pomodoroTimer.stop()
        pomodoroTimer.onStateChangedListener = null
    }

    private fun updateTheme(phase: PomodoroTimer.PomodoroPhase) {
        when (phase) {
            PomodoroTimer.PomodoroPhase.ACTIVE -> {
                val activeColor = ContextCompat.getColor(this, R.color.red_500)
                circularProgressBar.foregroundProgressColor = activeColor
                timerTextView.setTextColor(activeColor)
                statusTextView.setTextColor(activeColor)
                statusTextView.setText(R.string.main_pomodoro_phase_focus)
                ImageViewCompat.setImageTintList(playbackControlButton, ColorStateList.valueOf(activeColor))
            }
            PomodoroTimer.PomodoroPhase.BREAK -> {
                val breakColor = ContextCompat.getColor(this, R.color.gray_400)
                circularProgressBar.foregroundProgressColor = breakColor
                timerTextView.setTextColor(breakColor)
                statusTextView.setTextColor(breakColor)
                statusTextView.setText(R.string.main_pomodoro_phase_break)
                ImageViewCompat.setImageTintList(playbackControlButton, ColorStateList.valueOf(breakColor))
            }
        }
    }

    private fun updateTimer(timeInMillis: Long, progress: Float) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60

        timerTextView.text = String.format(TIMER_FORMAT, minutes, seconds)
        circularProgressBar.progress = progress
    }

    private fun updatePlaybackButtonState(playbackPhase: PomodoroTimer.State) {
        val icon = when (playbackPhase) {
            PomodoroTimer.State.AWAITING_START -> R.drawable.ic_round_play
            PomodoroTimer.State.RUNNING -> R.drawable.ic_round_next
        }

        playbackControlButton.setImageResource(icon)
    }
}
