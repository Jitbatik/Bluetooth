package com.example.transfer.model

enum class AxisOrientation { VERTICAL, HORIZONTAL }
data class TickLabel(val position: Float, val label: String)

data class ParametersGroup(
    val time: DateTime,
    val data: List<Parameter>,
)
//todo в data записхать Test
data class LiftParameters(
    val timeStamp: Long,
    val timeMilliseconds: Int,
    val frameId: Int,
    val data: List<Test>,
)

data class Test(
    val label: ParameterLabel,
    val value: Int
)


data class DateTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
)

data class Parameter(
    val id: Int,
    val stepCount: Int,
    val label: ParameterLabel,
    val points: List<ParameterPoint>,
)



data class ParameterPoint(
    val timeStamp: Int,
    val value: Int,
)
data class ChartParameters(
    val stepCount: Int = 20,
    val scale: Float = 1f,
    val minScale: Float = 1f,
    val maxScale: Float = 2f,
    val offset: Float = 0f,
    val minOffsetX: Float = 0f,
    val maxOffsetX: Float = 0f,
)


sealed interface ParameterLabel {
    val value: String

    companion object {
        fun allValues(): List<ParameterLabel> =
            ParametersLabel.entries + InputKey.entries
//        + OutputKey.entries + CabinState.entries +
//                    CabinMovement.entries + CabinLoading.entries + CabinDoorState.entries +
//                    SignalState.entries
    }
}

enum class ParametersLabel(override val value: String) : ParameterLabel {
    TIME_STAMP_MILLIS("Timestamp (millis)"),
    FRAME_ID("Frame ID"),
    //    InputKey
    ENCODER_READINGS("Показания Энкодера"),
    ENCODER_FREQUENCY("Частота Энкодера"),
    ELEVATOR_SPEED("Скорость лифта"),
//    //OutputKey
//    TEMPERATURE("Температура"),
//    SHAFT_SECURITY("Охрана шахты"),
//    //CabinState
//    MOTOR_TEMP_RESISTANCE("Расчетное сопротивление датчика температуры двигателя Rt"),
//    SHAFT_SECURITY_RESISTANCE("Расчетное сопротивление охраны шахты Rohr"),
//    LAST_ERROR_CODE("Код Аварийного сообщения"),
//    ELEVATOR_OPERATION_MODE("Режим работы лифт"),
//    CABIN_CURRENT_POSITION("Текущее положение кабины"),
//    //CabinMovement
//    //CabinLoading
//    //CabinDoorState
////    ORDER("Приказ"),
////    CALLS_DOWN("Вызовы вниз"),
////    CALLS_UP("Вызовы вверх"),
////    SPEC_CONDITION_FLOOR_STOP("Специальное условие останова на этаже"),
//    SYSTEM_STATE("Состояние системы"),
//    //SignalState
//    LAST_WARNING_CODE("Код последнего предупредительного сообщения"),
//    //reserve
//    ID_CONTROLLER_ID_0("Идентификатор контроллера ID0"),
//    ID_CONTROLLER_ID_1("Идентификатор контроллера ID1"),
//    ID_CONTROLLER_ID_2("Идентификатор контроллера ID2"),
////    ID_CONTROLLER_ID_3("Резерв/Идентификатор контроллера ID3"),
//    PASSWORD_ID("Пароль доступа по идентификатору"),
//    PASSWORD_BT("Пароль Bluetooth"),
//    FRAME_INDEX("Индекс последнего записанного кадра"),
//    //reserve
//    CONTROLLER_STATE("Состояние контроллера"),
//    //reserve
//    ID_HOTSPOT_WIFI("Идентификатор точки доступа WiFi"),
//    PASSWORD_HOTSPOT_WIFI("Пароль точки доступа WiFi"),
//    //reserve
}

