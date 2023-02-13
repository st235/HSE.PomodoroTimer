package st235.com.github.pomodoro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private companion object {
        const val TIMER_FORMAT = "%1\$02d:%2\$02d"
    }

    private val pomodoroTimer = PomodoroTimer(timeInMinutes = 1)

    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var timerTextView: TextView
    private lateinit var playbackControlButton: AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        circularProgressBar = findViewById(R.id.progress_bar)
        timerTextView = findViewById(R.id.timer_text_view)
        playbackControlButton = findViewById(R.id.playback_control_button)

        updatePlaybackButtonState(pomodoroTimer.state)
        updateTimer(pomodoroTimer.currentTimeInMillis, pomodoroTimer.progress)

        pomodoroTimer.onStateChangedListener = { state ->
            updatePlaybackButtonState(state)
        }

        pomodoroTimer.onTimeChangedListener = {
            updateTimer(pomodoroTimer.currentTimeInMillis, pomodoroTimer.progress)
        }

        playbackControlButton.setOnClickListener {
            when (pomodoroTimer.state) {
                PomodoroTimer.State.IDLE,
                PomodoroTimer.State.STOPPED,
                PomodoroTimer.State.FINISHED -> pomodoroTimer.start()
                PomodoroTimer.State.RUNNING -> pomodoroTimer.stop()
            }
        }
     }

    private fun updateTimer(timeInMillis: Long, progress: Float) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60

        timerTextView.text = String.format(TIMER_FORMAT, minutes, seconds)
        circularProgressBar.progress = progress
    }

    private fun updatePlaybackButtonState(playbackState: PomodoroTimer.State) {
        val icon = when(playbackState) {
            PomodoroTimer.State.IDLE -> R.drawable.ic_round_play
            PomodoroTimer.State.RUNNING -> R.drawable.ic_round_stop
            PomodoroTimer.State.STOPPED,
            PomodoroTimer.State.FINISHED -> R.drawable.ic_round_restart
        }

        playbackControlButton.setImageResource(icon)
    }
}
