package tests.cli

import munit.FunSuite
import moped.cli.CancelToken
import moped.internal.console.EagerExecutionContext
import scala.concurrent.ExecutionContext

class CancelTokenSuite extends FunSuite {
  implicit val ec =
    new ExecutionContext {
      def execute(runnable: Runnable): Unit = runnable.run()
      def reportFailure(cause: Throwable): Unit = throw cause
    }

  EagerExecutionContext
  test("toString") {
    assertNoDiff(
      CancelToken.empty().toString(),
      "CancelToken(isCanceled=false)"
    )
  }

  test("cancel") {
    val token = CancelToken.empty()
    assert(!token.isCanceled, token)
    assert(token.cancel())
    assert(token.isCanceled, token)
    assert(!token.cancel())
  }

  test("onCanceled") {
    var result = Option.empty[Boolean]
    val token = CancelToken.empty()
    token
      .onCanceled
      .future
      .foreach { value =>
        result = Some(value)
      }
    assert(!token.isCanceled)
    assertEquals(result, None)
    assert(token.cancel())
    assertEquals(result, Some(true))
  }

  test("exception") {
    val all = List(CancelToken.empty(), CancelToken.empty())
    case class Boom() extends RuntimeException("boom")
    all.foreach { token =>
      token
        .onCanceled
        .future
        .foreach { _ =>
          throw Boom()
        }
    }
    assert(all.forall(!_.isCanceled))
    val e =
      interceptMessage[Boom]("boom") {
        CancelToken.cancelAll(all)
      }
    assertEquals(e.getSuppressed().toList, List(Boom()))
    assert(all.forall(_.isCanceled))
    assert(!CancelToken.cancelAll(all)) // No exception
  }

  test("create") {
    var isCanceled = false
    val token = CancelToken.create(() => isCanceled = true)
    assert(!isCanceled)
    assert(token.cancel())
    assert(isCanceled)
    assert(!token.cancel())
  }

  test("fromIterable") {
    val all = List(CancelToken.empty(), CancelToken.empty())
    assert(clue(all).forall(!_.isCanceled))
    assert(CancelToken.cancelAll(all))
    assert(clue(all).forall(_.isCanceled))
  }

}
