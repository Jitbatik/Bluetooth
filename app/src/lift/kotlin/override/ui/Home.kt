package override.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.psis.elimlift.presentation.view.home.ButtonState
import com.psis.elimlift.presentation.view.home.ControlButtons
import com.psis.elimlift.presentation.view.home.HomeEvent
import com.psis.elimlift.presentation.view.home.HomeState

@NonRestartableComposable
@Composable
fun Home(state: HomeState) {

    val buttonStates = remember { mutableStateMapOf<ButtonType, ButtonState>() }
    val isReset = remember { mutableStateOf(false) }
    val handlePressStart: (ButtonType) -> Unit = { button ->
        buttonStates[button] = ButtonState.PRESSED
        val activeButtons = buttonStates.filterValues { it != ButtonState.DEFAULT }
        if (activeButtons.count() > 1) isReset.value = !isReset.value
        state.onEvents(HomeEvent.ButtonClick(buttons = activeButtons.keys.toList()))
    }
    val handlePressEnd: (ButtonType) -> Unit = { button ->
        buttonStates[button] = ButtonState.DEFAULT
        state.onEvents(HomeEvent.Press(0, 0))
    }
    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MultiLineTextBox(
                dataUIList = { state.data },
                lines = 4,
                modifier = Modifier
                    .background(Color(0xFF61D7A4))
                    .padding(4.dp)
                    .fillMaxWidth(),
            )
            ControlButtons(
                handlePressStart = handlePressStart,
                handlePressEnd = handlePressEnd,
                buttons = remember { mutableStateOf(ButtonLists.basic) },
                buttonStates = buttonStates,
                modifier = Modifier
                    .wrapContentSize()
                    .fillMaxWidth()
            )

            // Todo Что нужно вообще сделать?

            // Todo Парсер XMl - в нем храниться инфа для UI и адрессация по пакету, мин мах и
            //  def (что это?)

            // Todo Сверстать UI - уже основываясь на данных из XMl и функциональных кнопках

            // Todo Подготовить DataExchangeViewModel - HomeState и onEvent

            // Todo И уже после можно готовить часть связанную с Запросом - Парсингом Выводом

            // Todo Конечный этап это Запись новых значений в Контроллер

            // Todo По итогу можно будет проверять работу
            CentralControllerSetting(
                test = 0,
                modifier = Modifier.fillMaxSize().padding(8.dp)
            )
        }
    }
}

@Composable
fun CentralControllerSetting(
    test: Int,
    modifier: Modifier = Modifier
) {
    when (test) {
        1 -> ControllerListBox(modifier)
        2 -> EmptyStateBox(modifier)
        else -> NoConnectedBox(modifier)
    }
}

@Composable
fun ControllerListBox(modifier: Modifier = Modifier) {
    // Todo Что тут должно быть
    // Todo Список всех настроек с возможностью редактировать
    // Todo Кнопки Обновить и Отправить

    // Todo Возможна реализация где при прохождении N времени переход в EmptyStateBox
}

@Composable
fun EmptyStateBox(modifier: Modifier = Modifier) {
    // Todo Предупреждение о том что настройки еще не вычитанны
    //  уже список из XML, но он не редактируемый
    // Todo Переход в ControllerListBox при нажаитии кнопки Считать
}

@Composable
fun NoConnectedBox(modifier: Modifier = Modifier) {
    // Todo Предупреждение Нужно подключение к контроллеру для определения версии
}


object ButtonLists {
    val basic = listOf(
        ButtonType.PLUS,
        ButtonType.MINUS,
        ButtonType.CANCEL,
        ButtonType.ENTER,
    )
}