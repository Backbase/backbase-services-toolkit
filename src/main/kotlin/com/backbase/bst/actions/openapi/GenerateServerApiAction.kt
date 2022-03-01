package com.backbase.bst.actions.openapi

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.MavenTools
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.reflect.DomCollectionChildDescription
import org.jetbrains.idea.maven.dom.MavenDomUtil
import org.jetbrains.idea.maven.dom.model.MavenDomGoal
import org.jetbrains.idea.maven.dom.model.MavenDomPlugin
import org.jetbrains.idea.maven.dom.model.MavenDomPlugins
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil


class GenerateServerApiAction : DumbAwareAction() {

    private val title = "Generating OpenApi client"

    override fun update(e: AnActionEvent) {

        //Disable if the File selected file in not pom.xml
        if (!SpecUtils.isPomFileSelected(e)) {
            e.presentation.isEnabledAndVisible = false
            return
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = MavenActionUtil.getProject(e.dataContext) ?: return
        val selectedModule = e.getData(LangDataKeys.MODULE)
        val fileDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
            .withDescription(BackbaseBundle.message("action.add.openapi.server.api.select.file"))
            .withRoots(project.guessProjectDir())
            .withShowHiddenFiles(false)
        //Select spec file. Return if can
        val selectedSpecFile = FileChooser.chooseFile(fileDescriptor, project, project.guessProjectDir())
        if (selectedSpecFile == null || !SpecUtils.isFileAnOpenApiSpec(selectedSpecFile)) {
            return
        }

        val dialog = GenerateServerApiDialog(project, selectedSpecFile)
        dialog.show()
        if (dialog.exitCode === DialogWrapper.CANCEL_EXIT_CODE) {
            return
        }

        //Select pomfile only from the selected pom.xml if a multimodule project
        val projectPomFile: VirtualFile = selectedModule?.let { MavenTools.findProjectPom(project, it) } ?: return

        //Add additional dependencies if not present
        addAdditionalDependencies(project, projectPomFile, e.dataContext)

        val boatPluginId = MavenId("com.backbase.oss", "boat-maven-plugin", "")

        //Add Boat maven plugin or just add the execution
        if (!MavenTools.findPluginOnBom(project, projectPomFile, boatPluginId)) {
            WriteCommandAction.runWriteCommandAction(project) {
                createDomPlugin(
                    MavenDomUtil
                        .getMavenDomProjectModel(project, projectPomFile)!!.build.plugins,
                    project,
                    dialog,
                    selectedSpecFile
                )

            }
            Notification(
                "Backbase notification group", BackbaseBundle.message("action.add.openapi.server.api.title"),
                BackbaseBundle.message("action.add.openapi.message.adding.boat.plugin"),
                NotificationType.INFORMATION
            ).notify(project)
        } else {
            // Only add execution to exsisting plugin
            val plugin = MavenDomUtil
                .getMavenDomProjectModel(
                    project,
                    projectPomFile
                )!!.build.plugins.plugins.find { it.artifactId.stringValue == "boat-maven-plugin" }
            val executionId = "generate-" + SpecUtils.extractServiceNameWithType(selectedSpecFile.name) + "-api-code"
            plugin?.executions?.executions?.forEach {
                if (it.id.stringValue.equals(executionId)) {
                    Notification(
                        "Backbase notification group", BackbaseBundle.message("action.add.openapi.server.api.title"),
                        BackbaseBundle.message("action.add.openapi.message.boat.execution.present"),
                        NotificationType.WARNING
                    ).notify(project)
                    return
                }
            }

            WriteCommandAction.runWriteCommandAction(project) {
                createPluginExecution(plugin!!, project, dialog, selectedSpecFile)
            }
        }
        val mavenProjectManager = MavenProjectsManager.getInstance(project)
        mavenProjectManager.forceUpdateProjects(mavenProjectManager.projects)

        mavenProjectManager.waitForPostImportTasksCompletion()

    }

    private fun createDomPlugin(
        plugins: MavenDomPlugins?,
        project: Project,
        dialog: GenerateServerApiDialog,
        selectedSpecFile: VirtualFile
    ): MavenDomPlugin {
        val plugin = plugins!!.addPlugin()
        plugin!!.groupId.stringValue = "com.backbase.oss"
        plugin.artifactId.stringValue = "boat-maven-plugin"

        createPluginExecution(plugin, project, dialog, selectedSpecFile)

        return plugin
    }

    private fun createPluginExecution(
        plugin: MavenDomPlugin,
        project: Project,
        dialog: GenerateServerApiDialog,
        selectedSpecFile: VirtualFile
    ): MavenDomPlugin {

        val execution = plugin.executions.addExecution()
        execution.id.stringValue =
            "generate-" + SpecUtils.extractServiceNameWithType(selectedSpecFile.name) + "-api-code"
        execution.phase.stringValue = "generate-sources"
        val goals = execution.goals

        val childDescription: DomCollectionChildDescription =
            goals.genericInfo.getCollectionChildDescription("goal")!!
        if (childDescription != null) {
            val element: DomElement = childDescription.addValue(goals)
            if (element is MavenDomGoal) {
                element.stringValue = "generate-spring-boot-embedded"
            }
        }


        val configuration = execution.configuration
        configuration.ensureTagExists()

        SpecUtils.addElement(configuration, "inputSpec", dialog.specPath)
        SpecUtils.addElement(configuration, "apiPackage", dialog.apiPackage)
        SpecUtils.addElement(configuration, "modelPackage", dialog.modelPackage)

        return plugin
    }

    private fun addAdditionalDependencies(project: Project, pomFile: VirtualFile, dataContext: DataContext) {
        //Mutable because it needs to filter out of things already present in pom
        val dependencies = mutableListOf(
            MavenId("io.swagger", "swagger-annotations", ""),
            MavenId("org.openapitools", "jackson-databind-nullable", ""),
            MavenId("org.springframework.data", "spring-data-commons", ""),
            MavenId("io.springfox", "springfox-core", "3.0.0"),
            MavenId("com.backbase.buildingblocks", "spring-security-csrf", "")
        )

        SpecUtils.addAdditionalDependencies(dependencies, project, pomFile, dataContext, title)
    }
}