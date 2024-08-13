package com.example.bluetooth.presentation.view.homecontainer

import androidx.lifecycle.ViewModel
import com.example.domain.repository.ExchangeDataRepository
import javax.inject.Inject

class ExchangeDataViewModel @Inject constructor(
    //private val socket: BluetoothSocket?,
    private val exchangeDataRepository: ExchangeDataRepository,
): ViewModel() {

}