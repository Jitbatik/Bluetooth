package override.ui

import androidx.annotation.StringRes
import com.example.bluetooth.R

enum class ButtonType(@StringRes val labelRes: Int) {
    PLUS(R.string.button_help_box_button_label_plus),
    MINUS(R.string.button_help_box_button_label_minus),
    CANCEL(R.string.button_help_box_button_label_cancel),
    ENTER(R.string.button_help_box_button_label_enter),
}