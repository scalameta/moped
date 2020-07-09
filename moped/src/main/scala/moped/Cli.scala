package moped.internal

import moped._
import moped.annotation.Inline
import moped.internal.diagnostics.TypeMismatchDiagnostic
import moped.generic.Setting
import moped.generic.Settings
import org.typelevel.paiges.Doc
import org.typelevel.paiges.Doc._

object Cli {
  def help[T: JsonEncoder](default: T)(implicit settings: Settings[T]): Doc = {
    def toHelp(setting: Setting, value: JsonElement) = {
      val name = Case.camelToKebab(setting.name)
      val key = s"--$name: ${setting.tpe} = $value "
      key -> paragraph(setting.description.getOrElse(""))
    }

    val defaultConf = JsonEncoder[T].encode(default) match {
      case JsonObject(members) => members.map(_.value)
      case els =>
        ???
      // new TypeMismatchDiagnostic(
      //   "JsonObject",
      //   els.productPrefix,
      //   els.position,
      //   NoCursor
      // )
    }

    val keyValues = settings.settings.zip(defaultConf).flatMap {
      case (setting, value) =>
        if (setting.isHidden) {
          Nil
        } else if (setting.annotations.exists(_.isInstanceOf[Inline])) {
          for {
            underlying <- setting.underlying.toList
            (field, JsonMember(_, fieldDefault)) <-
              underlying.settings
                .zip(value.asInstanceOf[JsonObject].members)
          } yield toHelp(field, fieldDefault)
        } else {
          toHelp(setting, value) :: Nil
        }
    }
    tabulate(keyValues)
  }
}
