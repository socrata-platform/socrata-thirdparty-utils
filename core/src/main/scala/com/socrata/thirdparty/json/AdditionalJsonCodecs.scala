package com.socrata.thirdparty.json

import com.rojoma.json.v3.ast.{JString, JValue}
import com.rojoma.json.v3.codec.DecodeError.{InvalidType, InvalidValue}
import com.rojoma.json.v3.codec.JsonDecode._
import com.rojoma.json.v3.codec.{JsonDecode, JsonEncode}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

object AdditionalJsonCodecs {

  implicit object DateTimeCodec extends JsonEncode[DateTime] with JsonDecode[DateTime] {
    val formatter = ISODateTimeFormat.dateTime
    val parser = ISODateTimeFormat.dateTimeParser

    def encode(x: DateTime): JValue = JString(formatter.print(x))

    def decode(x: JValue): DecodeResult[DateTime] = x match {
      case JString(s) =>
        try {
          Right(parser.parseDateTime(s))
        } catch {
          case _: IllegalArgumentException =>
            Left(InvalidValue(x))
        }
      case u => Left(InvalidType(JString, u.jsonType))
    }
  }
}