import androidx.annotation.StringRes
import com.example.bluetooth.R

enum class ButtonType(@StringRes val labelRes: Int) {
    MENU(labelRes = R.string.button_help_box_button_label_menu),
    MODE(labelRes = R.string.button_help_box_button_label_mode),
    ENTER(labelRes = R.string.button_help_box_button_label_enter),
    CANCEL(labelRes = R.string.button_help_box_button_label_cancel),
    Archive(labelRes = R.string.button_help_box_button_label_archive),
    ArrowUp(labelRes = R.string.button_help_box_button_label_up_arrow),
    ArrowDown(labelRes = R.string.button_help_box_button_label_down_arrow),
    F(labelRes = R.string.button_help_box_button_label_f),

}