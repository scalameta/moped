package moped.internal.console

import moped._
import moped.json._
import moped.generic._
import moped.annotations.Inline
import moped.internal.diagnostics.TypeMismatchDiagnostic
import org.typelevel.paiges.Doc
import org.typelevel.paiges.Doc._

object HelpMessage {
  def generate[T: JsonEncoder](
      default: T
  )(implicit settings: ClassDefinition[T]): Doc = {
    def toHelp(setting: ParameterDefinition, value: JsonElement) = {
      val name = Cases.camelToKebab(setting.name)
      val key = s"--$name: ${setting.tpe} = $value "
      key -> paragraph(setting.description.getOrElse(""))
    }

    val defaultConf = JsonEncoder[T].encode(default) match {
      case JsonObject(members) => members.map(_.value)
      case els                 => Nil
    }

    val keyValues = settings.settings.zip(defaultConf).flatMap {
      case (setting, value: JsonObject) =>
        if (setting.isHidden) {
          Nil
        } else if (setting.annotations.exists(_.isInstanceOf[Inline])) {
          for {
            underlying <- setting.underlying.toList
            (field, JsonMember(_, fieldDefault)) <-
              underlying.settings.zip(value.members)
          } yield toHelp(field, fieldDefault)
        } else {
          toHelp(setting, value) :: Nil
        }
      case _ => Nil
    }
    tabulate(keyValues)
  }
}
