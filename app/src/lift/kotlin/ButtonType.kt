import androidx.annotation.StringRes
import com.example.bluetooth.R

enum class ButtonType(@StringRes val labelRes: Int) {
    ARROW_DOWN(R.string.button_help_box_button_label_down_arrow),
    ARROW_UP(R.string.button_help_box_button_label_up_arrow),
    CANCEL(R.string.button_help_box_button_label_cancel),
    ENTER(R.string.button_help_box_button_label_enter),
}