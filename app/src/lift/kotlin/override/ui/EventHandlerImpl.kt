package override.ui

import com.psis.elimlift.EventHandler
import com.psis.elimlift.presentation.view.home.HomeEvent
import javax.inject.Inject


class EventHandlerImpl @Inject constructor() : EventHandler<HomeEvent, ByteArray> {
    private val baseLift = intArrayOf(0x01, 0x10, 0x01, 0x04, 0x00, 0x01, 0x02)
    private val defaultCommand = intArrayOf(0x01, 0x10, 0x01, 0x04, 0x00, 0x01, 0x02, 0x00, 0x00)

    override fun handle(event: HomeEvent): ByteArray = when (event) {
        is HomeEvent.ButtonClick -> handleButtonCommands(event.buttons)
        is HomeEvent.Press -> defaultCommand
    }.toByteArray()


    private fun handleButtonCommands(activeButtons: List<ButtonType>): IntArray {
        return if (activeButtons.isNotEmpty()) {
            activeButtons
                .map(::mapButtonToCommand)
                .reduce { acc, array -> combineWith(acc, array) }
        } else defaultCommand
    }

    private fun mapButtonToCommand(type: ButtonType): IntArray {
        return when (type) {
            ButtonType.ENTER -> baseLift + intArrayOf(0x00, 0x08)
            ButtonType.CANCEL -> baseLift + intArrayOf(0x00, 0x04)
            ButtonType.PLUS -> baseLift + intArrayOf(0x00, 0x01)
            ButtonType.MINUS -> baseLift + intArrayOf(0x00, 0x02)
        }
    }

    private fun combineWith(array1: IntArray, array2: IntArray): IntArray =
        array1.zip(array2) { a, b -> a or b }.toIntArray()

    private fun IntArray.toByteArray(): ByteArray = this.map(Int::toByte).toByteArray()
}