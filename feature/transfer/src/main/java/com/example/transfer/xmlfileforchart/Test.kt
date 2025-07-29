package com.example.transfer.xmlfileforchart

import android.content.Context
import android.util.Log
import com.example.transfer.chart.domain.model.SignalCode
import com.example.transfer.chart.domain.model.SignalSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject


class XmlReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun openXmlStream(fileName: String): InputStream {
        validateXmlFile(fileName)
        return context.assets.open(fileName)
    }

    private fun validateXmlFile(fileName: String) {
        require(fileName.endsWith(".xml", ignoreCase = true)) {
            "XmlReader can only read .xml files: '$fileName' is not valid"
        }
    }

}

class SignalSettingsParser @Inject constructor(
    private val xmlReader: XmlReader
) {
    fun parse(xmlFileName: String): List<SignalSettings> {
        xmlReader.openXmlStream(xmlFileName).use { inputStream ->
            val parser = createParser(inputStream)
            return parseFields(parser)
        }
    }


    private fun createParser(inputStream: InputStream): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        return factory.newPullParser().apply {
            setInput(inputStream.reader())
        }
    }

    private fun parseFields(parser: XmlPullParser): List<SignalSettings> {
        val result = mutableListOf<SignalSettings>()
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "field") {
                    parseField(parser)?.let { result.add(it) }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            Log.e("SignalSettingsParser", "XML parsing failed", e)
        } catch (e: IOException) {
            Log.e("SignalSettingsParser", "IO error during XML parsing", e)
        }
        return result
    }


    private fun parseField(parser: XmlPullParser): SignalSettings? {
        val name = parser.getAttributeValue(null, "name") ?: return null
        val comment = parser.getAttributeValue(null, "comment") ?: name
        val offset = parser.getAttributeValue(null, "offset")?.toIntOrNull() ?: 0
        val type = parser.getAttributeValue(null, "type") ?: ""
        val codes = mutableListOf<SignalCode>()


        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "field")) {
            if (type == "e8" && eventType == XmlPullParser.START_TAG && parser.name == "code") {
                val value = parser.getAttributeValue(null, "value")?.toIntOrNull()
                val desc = parser.getAttributeValue(null, "desc") ?: ""
                if (value != null) {
                    codes.add(SignalCode(value, desc))
                }
            }
            eventType = parser.next()
        }

        return SignalSettings(
            name = name,
            comment = comment,
            offset = offset,
            type = type,
            codes = codes
        )
    }
}

