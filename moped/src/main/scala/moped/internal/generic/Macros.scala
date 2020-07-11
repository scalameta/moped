package moped.internal.generic

import scala.annotation.StaticAnnotation
import scala.reflect.macros.blackbox
import moped.console._
import moped.json._
import moped.generic._
import java.nio.file.Path
import java.io.File
import moped.generic.ClassDefinition

object Macros
class Macros(val c: blackbox.Context) {
  import c.universe._
  def assumeClass[T: c.WeakTypeTag]: Type = {
    val T = weakTypeOf[T]
    if (!T.typeSymbol.isClass || !T.typeSymbol.asClass.isCaseClass)
      c.abort(c.enclosingPosition, s"$T must be a case class")
    T
  }

  def params(T: Type): List[Symbol] = {
    val paramss = T.typeSymbol.asClass.primaryConstructor.asMethod.paramLists
    if (paramss.lengthCompare(1) > 0) {
      c.abort(
        c.enclosingPosition,
        s"${T.typeSymbol} has a curried parameter list, which is not supported."
      )
    }
    paramss.head
  }

  def deriveJsonCodecImpl[T: c.WeakTypeTag](default: Tree): Tree = {
    val T = assumeClass[T]
    q"""
        _root_.moped.json.JsonCodec.EncoderDecoderToCodec[$T](
          _root_.moped.generic.deriveEncoder[$T],
          _root_.moped.generic.deriveDecoder[$T]($default)
        )
     """
  }

  def deriveJsonEncoderImpl[T: c.WeakTypeTag]: Tree = {
    val T = assumeClass[T]
    val params = this.params(T)
    val writes = params.map { param =>
      val name = param.name.decodedName.toString
      val select = Select(q"value", param.name)
      val encoder =
        q"_root_.scala.Predef.implicitly[_root_.moped.json.JsonEncoder[${param.info}]]"
      q"_root_.moped.json.JsonMember(_root_.moped.json.JsonString($name), $encoder.write($select))"
    }
    val result = q"""
       new ${weakTypeOf[JsonEncoder[T]]} {
         override def write(value: ${weakTypeOf[T]}): _root_.moped.Conf = {
           new _root_.moped.json.JsonObject(
             _root_.scala.List.apply(..$writes)
           )
         }
       }
     """
    result
  }

  def deriveJsonDecoderImpl[T: c.WeakTypeTag](default: Tree): Tree = {
    val T = assumeClass[T]
    val Tclass = T.typeSymbol.asClass
    val settings = c.inferImplicitValue(weakTypeOf[ClassDefinition[T]])
    if (settings == null || settings.isEmpty) {
      c.abort(
        c.enclosingPosition,
        s"Missing implicit for ${weakTypeOf[ClassDefinition[T]]}]. " +
          s"Hint, add `implicit val surface: ${weakTypeOf[ClassDefinition[T]]}` " +
          s"to the companion ${T.companion.typeSymbol}"
      )
    }
    val paramss = Tclass.primaryConstructor.asMethod.paramLists
    if (paramss.size > 1) {}
    if (paramss.head.isEmpty)
      return q"_root_.moped.JsonDecoder.constant($default)"

    val (head :: params) :: Nil = paramss
    def next(param: Symbol): Tree = {
      if (param.info <:< typeOf[Environment]) {
        q"context.environment"
      } else {
        val P = param.info.resultType
        val name = param.name.decodedName.toString
        val getter = T.member(param.name)
        val fallback = q"tmp.$getter"
        val next =
          q"conf.getSettingOrElse[$P](settings.unsafeGet($name), $fallback)"
        next
      }
    }
    val product = params.foldLeft(next(head)) {
      case (accum, param) => q"$accum.product(${next(param)})"
    }
    val tupleExtract = 1.to(params.length).foldLeft(q"t": Tree) {
      case (accum, _) => q"$accum._1"
    }
    var curr = tupleExtract
    val args = 0.to(params.length).map { _ =>
      val old = curr
      curr = curr match {
        case q"$qual._1" =>
          q"$qual._2"
        case q"$qual._1._2" =>
          q"$qual._2"
        case q"$qual._2" =>
          q"$qual"
        case q"t" => q"t"
      }
      old
    }
    val ctor = q"new $T(..$args)"

    val result = q"""
       new ${weakTypeOf[JsonDecoder[T]]} {
         def read(
             context: _root_.moped.json.DecoderContext
         ): ${weakTypeOf[DecodingResult[T]]} = {
           val conf = context.conf
           val settings = $settings
           val tmp = $default
           $product.map { t =>
             $ctor
           }
         }
       }
     """
    result
  }

