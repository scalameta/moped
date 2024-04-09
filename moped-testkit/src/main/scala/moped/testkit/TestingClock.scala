package moped.testkit

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class TestingClock(
    var underlying: Clock = Clock.fixed(
      Instant.parse("2020-11-05T11:18:03.739Z"),
      ZoneId.of("Europe/Oslo")
    )
) extends Clock {
  def tick(duration: Duration): Unit = {
    val instant = underlying.instant().plus(duration)
    underlying = Clock.fixed(instant, underlying.getZone())
  }
  def getZone(): ZoneId = underlying.getZone()
  override def withZone(x: ZoneId): Clock = underlying.withZone(x)
  def instant(): Instant = underlying.instant()
}
