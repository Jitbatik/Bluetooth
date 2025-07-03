package com.example.bluetooth.presentation.view.settings

import com.example.bluetooth.EventHandler
import com.example.bluetooth.presentation.view.settings.model.ChartXmlPickerEvent
import com.example.transfer.filePick.ChartFilesInteractor
import javax.inject.Inject

class ChartXmlEventHandler @Inject constructor(
    private val chartFilesInteractor: ChartFilesInteractor
) : EventHandler<ChartXmlPickerEvent, Unit> {

    override fun handle(event: ChartXmlPickerEvent) {
        when (event) {
            is ChartXmlPickerEvent.UploadFile ->
                chartFilesInteractor.add(event.uri, event.fileType)

            is ChartXmlPickerEvent.DeleteFile ->
                chartFilesInteractor.delete(event.name)

            is ChartXmlPickerEvent.SelectFile ->
                chartFilesInteractor.select(event.name)
        }
    }
}