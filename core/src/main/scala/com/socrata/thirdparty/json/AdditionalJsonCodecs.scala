package com.socrata.thirdparty.json

import com.rojoma.json.v3.util.WrapperJsonCodec
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat


object AdditionalJsonCodecs {

  implicit val dateTimeCodec = locally {
    val formatter = ISODateTimeFormat.dateTime.withZoneUTC
    val parser = ISODateTimeFormat.dateTimeParser

    WrapperJsonCodec[DateTime](parser.parseDateTime, formatter.print)
  }
}