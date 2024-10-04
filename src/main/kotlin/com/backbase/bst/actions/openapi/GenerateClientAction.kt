package com.backbase.bst.actions.openapi

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.FileTools
import com.backbase.bst.common.MavenTools
import com.backbase.bst.common.SsdkUtils
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.ui.GotItMessage
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.reflect.DomCollectionChildDescription
import org.jetbrains.idea.maven.dom.MavenDomUtil
import org.jetbrains.idea.maven.dom.model.MavenDomGoal
import org.jetbrains.idea.maven.dom.model.MavenDomPlugin
import org.jetbrains.idea.maven.dom.model.MavenDomPlugins
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil
import java.io.File
import java.nio.file.Paths
import java.util.*

class GenerateClientAction : DumbAwareAction() {

    private val title = "Generating OpenApi client"

    override fun update(e: AnActionEvent) {

        //Disable if the File is not a valid Open Api file
        if (!SpecUtils.isFileAnOpenApiSpec(e)) {
            e.presentation.isEnabledAndVisible = false
            return
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = MavenActionUtil.getProject(e.dataContext) ?: return
        val selectedModule = e.getData(LangDataKeys.MODULE) // If a multimodule project
        val dialog = GenerateClientDialog(project, SpecUtils.getFileName(e))
        dialog.show()
        if (dialog.exitCode == DialogWrapper.CANCEL_EXIT_CODE) {
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
                        .getMavenDomProjectModel(project, projectPomFile)!!.build.plugins, dialog
                )

            }
            Notification(
                "Backbase notification group", BackbaseBundle.message("action.add.openapi.client.title"),
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
            val executionId = "generate-" + dialog.serviceName + "-client-code"
            plugin?.executions?.executions?.forEach {
                if (it.id.stringValue.equals(executionId)) {
                    Notification(
                        "Backbase notification group", BackbaseBundle.message("action.add.openapi.client.title"),
                        BackbaseBundle.message("action.add.openapi.message.boat.execution.present"),
                        NotificationType.WARNING
                    ).notify(project)
                    return
                }
            }

            WriteCommandAction.runWriteCommandAction(project) {
                createPluginExecution(plugin!!, dialog)
            }
        }
        val mavenProjectManager = MavenProjectsManager.getInstance(project)
        mavenProjectManager.forceUpdateAllProjectsOrFindAllAvailablePomFiles()

        if (dialog.addRestClientConfiguration) {
            addRestClientConfigClass(selectedModule, project, "restClientConfiguration", dialog)

            val gotIt = GotItMessage.createMessage(
                BackbaseBundle.message("action.add.define.event.dialog.gotit.title"),
                BackbaseBundle.message("action.add.openapi.client.gotit.message")
            )

            gotIt.show(
                RelativePoint.getCenterOf(FileEditorManager.getInstance(project).selectedTextEditor!!.component),
                Balloon.Position.above
            )
        }

    }

    private fun createDomPlugin(plugins: MavenDomPlugins?, dialog: GenerateClientDialog): MavenDomPlugin {
        val plugin = plugins!!.addPlugin()
        plugin!!.groupId.stringValue = "com.backbase.oss"
        plugin.artifactId.stringValue = "boat-maven-plugin"

        createPluginExecution(plugin, dialog)

        return plugin
    }

    private fun createPluginExecution(
        plugin: MavenDomPlugin,
        dialog: GenerateClientDialog
    ): MavenDomPlugin {

        val execution = plugin.executions.addExecution()
        execution.id.stringValue = "generate-" + dialog.serviceName + "-client-code"
        execution.phase.stringValue = "generate-sources"
        val goals = execution.goals

        val childDescription: DomCollectionChildDescription =
            goals.genericInfo.getCollectionChildDescription("goal")!!
        if (childDescription != null) {
            val element: DomElement = childDescription.addValue(goals)
            if (element is MavenDomGoal) {
                element.stringValue = "generate-rest-template-embedded"
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
        val dependencies = mutableListOf(
            MavenId("io.swagger", "swagger-annotations", ""),
            MavenId("org.openapitools", "jackson-databind-nullable", ""),
            MavenId("com.google.code.findbugs", "jsr305", "3.0.2"),
            MavenId("com.backbase.buildingblocks", "communication", "")
        )

        SpecUtils.addAdditionalDependencies(dependencies, project, pomFile, dataContext, title)
    }

    private fun addRestClientConfigClass(
        module: Module,
        project: Project,
        configurationClass: String,
        dialog: GenerateClientDialog
    ) {

        val mavenModel = MavenDomUtil
            .getMavenDomProjectModel(project, MavenTools.findProjectPom(project)!!)


        val directoryPath: String
        val psiDirectory: PsiDirectory
        if (project.name == module.name) {
            //Single project
            val packageName = SsdkUtils.cleanPackageName(mavenModel!!.groupId.value + "." + project.name.lowercase() + ".config")
            directoryPath = packageName.replace(".", File.separator)
            val directory = VfsUtil.createDirectories(
                project.basePath + File.separator + Paths.get(
                    "src",
                    "main",
                    "java"
                ) + File.separator + directoryPath
            )
            psiDirectory = PsiManager.getInstance(project).findDirectory(directory)!!
        } else {
            //Multimodule project
            val packageName = SsdkUtils.cleanPackageName(mavenModel!!.groupId.value + "." + module.name.lowercase() + ".config")
            directoryPath = packageName.replace(".", File.separator)
            val directory = VfsUtil.createDirectories(
                project.basePath + File.separator + module.name + File.separator + File.separator + Paths.get(
                    "src",
                    "main",
                    "java"
                ) + File.separator + directoryPath
            )
            psiDirectory = PsiManager.getInstance(project).findDirectory(directory)!!
        }
        val templateChangeLogPersistence =
            FileTemplateManager.getInstance(project).getTemplate("myServiceRestClientConfiguration")
        createFileFromTemplate(
            "$configurationClass-event",
            templateChangeLogPersistence,
            psiDirectory,
            HashMap<String, String>(),
            project,
            SpecUtils.createPropertiesForClientTemplate(dialog.serviceName, dialog.apiPackage)
        )
    }

    private fun createFileFromTemplate(
        name: String, template: FileTemplate, dir: PsiDirectory,
        templateValues: Map<String, String>, project: Project, properties: Properties
    ) {
        try {
            templateValues.forEach { (key, value) -> println("$key = $value") }
            FileTools.createFileFromTemplate(
                name, template, dir,
                "", true, templateValues, properties
            )
            Notification(
                "Backbase notification group", "Generating rest client config class", "Creating file $name-event",
                NotificationType.INFORMATION
            ).notify(project)
        } catch (e: Exception) {
            Notification(
                "Backbase notification group", "Generating rest client config class", "Event $name-event already exist",
                NotificationType.WARNING
            ).notify(project)
        }
    }
}