package com.example.bluetooth.presentation.view.connect.components.permission

import androidx.annotation.StringRes
import com.example.bluetooth.R

class TextPermissionProvider : TextProvider {
    @StringRes
    override val title = R.string.text_provider_title_bluetooth

    @StringRes
    override val description = R.string.text_provider_description_bluetooth

    @StringRes
    override val buttonText = R.string.text_provider_button_bluetooth
}