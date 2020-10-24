package moped.internal.console

import moped.macros.ParameterShape

/**
 * Represents a valid command-line option.
 *
 * @param name
 *   the syntax that is accepted from the command-line. Normally the same as
 *   `path.mkString(".")` for canonicalized options, but non-canonical options
 *   may drop a few prefixes from the path.
 * @param path
 *   the nested keys to insert to update this option. For example, the path
 *   List("a", "b", "c") becomes the JSON object {"a": {"b": {"c":
 *   PARSED_OPTION}}}.
 * @param isCanonical
 *   a valid option can have many different names, is this the canonical name of
 *   this option? For example
 * @param shape
 */
final case class ValidOption(
    name: String,
    path: List[String],
    isCanonical: Boolean,
    shape: ParameterShape
)

object ValidOption {
  def apply(param: ParameterShape): ValidOption =
    ValidOption(param.name, param.name :: Nil, isCanonical = true, param)
}
