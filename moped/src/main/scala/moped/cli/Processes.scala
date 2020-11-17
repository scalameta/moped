package moped.cli

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import java.util.concurrent.ConcurrentLinkedQueue

import os._

final class SpawnableProcess(
    command: Seq[Shellable],
    env: Environment,
    mocked: List[Application]
) {

  /**
   * Invokes the given subprocess like a function, passing in input and
   * returning a [[CommandResult]]. You can then call `result.exitCode` to see
   * how it exited, or `result.out.bytes` or `result.err.string` to access the
   * aggregated stdout and stderr of the subprocess in a number of convenient
   * ways. If a non-zero exit code is returned, this throws a
   * [[os.SubprocessException]] containing the [[CommandResult]], unless you
   * pass in `check = false`.
   *
   * If you want to spawn an interactive subprocess, such as `vim`, `less`, or a
   * `python` shell, set all of `stdin`/`stdout`/`stderr` to [[os.Inherit]]
   *
   * `call` provides a number of parameters that let you configure how the
   * subprocess is run:
   *
   * @param cwd
   *   the working directory of the subprocess
   * @param env
   *   any additional environment variables you wish to set in the subprocess
   * @param stdin
   *   any data you wish to pass to the subprocess's standard input
   * @param stdout
   *   How the process's output stream is configured.
   * @param stderr
   *   How the process's error stream is configured.
   * @param mergeErrIntoOut
   *   merges the subprocess's stderr stream into it's stdout
   * @param timeout
   *   how long to wait for the subprocess to complete
   * @param check
   *   disable this to avoid throwing an exception if the subprocess fails with
   *   a non-zero exit code
   * @param propagateEnv
   *   disable this to avoid passing in this parent process's environment
   *   variables to the subprocess
   */
  def call(
      cwd: java.nio.file.Path = null,
      env: Map[String, String] = null,
      stdin: ProcessInput = Pipe,
      stdout: ProcessOutput = Pipe,
      stderr: ProcessOutput = os.Inherit,
      mergeErrIntoOut: Boolean = false,
      timeout: Long = -1,
      check: Boolean = true,
      propagateEnv: Boolean = true
  ): CommandResult = {

    val chunks = new ConcurrentLinkedQueue[Either[geny.Bytes, geny.Bytes]]

    val sub = spawn(
      cwd,
      env,
      stdin,
      if (stdout ne os.Pipe)
        stdout
      else
        os.ProcessOutput
          .ReadBytes((buf, n) =>
            chunks.add(Left(new geny.Bytes(java.util.Arrays.copyOf(buf, n))))
          ),
      if (stderr ne os.Pipe)
        stderr
      else
        os.ProcessOutput
          .ReadBytes((buf, n) =>
            chunks.add(Right(new geny.Bytes(java.util.Arrays.copyOf(buf, n))))
          ),
      mergeErrIntoOut,
      propagateEnv
    )
    import collection.JavaConverters._

    sub.join(timeout)

    val chunksArr = chunks.iterator.asScala.toArray
    val res = CommandResult(sub.exitCode(), chunksArr)
    if (res.exitCode == 0 || !check)
      res
    else
      throw SubprocessException(res)
  }

  /**
   * The most flexible of the [[os.proc]] calls, `os.proc.spawn` simply
   * configures and starts a subprocess, and returns it as a `java.lang.Process`
   * for you to interact with however you like.
   *
   * To implement pipes, you can spawn a process, take it's stdout, and pass it
   * as the stdin of a second spawned process.
   *
   * Note that if you provide `ProcessOutput` callbacks to `stdout`/`stderr`,
   * the calls to those callbacks take place on newly spawned threads that
   * execute in parallel with the main thread. Thus make sure any data
   * processing you do in those callbacks is thread safe!
   */
  def spawn(
      cwd: java.nio.file.Path = null,
      env: Map[String, String] = null,
      stdin: ProcessInput = Pipe,
      stdout: ProcessOutput = Pipe,
      stderr: ProcessOutput = os.Inherit,
      mergeErrIntoOut: Boolean = false,
      propagateEnv: Boolean = true
  ): SubProcess = {
    val mockedApplication =
      for {
        binaryName <- command.headOption.flatMap(_.value.headOption).toList
        app <- mocked
        if app.binaryName == binaryName
      } yield app
    val actualWorkingDirectory = Option(cwd)
      .getOrElse(this.env.workingDirectory)

    val actualEnvironment = Map.newBuilder[String, String]
    if (propagateEnv) {
      actualEnvironment ++= this.env.environmentVariables
    }
    Option(env).foreach(actualEnvironment ++= _)

    mockedApplication.headOption match {
      case Some(app) =>
        mockedSpawn(
          app,
          actualWorkingDirectory,
          actualEnvironment.result(),
          stdin,
          stdout,
          stderr,
          mergeErrIntoOut,
          propagateEnv
        )
      case None =>
        os.proc(command: _*)
          .spawn(
            os.Path(actualWorkingDirectory),
            actualEnvironment.result(),
            stdin,
            stdout,
            stderr,
            mergeErrIntoOut,
            propagateEnv = false
          )
    }
  }

  private def mockedSpawn(
      toMock: Application,
      cwd: java.nio.file.Path,
      env: Map[String, String],
      stdin: ProcessInput,
      stdout: ProcessOutput,
      stderr: ProcessOutput,
      mergeErrIntoOut: Boolean,
      propagateEnv: Boolean
  ): SubProcess = {
    val arguments = this.command.flatMap(_.value).toList.tail
    val commandStr = arguments.mkString(" ")
    val in = new PipedInputStream()
    val out = new PipedOutputStream()
    val err = new PipedOutputStream()
    val app = toMock.withEnv(
      this
        .env
        .withWorkingDirectory(cwd)
        .withStandardInput(new BufferedReader(new InputStreamReader(in)))
        .withStandardOutput(new PrintStream(out))
        .withStandardError(new PrintStream(err))
    )
    @volatile
    var exit = -1
    val thread =
      new Thread("Mocked " + app.binaryName) {
        override def run(): Unit = {
          try {
            exit = app.run(arguments.toList)
          } finally {
            out.close()
            err.close()
            in.close()
          }
        }
      }
    val mockedProcess: Process =
      new Process {
        override def getOutputStream(): OutputStream = new PipedOutputStream(in)
        override def getInputStream(): InputStream = new PipedInputStream(out)
        override def getErrorStream(): InputStream = new PipedInputStream(err)
        override def waitFor(): Int = exitValue()
        override def exitValue(): Int = {
          thread.join()
          exit
        }
        override def destroy(): Unit = ()
      }
    lazy val sub: SubProcess =
      new SubProcess(
        mockedProcess,
        stdin
          .processInput(sub.stdin)
          .map(new Thread(_, "Mocked " + commandStr + " stdin thread")),
        stdout
          .processOutput(sub.stdout)
          .map(new Thread(_, "Mocked " + commandStr + " stdout thread")),
        stderr
          .processOutput(sub.stderr)
          .map(new Thread(_, "Mocked " + commandStr + " stderr thread"))
      )
    sub.inputPumperThread.foreach(_.start())
    sub.outputPumperThread.foreach(_.start())
    sub.errorPumperThread.foreach(_.start())
    thread.start()
    sub
  }
}
