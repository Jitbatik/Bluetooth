import com.example.bluetooth.presentation.view.home.EventHandler
import com.example.bluetooth.presentation.view.home.HomeEvent
import javax.inject.Inject

class EventHandlerImpl @Inject constructor() : EventHandler<HomeEvent, ByteArray> {
    private val baseUART = intArrayOf(0xFE, 0x08, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00)

    private val defaultCommand = byteArrayOf(0x01, 0x17, 0x04, 0x00, 0x00, 0x00, 0x00)

    //todo заглушка на реализацию
    override fun handle(event: HomeEvent): ByteArray {
        return defaultCommand
    }

    fun handleButton1(type: ButtonType): IntArray {
        return when (type) {
            ButtonType.MENU -> baseUART + intArrayOf(0, 4)
            ButtonType.MODE -> baseUART + intArrayOf(0, 20)
            ButtonType.ENTER -> baseUART + intArrayOf(80, 0)
            ButtonType.CANCEL -> baseUART + intArrayOf(10, 0)
            ButtonType.Archive -> baseUART + intArrayOf(2, 0)
            ButtonType.ArrowUp -> baseUART + intArrayOf(0, 0)
            ButtonType.ArrowDown -> baseUART + intArrayOf(0, 0)
            ButtonType.F -> baseUART + intArrayOf(0, 40)
        }
    }
}