enum class InputKey(override val value: String) : ParameterLabel {
    INPUT_ONE("IN D2"),
    INPUT_TWO("IN D1"),
    INPUT_THREE("IN3 Кнопка \"ТО\" - 615(X15.5)"),
    INPUT_FOUR("IN4 МС / БС - 614(Х15.6)"),
    INPUT_FIVE("IN5 Ревизия - 869(Х16.1)"),
    INPUT_SIX("IN6 НР / МП - 611(Х16.2)"),
    INPUT_SEVEN("IN7 Кнопка \"Вверх\" - 501(Х16.3)"),
    INPUT_EIGHT("IN8 Кнопка \"Вниз\" - 500(Х16.4)"),
    INPUT_NINE("IN9 Кнопка тормоза - 620(Х16.5)"),
    INPUT_TEN("IN10 Растормаживание - 621(Х16.6)"),
    INPUT_ELEVEN("IN11 ДТО - 842.2(Х17.1)"),
    INPUT_TWELVE("IN12 ДНЭ - 817(Х17.2)"),
    INPUT_THIRTEEN("IN13 ДВЭ - 818(Х17.3)"),
    INPUT_FOURTEEN("IN14 Состояние контакт.- 604(Х17.4)"),
    INPUT_FIFTEEN("IN15 Пожарн. опсность - 163(Х17.5)"),
    INPUT_SIXTEEN("IN16 Сейсм. Опасность - 806(Х17.6)"),
    INPUT_SEVENTEEN("IN17 ДТО1 - 842.3(Х23.1)"),
    INPUT_EIGHTEEN("IN18 ДДВЭ - 845(Х23.2)"),
    INPUT_NINETEEN("IN19 ДДНЭ - 844(Х23.3)"),
    INPUT_TWENTY("IN20 ДЗВ - 843.1(Х23.4)"),
    INPUT_TWENTY_ONE("IN21 ПЧ Ток - 015.1(Х23.5)"),
    INPUT_TWENTY_TWO("IN22 ПЧ Торм. - 012.1(Х23.6)"),
    INPUT_TWENTY_THREE("IN23 ПЧ Готов - 014.1(Х26.1)"),
    INPUT_TWENTY_FOUR("IN24 163_1 ПО"),
    INPUT_TWENTY_FIVE("IN25 Контроль сети 220-F(X1.1)"),
    INPUT_TWENTY_SIX("IN26 ЦБ 213(Х2.2)"),
    INPUT_TWENTY_SEVEN("IN27 ДШ 212(Х2.3)"),
    INPUT_TWENTY_EIGHT("IN28 Контроль ИБП 110-4(Х2.4)"),
    INPUT_TWENTY_NINE("IN29 ДК 64-4(Х2.5)"),
    INPUT_THIRTY("IN30 110В резерв"),
    INPUT_THIRTY_ONE("IN31 110В резерв"),
    INPUT_THIRTY_TWO("IN32 Контроль разряда батареи"),
}

