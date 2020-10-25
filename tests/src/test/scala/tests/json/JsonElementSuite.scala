package tests.json

class JsonElementSuite extends BaseJsonElementSuite {

  test("isArray") {
    assert(!nul.isArray)
    assert(!string.isArray)
    assert(!bool.isArray)
    assert(!number.isArray)
    assert(array.isArray)
    assert(!obj.isArray)
  }

  test("isObject") {
    assert(!nul.isObject)
    assert(!string.isObject)
    assert(!bool.isObject)
    assert(!number.isObject)
    assert(!array.isObject)
    assert(obj.isObject)
  }

  test("isString") {
    assert(!nul.isString)
    assert(string.isString)
    assert(!bool.isString)
    assert(!number.isString)
    assert(!array.isString)
    assert(!obj.isString)
  }

  test("isBoolean") {
    assert(!nul.isBoolean)
    assert(!string.isBoolean)
    assert(bool.isBoolean)
    assert(!number.isBoolean)
    assert(!array.isBoolean)
    assert(!obj.isBoolean)
  }

  test("isNumber") {
    assert(!nul.isNumber)
    assert(!string.isNumber)
    assert(!bool.isNumber)
    assert(number.isNumber)
    assert(!array.isNumber)
    assert(!obj.isNumber)
  }

  test("isNull") {
    assert(nul.isNull)
    assert(!string.isNull)
    assert(!bool.isNull)
    assert(!number.isNull)
    assert(!array.isNull)
    assert(!obj.isNull)
  }

  test("isPrimitive") {
    assert(nul.isPrimitive)
    assert(string.isPrimitive)
    assert(bool.isPrimitive)
    assert(number.isPrimitive)
    assert(!array.isPrimitive)
    assert(!obj.isPrimitive)
  }

}
