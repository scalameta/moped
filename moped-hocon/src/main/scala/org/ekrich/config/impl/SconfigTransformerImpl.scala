package org.ekrich.config.impl

import scala.collection.JavaConverters._
import scala.collection.mutable

import moped.reporters.Input
import org.ekrich.config.ConfigList
import org.ekrich.config.ConfigObject
import org.ekrich.config.ConfigValue
import ujson.AstTransformer
import upickle.core.ArrVisitor
import upickle.core.ObjVisitor
import upickle.core.ParseUtils
import upickle.core.Visitor

class SconfigTransformerImpl(input: Input) extends AstTransformer[ConfigValue] {
  val origin: SimpleConfigOrigin = SimpleConfigOrigin.newSimple("origin")
  override def transform[T](j: ConfigValue, f: Visitor[_, T]): T = {
    val index =
      if (j.origin != null && j.origin.lineNumber >= 0 && !input.isEmpty) {
        input.lineToOffset(j.origin.lineNumber - 1)
      } else {
        -1
      }
    j match {
      case c: ConfigObject =>
        transformObject(f, c.asScala)
      case c: ConfigList =>
        transformArray(f, c.asScala)
      case o =>
        o.unwrapped match {
          case java.lang.Boolean.TRUE =>
            f.visitTrue(index)
          case java.lang.Boolean.FALSE =>
            f.visitFalse(index)
          case c: java.lang.String =>
            f.visitString(c, index)
          case c: java.lang.Integer =>
            f.visitInt32(c, index)
          case c: java.lang.Double =>
            f.visitFloat64(c, index)
          case c: java.lang.Float =>
            f.visitFloat32(c, index)
          case c: java.lang.Long =>
            f.visitInt64(c, index)
        }
    }
  }
  override def visitArray(
      length: Int,
      index: Int
  ): ArrVisitor[ConfigValue, ConfigValue] =
    new AstArrVisitor[mutable.ListBuffer](buf =>
      new SimpleConfigList(
        origin,
        buf
          .iterator
          .collect { case a: AbstractConfigValue =>
            a
          }
          .toList
          .asJava
      )
    )
  override def visitJsonableObject(
      length: Int,
      index: Int
  ): ObjVisitor[ConfigValue, ConfigValue] =
    new AstObjVisitor[mutable.LinkedHashMap[String, ConfigValue]](map =>
      new SimpleConfigObject(
        origin,
        map
          .collect { case (k, v: AbstractConfigValue) =>
            (k, v)
          }
          .asJava
      )
    )
  override def visitNull(index: Int): ConfigValue = new ConfigNull(origin)
  override def visitFalse(index: Int): ConfigValue =
    new ConfigBoolean(origin, false)
  override def visitTrue(index: Int): ConfigValue =
    new ConfigBoolean(origin, true)
  override def visitFloat64StringParts(
      s: CharSequence,
      decIndex: Int,
      expIndex: Int,
      index: Int
  ): ConfigValue =
    ConfigNumber.newNumber(
      origin,
      if (decIndex != -1 || expIndex != -1)
        s.toString.toDouble
      else
        ParseUtils.parseIntegralNum(s, decIndex, expIndex, index),
      s.toString()
    )
  override def visitString(s: CharSequence, index: Int): ConfigValue =
    new ConfigString.Quoted(origin, s.toString())

}
