package org.jetbrains.compose.internal.gradle.publishing.sonatype

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.MediaType.Companion.toMediaType

internal object Xml {
    val mediaType = "application/xml".toMediaType()

    fun serialize(value: Any): String =
        kotlinXmlMapper.writeValueAsString(value)

    inline fun <reified T> deserialize(xml: String): T =
        kotlinXmlMapper.readValue(xml, T::class.java)

    private val kotlinXmlMapper: ObjectMapper =
        XmlMapper(JacksonXmlModule().apply {
            setDefaultUseWrapper(false)
        }).registerKotlinModule()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}
