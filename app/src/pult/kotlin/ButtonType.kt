import androidx.annotation.StringRes
import com.example.bluetooth.R

enum class ButtonType(@StringRes val labelRes: Int) {
    CLOSE(labelRes = R.string.button_help_box_button_label_close),
    OPEN(labelRes = R.string.button_help_box_button_label_open),
    STOP(labelRes = R.string.button_help_box_button_label_stop),
    BURNER(labelRes = R.string.button_help_box_button_label_burner),
    F(labelRes = R.string.button_help_box_button_label_f),
    CANCEL(labelRes = R.string.button_help_box_button_label_cancel),
    ENTER(labelRes = R.string.button_help_box_button_label_enter),
    ARROW_UP(labelRes = R.string.button_help_box_button_label_up_arrow),
    ARROW_DOWN(labelRes = R.string.button_help_box_button_label_down_arrow),

    ONE(labelRes = R.string.button_help_box_button_label_one),
    TWO(labelRes = R.string.button_help_box_button_label_two),
    THREE(labelRes = R.string.button_help_box_button_label_three),
    FOUR(labelRes = R.string.button_help_box_button_label_four),
    FIVE(labelRes = R.string.button_help_box_button_label_five),
    SIX(labelRes = R.string.button_help_box_button_label_six),
    SEVEN(labelRes = R.string.button_help_box_button_label_seven),
    EIGHT(labelRes = R.string.button_help_box_button_label_eight),
    NINE(labelRes = R.string.button_help_box_button_label_nine),
    ZERO(labelRes = R.string.button_help_box_button_label_zero),
    MINUS(labelRes = R.string.button_help_box_button_label_minus),
    POINT(labelRes = R.string.button_help_box_button_label_point),
}