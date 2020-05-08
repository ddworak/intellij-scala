package org.jetbrains.plugins.scala.lang.formatting.scalafmt

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiElement, PsiFile}
import com.typesafe.config.{ConfigException, ConfigFactory}
import org.jetbrains.annotations.{CalledInAwt, NonNls}
import org.jetbrains.plugins.scala.extensions.inReadAction
import org.jetbrains.plugins.scala.lang.formatting.scalafmt.ScalafmtDynamicConfigService.ConfigResolveResult
import org.jetbrains.plugins.scala.lang.formatting.scalafmt.ScalafmtDynamicService.{ScalafmtResolveError, ScalafmtVersion}
import org.jetbrains.plugins.scala.lang.formatting.scalafmt.ScalafmtNotifications.FmtVerbosity
import org.jetbrains.plugins.scala.lang.formatting.scalafmt.dynamic.ScalafmtDynamicConfig

import scala.util.Try

trait ScalafmtDynamicConfigService {

  def init(): Unit

  def clearCaches(): Unit

  def configForFileWithTimestamp(
    psiFile: PsiFile,
    verbosity: FmtVerbosity = FmtVerbosity.Verbose,
    resolveFast: Boolean = false
  ): Option[(ScalafmtDynamicConfig, Option[Long])]

  final def configForFile(
    psiFile: PsiFile,
    verbosity: FmtVerbosity = FmtVerbosity.Verbose,
    resolveFast: Boolean = false
  ): Option[ScalafmtDynamicConfig] =
    configForFileWithTimestamp(psiFile, verbosity, resolveFast).map(_._1)

  def resolveConfigAsync(
    configFile: VirtualFile,
    version: ScalafmtVersion,
    verbosity: FmtVerbosity,
    @CalledInAwt
    onResolveFinished: ConfigResolveResult => Unit
  ): Unit

  def isFileIncludedInProject(file: PsiFile, config: ScalafmtDynamicConfig): Boolean

  def isFileIncludedInProject(file: PsiFile): Boolean
}

object ScalafmtDynamicConfigService {

  @NonNls
  val DefaultConfigurationFileName: String = ".scalafmt.conf"

  def instanceIn(project: Project): ScalafmtDynamicConfigService =
    project.getService(classOf[ScalafmtDynamicConfigService])

  def instanceIn(file: PsiElement): ScalafmtDynamicConfigService =
    file.getProject.getService(classOf[ScalafmtDynamicConfigService])

  def isIncludedInProject(file: PsiFile, config: ScalafmtDynamicConfig): Boolean =
    instanceIn(file.getProject).isFileIncludedInProject(file, config)

  def isIncludedInProject(file: PsiFile): Boolean =
    instanceIn(file.getProject).isFileIncludedInProject(file)

  type ConfigResolveResult = Either[ConfigResolveError, ScalafmtDynamicConfig]

  sealed trait ConfigResolveError

  object ConfigResolveError {
    sealed trait ConfigError extends ConfigResolveError
    case class ConfigFileNotFound(configPath: String) extends ConfigError
    case class ConfigParseError(configPath: String, cause: Throwable) extends ConfigError
    case class ConfigScalafmtResolveError(error: ScalafmtResolveError) extends ConfigResolveError
    case class UnknownError(message: String, cause: Option[Throwable]) extends ConfigResolveError

    object UnknownError {
      def apply(cause: Throwable): UnknownError = new UnknownError(cause.getMessage, Option(cause))
    }
  }

  def readVersion(configFile: VirtualFile): Either[Throwable, Option[String]] =
    Try {
      val document = inReadAction(FileDocumentManager.getInstance.getDocument(configFile))
      val config = ConfigFactory.parseString(document.getText)
      Option(config.getString("version").trim)
    }.toEither.left.flatMap {
      case _: ConfigException.Missing => Right(None)
      case e => Left(e)
    }
}
