package moped.cli

import java.nio.file.Files
import java.nio.file.Path

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import moped.internal.console.EagerExecutionContext
import moped.json.Result
import moped.json.JsonElement
import moped.reporters.Input

trait ConfigurationSearcher {
  def findAsync(app: Application): Future[List[Result[JsonElement]]]
}
object ConfigurationSearcher {
  def candidates(
      app: Application,
      directory: Path,
      filename: String
  ): List[Result[JsonElement]] = {
    for {
      parser <- app.parsers
      extension <- parser.supportedFileExtensions
      file = directory.resolve(s"$filename.$extension")
      if Files.isRegularFile(file) && Files.isReadable(file)
    } yield parser.parse(Input.path(file))
  }
}
abstract class BlockingConfigurationSearcher extends ConfigurationSearcher {
  final def findAsync(app: Application): Future[List[Result[JsonElement]]] = {
    Future.successful(find(app))
  }
  def find(app: Application): List[Result[JsonElement]]
}

object EmptySearcher extends BlockingConfigurationSearcher {
  def find(app: Application): List[Result[JsonElement]] = List()
}

object SystemSearcher extends BlockingConfigurationSearcher {
  def find(app: Application): List[Result[JsonElement]] = {
    ConfigurationSearcher
      .candidates(app, app.env.preferencesDirectory, app.binaryName)
  }
}

object ProjectSearcher extends BlockingConfigurationSearcher {
  def find(app: Application): List[Result[JsonElement]] = {
    val buf = mutable.ListBuffer.empty[Result[JsonElement]]
    val cwd = app.env.workingDirectory
    List(
      ConfigurationSearcher
        .candidates(app, cwd.resolve(".config"), app.binaryName),
      ConfigurationSearcher.candidates(app, cwd, "." + app.binaryName)
    ).flatten
  }
}

class AggregateSearcher(
    val underlying: List[ConfigurationSearcher],
    val ec: ExecutionContext
) extends ConfigurationSearcher {
  def this(underlying: List[ConfigurationSearcher]) =
    this(underlying, EagerExecutionContext)
  def findAsync(app: Application): Future[List[Result[JsonElement]]] = {
    implicit val e = ec
    Future.sequence(underlying.map(_.findAsync(app))).map(_.flatten)
  }
}