  def deriveSurfaceImpl[T: c.WeakTypeTag]: Tree = {
    val T = weakTypeOf[T]
    if (!T.typeSymbol.isClass || !T.typeSymbol.asClass.isCaseClass)
      c.abort(c.enclosingPosition, s"$T must be a case class")
    val Tclass = T.typeSymbol.asClass
    val ctor = Tclass.primaryConstructor.asMethod
    val argss = ctor.paramLists.map { params =>
      val fields = params.map { param =>
        val paramTpe = param.info.resultType
        val baseAnnots = param.annotations.collect {
          case annot if annot.tree.tpe <:< typeOf[StaticAnnotation] =>
            annot.tree
        }
        val isMap = paramTpe <:< typeOf[Map[_, _]]
        val isConf = paramTpe <:< typeOf[JsonElement]
        val isIterable = paramTpe <:< typeOf[Iterable[_]] && !isMap
        val repeated =
          if (isIterable) {
            q"new _root_.moped.annotations.Repeated" :: Nil
          } else {
            Nil
          }
        val dynamic =
          if (isMap || isConf) {
            q"new _root_.moped.annotations.Dynamic" :: Nil
          } else {
            Nil
          }
        val flag =
          if (paramTpe <:< typeOf[Boolean]) {
            q"new _root_.moped.annotations.Flag" :: Nil
          } else {
            Nil
          }

        val tabCompletePath =
          if (paramTpe <:< typeOf[Path] || paramTpe <:< typeOf[File]) {
            q"new _root_.moped.annotations.TabCompleteAsPath" :: Nil
          } else {
            Nil
          }

        val finalAnnots =
          repeated ::: dynamic ::: flag ::: tabCompletePath ::: baseAnnots
        val fieldsParamTpe = c.internal.typeRef(
          NoPrefix,
          weakTypeOf[ClassDefinition[_]].typeSymbol,
          paramTpe :: Nil
        )
        val underlyingInferred = c.inferImplicitValue(fieldsParamTpe)
        val underlying =
          if (underlyingInferred == null || underlyingInferred.isEmpty) {
            q"_root_.scala.Nil"
          } else {
            q"$underlyingInferred.fields"
          }
        val tprint = c.internal.typeRef(
          NoPrefix,
          weakTypeOf[pprint.TPrint[_]].typeSymbol,
          paramTpe :: Nil
        )
        val tpeString = c.inferImplicitValue(tprint)

        val field = q"""new ${weakTypeOf[ClassParameter]}(
           ${param.name.decodedName.toString},
           $tpeString.render,
           _root_.scala.List.apply(..$finalAnnots),
           $underlying
         )"""
        field
      }
      val args = q"_root_.scala.List.apply(..$fields)"
      args
    }
    val args = q"_root_.scala.List.apply(..$argss)"
    val classAnnotations = Tclass.annotations.collect {
      case annot if annot.tree.tpe <:< typeOf[StaticAnnotation] =>
        annot.tree
    }
    val result =
      q"new ${weakTypeOf[ClassDefinition[T]]}($args, _root_.scala.List.apply(..$classAnnotations))"
    c.untypecheck(result)
  }

}
