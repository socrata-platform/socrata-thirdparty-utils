package com.socrata.thirdparty.json

import com.rojoma.json.v3.codec.{JsonDecode, JsonEncode}
import com.socrata.thirdparty.json.AdditionalJsonCodecs._
import org.joda.time.DateTime
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{FunSuite, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class AdditionalJsonCodecsTest extends FunSuite with Matchers with ScalaCheckPropertyChecks {

  val minDateTime = DateTime.now
    .year.withMinimumValue
    .monthOfYear.withMinimumValue
    .dayOfMonth.withMinimumValue
    .hourOfDay.withMinimumValue
    .minuteOfHour.withMinimumValue
    .secondOfMinute.withMinimumValue
    .millisOfSecond.withMinimumValue

  val maxDateTime = DateTime.now
    .year.withMaximumValue
    .monthOfYear.withMaximumValue
    .dayOfMonth.withMaximumValue
    .hourOfDay.withMaximumValue
    .minuteOfHour.withMaximumValue
    .secondOfMinute.withMaximumValue
    .millisOfSecond.withMaximumValue

  implicit val ArbitraryDateTime = Arbitrary {
    for {x <- Gen.choose(minDateTime.getMillis, maxDateTime.getMillis)} yield new DateTime(x)
  }

  test("DateTime Codec") {
    forAll { (x: DateTime) =>
      val jv = JsonEncode.toJValue(x)
      val y = JsonDecode.fromJValue[DateTime](jv)
      Right(x) should be (y)
    }
  }
}
