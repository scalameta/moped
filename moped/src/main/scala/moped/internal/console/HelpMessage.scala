package moped.internal.console

import moped.annotations.Inline
import moped.json._
import moped.macros._
import org.typelevel.paiges.Doc
import org.typelevel.paiges.Doc._

object HelpMessage {
  def generate[T](
      default: T
  )(implicit encoder: JsonEncoder[T], settings: ClassShaper[T]): Doc = {
    def toHelp(setting: ParameterShape, value: JsonElement): (String, Doc) = {
      val name = Cases.camelToKebab(setting.name)
      val key = s"--$name: ${setting.tpe} = ${value.toDoc.render(80)} "
      key -> paragraph(setting.description.getOrElse(""))
    }

    val defaultConf = encoder.encode(default) match {
      case JsonObject(members) => members.map(_.value)
      case _ => Nil
    }

    val keyValues = settings.parametersFlat.zip(defaultConf).flatMap {
      case (setting, value) =>
        if (setting.isHidden) {
          Nil
        } else if (setting.annotations.exists(_.isInstanceOf[Inline])) {
          for {
            underlying <- setting.underlying.toList
            (field, fieldDefault) <- underlying.parametersFlat.zip(value match {
              case obj: JsonObject => obj.members.map(_.value)
              case _ => List(value)
            })
          } yield toHelp(field, fieldDefault)
        } else {
          toHelp(setting, value) :: Nil
        }
      case _ => Nil
    }
    tabulate(keyValues)
  }
}
