package tests

import moped.json.JsonCodec

final case class VeryNestedOptions3(
    a: Int = 42
)
object VeryNestedOptions3 {
  implicit val codec3: JsonCodec[VeryNestedOptions3] =
    moped.macros.deriveCodec(VeryNestedOptions3())
}

final case class VeryNestedOptions2(
    nested: VeryNestedOptions3 = VeryNestedOptions3()
)
object VeryNestedOptions2 {
  implicit val codec3: JsonCodec[VeryNestedOptions2] =
    moped.macros.deriveCodec(VeryNestedOptions2())
}

final case class VeryNestedOptions1(
    nested: VeryNestedOptions2 = VeryNestedOptions2()
)
object VeryNestedOptions1 {
  implicit val codec3: JsonCodec[VeryNestedOptions1] =
    moped.macros.deriveCodec(VeryNestedOptions1())
}

final case class VeryNestedOptions(
    nested: VeryNestedOptions1 = VeryNestedOptions1()
)
object VeryNestedOptions {
  implicit val codec: JsonCodec[VeryNestedOptions] =
    moped.macros.deriveCodec(VeryNestedOptions())
}
