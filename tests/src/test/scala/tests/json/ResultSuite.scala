package tests.json

import moped.internal.diagnostics.AggregateDiagnostic
import moped.json.ErrorResult
import moped.json.Result
import moped.json.ValueResult
import moped.reporters.Diagnostic
import munit.FunSuite

class ResultSuite extends FunSuite {
  val a: Diagnostic = Diagnostic.error("a")
  val b: Diagnostic = Diagnostic.error("b")
  val value: Result[Int] = Result.value(1)
  val error: Result[Int] = Result.error[Int](a)

  test("value.equals") {
    assertEquals(value, ValueResult(1))
  }
  test("value.isValue") {
    assert(value.isValue)
  }
  test("value.isError") {
    assert(!value.isError)
  }
  test("value.getOrElse") {
    assertEquals(value.getOrElse(2), 1)
  }
  test("value.orElse") {
    assertEquals(value.orElse(ValueResult(2)), ValueResult(1))
  }
  test("value.toOption") {
    assertEquals(value.toOption, Some(1))
  }
  test("value.toEither") {
    assertEquals(value.toEither, Right(1))
  }
  test("value.toList") {
    assertEquals(value.toList, List(1))
  }
  test("value.iterator") {
    assertEquals(value.iterator().toList, List(1))
  }
  test("value.foreach") {
    var foreach = -1
    value.foreach(n => foreach = n)
    assertEquals(foreach, 1)
  }
  test("value.filter-true") {
    assertEquals(value.filter(_ > 0), value)
  }
  test("value.filter-false") {
    assert(value.filter(_ > 1).isError)
  }

  test("error.equals") {
    assertEquals(error, ErrorResult(Diagnostic.error("a")))
  }
  test("error.isValue") {
    assert(!error.isValue)
  }
  test("error.isError") {
    assert(error.isError)
  }
  test("error.getOrElse") {
    assertEquals(error.getOrElse(2), 2)
  }
  test("error.orElse") {
    assertEquals(error.orElse(ValueResult(2)), ValueResult(2))
  }
  test("error.toOption") {
    assertEquals(error.toOption, None)
  }
  test("error.toEither") {
    assertEquals(error.toEither, Left(Diagnostic.error("a")))
  }
  test("error.toList") {
    assertEquals(error.toList, List())
  }
  test("error.iterator") {
    assertEquals(error.iterator().toList, List())
  }
  test("error.foreach") {
    var foreach = -1
    error.foreach(n => foreach = n)
    assertEquals(foreach, -1)
  }
  test("error.filter") {
    assertEquals(error.filter(_ > 0), error)
  }

  test("product-error-error") {
    val obtained = Result.error(a).product(Result.error(b))
    assertEquals(obtained, ErrorResult(AggregateDiagnostic(a, List(b))))
  }

  test("product-value-error") {
    val obtained = value.product(Result.error(b))
    assertEquals(obtained, Result.error(b))
  }

  test("product-error-value") {
    val obtained = Result.error(b).product(value)
    assertEquals(obtained, Result.error(b))
  }

  test("product-value-value") {
    val obtained = value.product(value)
    assertEquals(obtained, Result.value(1 -> 1))
  }

}
