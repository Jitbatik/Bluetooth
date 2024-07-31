package com.example.bluetooth.presentation.view.connectcontainer

class TextPermissionProvider : TextProvider {
    override val title: String
        get() = "Пожалуйста, предоставьте разрешения"
    override val description: String
        get() = "Приложению для сканирования и подключения требуются разрешения на эти действия "
    override val buttonText: String
        get() = "Перейти в настройки"
}