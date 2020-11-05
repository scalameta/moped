package tests.progressbars

import java.time.Duration

import moped.progressbars.Timer
import moped.testkit.TestingClock
import tests.BaseSuite

class TimerSuite extends BaseSuite {
  override def clock: TestingClock = _clock
  var _clock = new TestingClock()
  override def beforeEach(context: BeforeEach): Unit = {
    _clock = new TestingClock()
  }
  test("basic") {
    val timer = new Timer(clock)
    Thread.sleep(5)
    assertEquals(timer.duration(), Duration.ZERO)
    assertNoDiff(timer.toString(), "0.0s")

    clock.tick(Duration.ofMillis(49))
    assertNoDiff(timer.toString(), "0.0s")
    clock.tick(Duration.ofMillis(1))
    assertNoDiff(timer.toString(), "0.1s")
    clock.tick(Duration.ofMillis(450))
    assertNoDiff(timer.toString(), "0.5s")

    clock.tick(Duration.ofSeconds(10))
    assertNoDiff(timer.toString(), "10.5s")
    clock.tick(Duration.ofMinutes(1))
    assertNoDiff(timer.toString(), "1m10.5s")

    clock.tick(Duration.ofMinutes(10))
    assertNoDiff(timer.toString(), "11m10.5s")

    clock.tick(Duration.ofHours(1))
    assertNoDiff(timer.toString(), "1hr11m10.5s")

    clock.tick(Duration.ofHours(1))
    assertNoDiff(timer.toString(), "2hr11m10.5s")

    clock.tick(Duration.ofHours(20))
    assertNoDiff(timer.toString(), "22hr11m10.5s")

    clock.tick(Duration.ofHours(3))
    assertNoDiff(timer.toString(), "1day1hr11m10.5s")

    clock.tick(Duration.ofDays(100))
    assertNoDiff(timer.toString(), "101days1hr11m10.5s")
  }
}
