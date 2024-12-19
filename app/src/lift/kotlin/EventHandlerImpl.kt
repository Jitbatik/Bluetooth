import com.example.bluetooth.presentation.view.home.EventHandler
import com.example.bluetooth.presentation.view.home.HomeEvent
import javax.inject.Inject

class EventHandlerImpl @Inject constructor() : EventHandler {
    private val defaultCommand = byteArrayOf(0x01, 0x17, 0x04, 0x00, 0x00, 0x00, 0x00)

    //todo заглушка на реализацию
    override fun handleEvent(event: HomeEvent): ByteArray {
        return defaultCommand
    }
}