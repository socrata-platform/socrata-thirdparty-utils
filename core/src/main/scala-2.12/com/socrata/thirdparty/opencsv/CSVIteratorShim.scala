package com.socrata.thirdparty.opencsv

trait CSVIteratorShim { self: CSVIterator =>
  override def toStream = it.toStream
  override def toSeq = it.toStream
}
