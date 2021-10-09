package com.socrata.thirdparty.opencsv

trait CSVIteratorShim { self: CSVIterator =>
  override def toSeq: LazyList[IndexedSeq[String]] = it.to(LazyList)
}