//enum class OutputKey(override val value: String) : ParameterLabel {
//    OUTPUT_ONE("OUT1 К1_251 Вниз (X4.1)"),
//    OUTPUT_TWO("OUT2 К2_252 Вверх (X4.3)"),
//    OUTPUT_THREE("OUT3 К3_274 Скорость (Х4.4)"),
//    OUTPUT_FOUR("OUT4 К4_275 Скорость (Х5.1)"),
//    OUTPUT_FIVE("OUT5 К5_Управление питанием главн.привода 320 (Х5.4)"),
//    OUTPUT_SIX("OUT6 К6_Эвакуация 319 (Х5.6)"),
//    OUTPUT_SEVEN("OUT7 К7_Авария ОШ (Х6.1)"),
//    OUTPUT_EIGHT("OUT8 К8_Авария (Х6.3)"),
//    OUTPUT_NINE("OUT9 К9(Х6.5)"),
//    OUTPUT_TEN("OUT10 K10_273 Скорость (Х24.2)"),
//    OUTPUT_ELEVEN("OUT11 624 Индикация ТО"),
//    OUTPUT_TWELVE("OUT12 623 Растормаживание"),
//    OUTPUT_THIRTEEN("OUT13 Резерв"),
//    OUTPUT_FOURTEEN("OUT14 Резерв"),
//    OUTPUT_FIFTEEN("OUT15 Резерв"),
//    OUTPUT_SIXTEEN("Признак записи в EEPROM"),
//}
//
//enum class CabinState(override val value: String) : ParameterLabel {
//    CABIN_STATE_INPUT_ONE("(IN1) Механический реверс А (311)"),
//    CABIN_STATE_INPUT_TWO("(IN2) ВКО А (312)"),
//    CABIN_STATE_INPUT_THREE("(IN3) ВКЗ А (313)"),
//    CABIN_STATE_INPUT_FOUR("(IN10) Фотореверс А (314)"),
//    CABIN_STATE_INPUT_FIVE("(IN4) Механический реверс В (321)"),
//    CABIN_STATE_INPUT_SIX("(IN5) ВКО В (322)"),
//    CABIN_STATE_INPUT_SEVEN("(IN6) ВКЗ В (323)"),
//    CABIN_STATE_INPUT_EIGHT("(IN12) Фотореверс В (324)"),
//    CABIN_STATE_INPUT_NINE("(IN14) Выключатель блокировки складного щита (630)"),
//    CABIN_STATE_INPUT_TEN("(IN13) Замок двери кабины В (325)"),
//    CABIN_STATE_INPUT_ELEVEN("(IN15) Вход управления вентилятором (305)"),
//    CABIN_STATE_INPUT_TWELVE("(IN11) Замок двери кабины А (315)"),
//    CABIN_STATE_INPUT_THIRTEEN("(IN7) Загрузка 15 кг (802)"),
//    CABIN_STATE_INPUT_FOURTEEN("(IN16) Загрузка 50%"),
//    CABIN_STATE_INPUT_FIFTEEN("(IN8) Загрузка 90% (805)"),
//    CABIN_STATE_INPUT_SIXTEEN("(IN9) Загрузка 110% (804)"),
//    CABIN_STATE_OUTPUT_ONE("(OUT9) Управление вентилятором (FA)"),
//    CABIN_STATE_OUTPUT_TWO("(OUT1) Освещение кабины (103)"),
//    CABIN_STATE_OUTPUT_THREE("(OUT2) Открыть дверь А (331)"),
//    CABIN_STATE_OUTPUT_FOUR("(OUT3) Закрыть дверь А (332)"),
//    CABIN_STATE_OUTPUT_FIVE("(OUT4) Открыть дверь В (333)"),
//    CABIN_STATE_OUTPUT_SIX("(OUT5) Закрыть дверь В (334)"),
//    CABIN_STATE_OUTPUT_SEVEN("(OUT7) Резерв 1"),
//    CABIN_STATE_OUTPUT_EIGHT("(OUT6)Запрет работы фотореверса (310)"),
//    CABIN_STATE_OUTPUT_("Резерв"),
//}
//
//enum class CabinMovement(override val value: String) : ParameterLabel {
//    CABIN_MOVEMENT_ONE("1 выбрано направление \"Вверх\"[B35X0]"),
//    CABIN_MOVEMENT_TWO("1 выбрано направление \"Вниз\"[B35X1]"),
//    CABIN_MOVEMENT_THREE("1/0 скорость Большая/Малая[B35X2]"),
//    CABIN_MOVEMENT_FOUR("1/0 движение Есть/Нет[B35X3]"),
//    CABIN_MOVEMENT_FIVE("1 есть сигнал ДТО (кабина в зоне ТО)[B35X4]"),
//    CABIN_MOVEMENT_SIX("1 есть сигнал ДНЭ[B35X5]"),
//    CABIN_MOVEMENT_SEVEN("1 есть сигнал ДВЭ[B35X6]"),
//    CABIN_MOVEMENT_EIGHT("1 есть сигнал ДТО2[B35X7]"),
//}
//
//enum class CabinLoading(override val value: String) : ParameterLabel {
//    CABIN_LOADING_ONE("1 есть 15 кг[B36X0]"),
//    CABIN_LOADING_TWO("0[B36X1]"),
//    CABIN_LOADING_THREE("1 кабина загружена на 90%[B36X2] "),
//    CABIN_LOADING_FOUR("1 кабина загружена на 110%[B36X3]"),
//    CABIN_LOADING_FIVE("1 проникновение в шахту-1[B36X4]"),
//    CABIN_LOADING_SIX("1 проникновение в шахту-2[B36X5]"),
//    CABIN_LOADING_SEVEN("1 есть сигнал ДКЭ[B36X6]"),
//    CABIN_LOADING_EIGHT("1 есть сигнал ДЗВ[B36X7]"),
//}
//
//enum class CabinDoorState(override val value: String) : ParameterLabel {
//    ////    CABIN_POSITION_SEVENTEEN("1/0 двери открыты (сборка ВКО, ВКЗ, ДК, ДШ, ЗДК)[B37X0]"),
//////    CABIN_POSITION_EIGHTEEN("1/0 двери закрыты (сборка ВКО, ВКЗ, ДК, ДШ, ЗДК)[B37X1]"),
//////    CABIN_POSITION_NINETEEN("1 есть сигнал ДШ[B37X2]"),
//////    CABIN_POSITION_TWENTY("1 есть сигнал ДК[B37X3]"),
//////    CABIN_POSITION_TWENTY_ONE("1 есть сигнал ВКОа[B37X4]"),
//////    CABIN_POSITION_TWENTY_TWO("1 есть сигнал ВКЗа[B37X5]"),
//////    CABIN_POSITION_TWENTY_THREE("1 есть сигнал ЗДКа[B37X6]"),
//////    CABIN_POSITION_TWENTY_FOUR("резерв[B37X7]"),
//}
//
//enum class SignalState(override val value: String) : ParameterLabel {
//    SIGNAL_STATE_ONE("1 есть сигнал ВКОв[B59X0] "),
//    SIGNAL_STATE_TWO("1 есть сигнал ВКЗв[B59X1] "),
//    SIGNAL_STATE_THREE("1 есть сигнал ЗДКв[B59X2]"),
//    SIGNAL_STATE_FOUR("1 есть сигнал РЕВв[B59X3]"),
//    SIGNAL_STATE_FIVE("1 есть сигнал ФРв[B59X4]"),
//    SIGNAL_STATE_SIX("1 есть сигнал РЕВа[B59X5]"),
//    SIGNAL_STATE_SEVEN("1 есть сигнал ФРа[B59X6]"),
//    SIGNAL_STATE_EIGHT("1 есть сигнал ЦБ (цепь безопасности исправна)[B59X7]"),
//    SIGNAL_STATE_NINE("1 есть сигнал Торм (износ тормозных колодок или расторможены)[B60X0]"),
//    SIGNAL_STATE_TEN("1 есть сигнал 604 (контакторы разомкнуты)[B60X1]"),
//    SIGNAL_STATE_ELEVEN("1 есть сигнал закрыть двери A[B60X2]"),
//    SIGNAL_STATE_TWELVE("1 есть сигнал открыть двери A[B60X3]"),
//    SIGNAL_STATE_THIRTEEN("1 есть сигнал закрыть двери B[B60X4]"),
//    SIGNAL_STATE_FOURTEEN("1 есть сигнал открыть двери B[B60X5]"),
//}
//            in OutputKey.entries -> {
//                val index = InputKey.entries.indexOf(label)
//                byteDataList.subList(20, 22).toIntListFromByteData()[index]
//            }
//
//            ParametersLabel.TEMPERATURE -> byteDataList.subList(22, 23).toIntFromByteData()
//            ParametersLabel.SHAFT_SECURITY -> byteDataList.subList(23, 24).toIntFromByteData()
//            ParametersLabel.NOT_VALUE -> 0
//
//            in CabinState.entries -> {
//                val index = InputKey.entries.indexOf(label)
//                byteDataList.subList(24, 28).toIntListFromByteData()[index]
//            }
//
//            ParametersLabel.MOTOR_TEMP_RESISTANCE -> byteDataList.subList(28, 30)
//                .toIntFromByteData()
//
//            ParametersLabel.SHAFT_SECURITY_RESISTANCE -> byteDataList.subList(30, 32)
//                .toIntFromByteData()
//
//            ParametersLabel.LAST_ERROR_CODE -> byteDataList.subList(32, 33).toIntFromByteData()
//            ParametersLabel.ELEVATOR_OPERATION_MODE -> byteDataList.subList(33, 34)
//                .toIntFromByteData()
//
//            ParametersLabel.CABIN_CURRENT_POSITION -> byteDataList.subList(34, 35)
//                .toIntFromByteData()
//
//            in CabinMovement.entries -> {
//                val index = InputKey.entries.indexOf(label)
//                byteDataList.subList(35, 36).toIntListFromByteData()[index]
//            }
//
//            in CabinLoading.entries -> {
//                val index = InputKey.entries.indexOf(label)
//                byteDataList.subList(36, 37).toIntListFromByteData()[index]
//            }
//
//            in CabinDoorState.entries -> {
//                val index = InputKey.entries.indexOf(label)
//                byteDataList.subList(37, 38).toIntListFromByteData()[index]
//            }
////            byteDataList.subList(38, 43).toIntListFromByteData(), // Приказы
////            byteDataList.subList(43, 48).toIntListFromByteData(), // Вызовы вниз
////            byteDataList.subList(48, 53).toIntListFromByteData(), // Вызовы вверх
////            byteDataList.subList(53, 58).toIntListFromByteData(), // Спец условия остановки
//
//            ParametersLabel.SYSTEM_STATE -> byteDataList.subList(58, 59).toIntFromByteData()
//            in SignalState.entries -> {
//                val index = InputKey.entries.indexOf(label)
//                byteDataList.subList(59, 61).toIntListFromByteData()[index]
//            }
//
//            ParametersLabel.LAST_WARNING_CODE -> byteDataList.subList(61, 62).toIntFromByteData()
//            ParametersLabel.ID_CONTROLLER_ID_0 -> byteDataList.subList(64, 68).toIntFromByteData()
//            ParametersLabel.ID_CONTROLLER_ID_1 -> byteDataList.subList(68, 72).toIntFromByteData()
//            ParametersLabel.ID_CONTROLLER_ID_2 -> byteDataList.subList(72, 76).toIntFromByteData()
//
//            ParametersLabel.PASSWORD_ID -> byteDataList.subList(80, 84).toIntFromByteData()
//            ParametersLabel.PASSWORD_BT -> byteDataList.subList(84, 88).toIntFromByteData()
//            ParametersLabel.FRAME_INDEX -> byteDataList.subList(88, 90).toIntFromByteData()
//            ParametersLabel.CONTROLLER_STATE -> byteDataList.subList(98, 100).toIntFromByteData()
//            ParametersLabel.ID_HOTSPOT_WIFI -> byteDataList.subList(128, 144).toIntFromByteData()
//            ParametersLabel.PASSWORD_HOTSPOT_WIFI -> byteDataList.subList(144, 160)
//                .toIntFromByteData()
