package com.github.aivanovski.testwithme.web.data.database.converters

import com.github.aivanovski.testwithme.web.entity.Timestamp
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class TimestampConverter : AttributeConverter<Timestamp?, String?> {

    override fun convertToDatabaseColumn(attribute: Timestamp?): String? {
        return attribute?.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): Timestamp? {
        return dbData?.let { Timestamp.fromString(dbData) }
    }
}