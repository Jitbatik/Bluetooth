import com.example.bluetooth.presentation.view.home.EventHandler
import com.example.bluetooth.presentation.view.home.HomeEvent
import javax.inject.Inject

class EventHandlerImpl @Inject constructor() : EventHandler {
    private val baseModbus = intArrayOf(0x01, 0x17, 0x04)
    private val defaultCommand = intArrayOf(0x01, 0x17, 0x04, 0x00, 0x00, 0x00, 0x00)

    override fun handleEvent(event: HomeEvent): ByteArray {
        val command = when (event) {
            is HomeEvent.ButtonClick -> handleButtonCommands(event.buttons)
            is HomeEvent.Press -> handlePressCommands(colum = event.column, row = event.row)
        }
        return command.toByteArray()
    }

    private fun handleButtonCommands(activeButtons: List<ButtonType>): IntArray {
        return if (activeButtons.isNotEmpty()) {
            activeButtons
                .map(::mapButtonToCommand)
                .reduce { acc, array -> combineWith(acc, array) }
        } else defaultCommand
    }

    private fun handlePressCommands(colum: Int, row: Int): IntArray =
        baseModbus + intArrayOf(colum, row, 0x00, 0x00)

    private fun mapButtonToCommand(type: ButtonType): IntArray {
        return when (type) {
            ButtonType.BURNER -> baseModbus + intArrayOf(0x00, 0x00, 0x20, 0x00)
            ButtonType.F -> baseModbus + intArrayOf(0x00, 0x00, 0x40, 0x00)
            ButtonType.CANCEL -> baseModbus + intArrayOf(0x12, 0x1D, 0x00, 0x10)
            ButtonType.ENTER -> baseModbus + intArrayOf(0x16, 0x1D, 0x00, 0x80)
            ButtonType.ARROW_UP -> baseModbus + intArrayOf(0x19, 0x1D, 0x01, 0x00)
            ButtonType.ARROW_DOWN -> baseModbus + intArrayOf(0x1D, 0x1D, 0x08, 0x00)
            ButtonType.ONE -> baseModbus + intArrayOf(0x00, 0x00, 0x00, 0x04)
            ButtonType.TWO -> baseModbus + intArrayOf(0x00, 0x00, 0x80, 0x00)
            ButtonType.THREE -> baseModbus + intArrayOf(0x00, 0x00, 0x10, 0x00)
            ButtonType.FOUR -> baseModbus + intArrayOf(0x00, 0x00, 0x02, 0x00)
            ButtonType.FIVE -> baseModbus + intArrayOf(0x00, 0x00, 0x00, 0x20)
            ButtonType.SIX -> baseModbus + intArrayOf(0x00, 0x00, 0x00, 0x40)
            ButtonType.SEVEN, ButtonType.CLOSE -> baseModbus + intArrayOf(0x00, 0x00, 0x00, 0x08)
            ButtonType.EIGHT, ButtonType.OPEN -> baseModbus + intArrayOf(0x00, 0x00, 0x00, 0x01)
            ButtonType.NINE, ButtonType.STOP -> baseModbus + intArrayOf(0x00, 0x00, 0x04, 0x00)
            ButtonType.ZERO -> baseModbus + intArrayOf(0x00, 0x00, 0x00, 0x02)
            ButtonType.MINUS -> baseModbus + intArrayOf(0x00, 0x00, 0x08, 0x00)
            ButtonType.POINT -> baseModbus + intArrayOf(0x00, 0x00, 0x20, 0x00)
        }
    }

    private fun combineWith(array1: IntArray, array2: IntArray): IntArray =
        array1.zip(array2) { a, b -> a or b }.toIntArray()
    private fun IntArray.toByteArray(): ByteArray = this.map(Int::toByte).toByteArray()
}