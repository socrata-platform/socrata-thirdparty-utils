package com.socrata.thirdparty

private[thirdparty] object EitherCompat {
  implicit class EC[L, R](private val underlying: Either[L, R]) extends AnyVal {
    def leftProjection = underlying.left
    def rightProjection = underlying
    def getOrThrow = underlying.getOrElse {
      throw new NoSuchElementException("right.get")
    }
  }

  implicit class LP[L, R](private val underlying: Either.LeftProjection[L, R]) extends AnyVal {
    def getOrThrow = underlying.getOrElse {
      throw new NoSuchElementException("left.get")
    }
  }
}
