package tests

class CommandLineParserSuite extends BaseSuite {
  override def fatalUnknownFields: Boolean = true

  checkOutput(
    "nested-boolean",
    List("example-nested", "--nested.a", "--nested.b"),
    """|nested.a=true
       |nested.b=true
       |""".stripMargin
  )

  checkOutput(
    "inline-boolean",
    List("example-nested", "--ia", "--ib"),
    """|inline.a=true
       |inline.b=true
       |""".stripMargin
  )

  checkOutput(
    "nested-no-boolean",
    List("example-nested", "--no-nested.e", "--no-nested.g"),
    """|nested.e=false
       |nested.g=false
       |""".stripMargin
  )

  checkOutput(
    "inline-no-boolean",
    List("example-nested", "--no-ie", "--no-ig"),
    """|inline.ie=false
       |inline.ig=false
       |""".stripMargin
  )

  checkOutput(
    "inline-nested-mix-boolean",
    List("example-nested", "--ia", "--nested.a"),
    """|nested.a=true
       |inline.a=true
       |""".stripMargin
  )

  checkOutput(
    "string-nested",
    List("example-nested", "--nested.c", "hello"),
    """|nested.c=hello
       |""".stripMargin
  )

  checkOutput(
    "string-inline",
    List("example-nested", "--ic", "hello"),
    """|inline.ic=hello
       |""".stripMargin
  )

  checkOutput(
    "repeated-string-nested",
    List("example-nested", "--nested.c", "hello"),
    """|nested.c=hello
       |""".stripMargin
  )

  checkOutput(
    "repeated-string-inline",
    List("example-nested", "--id", "hello1", "--id", "hello2"),
    """|inline.id=hello1,hello2
       |""".stripMargin
  )

  checkOutput(
    "inline-conflict-boolean",
    List("example-nested", "--ih"),
    """|inline.ih=true
       |inline2.ih=true
       |""".stripMargin
  )

  checkOutput(
    "inline-conflict-string",
    List("example-nested", "--ii", "value"),
    """|inline.ii=value
       |inline2.ii=value
       |""".stripMargin
  )

  checkOutput(
    "nested-app",
    List("example-nested", "--print-cwd"),
    """|cwd=/workingDirectory
       |""".stripMargin
  )

  checkErrorOutput(
    "inline-conflict-string-boolean",
    List("example-nested", "--ij", "value"),
    """|error: 
       |invalid usage of @Inline. The field name 'ij' inlines to conflicting nested parameters {inlined.ij,inlined2.ij}, which mix boolean and non-boolean parameters.
       |You can only fix this problem by changing the source code of this command-line tool.
       |To fix this problem, you can try one of the following.
       |  1) change the types of the parameters to be only boolean or non-boolean
       |  2) remove the @Inline annotation for one of the nested parameters
       |""".stripMargin
  )

  checkOutput(
    "positional-boolean",
    List("example-fallback", "--invalid", "value1", "--flag", "value2"),
    """|flag=true
       |List(--invalid, value1, value2)
       |""".stripMargin
  )

  checkErrorOutput(
    "type-mismatch-object",
    List("example-nested", "--nested", "value"),
    """|error: Type mismatch at '.nested';
       |  found    : String
       |  expected : Object
       |""".stripMargin
  )

  checkErrorOutput(
    "dym-boolean",
    List("example-nested", "--iii"),
    """|error: found argument '--iii' which wasn't expected, or isn't valid in this context.
       |	Did you mean '--ii'?
       |""".stripMargin
  )

  checkOutput(
    "int",
    List("parser", "--port", "4040"),
    """|port=4040
       |""".stripMargin
  )

  checkOutput(
    "long",
    List("parser", "--long-port", "4040"),
    """|longPort=4040
       |""".stripMargin
  )

  checkOutput(
    "double",
    List("parser", "--ratio", "0.3"),
    """|ratio=0.3
       |""".stripMargin
  )

  checkOutput(
    "float",
    List("parser", "--float-ratio", "0.3"),
    """|floatRatio=0.3
       |""".stripMargin
  )

  // TODO: did you mean?
}
