package org.jetbrains.plugins.scala
package console

import com.intellij.execution.Executor
import com.intellij.execution.configurations._
import com.intellij.execution.filters.TextConsoleBuilderImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.runner.BaseRunConfiguration

/**
 * User: Alexander Podkhalyuzin
 * Date: 10.02.2009
 */

class ScalaConsoleRunConfiguration(project: Project, configurationFactory: ConfigurationFactory, name: String)
  extends BaseRunConfiguration(project, configurationFactory, name) {

  override val mainClass = "org.jetbrains.plugins.scala.compiler.rt.ConsoleRunner"

  def apply(params: ScalaConsoleRunConfigurationForm) {
    javaOptions = params.getJavaOptions
    consoleArgs = params.getConsoleArgs
    workingDirectory = params.getWorkingDirectory
    setModule(params.getModule)
  }

  override def getState(executor: Executor, env: ExecutionEnvironment): RunProfileState = {
    val state = new JavaCommandLineState(env) {
      protected override def createJavaParameters: JavaParameters = {
        val params = createParams
        params.getProgramParametersList.addParametersString(consoleArgs)
        params
      }
    }

    val consoleBuilder = new TextConsoleBuilderImpl(project) {
      override def getConsole: ConsoleView = ScalaLanguageConsoleBuilder.createConsole(project)
    }
    state.setConsoleBuilder(consoleBuilder)
    state
  }

  override def getConfigurationEditor: SettingsEditor[_ <: RunConfiguration] = new ScalaConsoleRunConfigurationEditor(project, this)
}
