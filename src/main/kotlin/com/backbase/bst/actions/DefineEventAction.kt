package com.backbase.bst.actions

import com.backbase.bst.BackbaseBundle
import com.backbase.bst.common.MavenTools
import com.intellij.ide.actions.CreateFileAction.MkDirs
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.ui.GotItMessage
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.reflect.DomCollectionChildDescription
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.jetbrains.idea.maven.dom.MavenDomUtil
import org.jetbrains.idea.maven.dom.model.MavenDomConfiguration
import org.jetbrains.idea.maven.dom.model.MavenDomGoal
import org.jetbrains.idea.maven.dom.model.MavenDomPlugin
import org.jetbrains.idea.maven.dom.model.MavenDomPlugins
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.server.MavenServerManager
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil

class DefineEventAction : DumbAwareAction(){



    override fun actionPerformed(e: AnActionEvent) {

        val project = MavenActionUtil.getProject(e.dataContext) ?: return

        val persistenceDialog = DefineEventDialog(project)
        persistenceDialog.show()

        if (persistenceDialog.exitCode === DialogWrapper.CANCEL_EXIT_CODE) {
            return
        }


        val file: VirtualFile = MavenTools.findPomXml(e.dataContext) ?: return

        val eventArtifact = MavenId("com.backbase.buildingblocks", "events", "")

        if(!MavenTools.findDependencyOnBom(project, file, eventArtifact)) {
            addingMavenDependencies(project, file, e)
            Notification("Backbase notification group", "Define an Event", "Adding maven dependencies on pom.xml",
                NotificationType.INFORMATION).notify(project)
        }

        addEventSpec(e, project, persistenceDialog.eventName)

        val pluginId = MavenId("com.backbase.codegen", "jsonschema-events-maven-plugin", "")
        if(!MavenTools.findPluginOnBom(project, file, pluginId)) {
            addingMavenPlugin(project, file, e)
            Notification("Backbase notification group", "Define an Event", "Adding plugin jsonschema-events-maven-plugin on pom.xml",
                NotificationType.INFORMATION).notify(project)
        }

        val gotIt = GotItMessage.createMessage(
            BackbaseBundle.message("action.add.define.event.dialog.gotit.title"),
            BackbaseBundle.message("action.add.define.event.dialog.gotit.message"))

        gotIt.show(RelativePoint.getCenterOf(FileEditorManager.getInstance(project).selectedTextEditor!!.component), Balloon.Position.above)

    }

    private fun addingMavenDependencies(
        project: @Nullable Project,
        file: VirtualFile,
        e: AnActionEvent
    ) {
        if (!MavenServerManager.getInstance().isUseMaven2) {

            actionEventDependencies(project, file, e.dataContext)
        }
        val mavenProjectManager = MavenProjectsManager.getInstance(project)
        mavenProjectManager.forceUpdateProjects(mavenProjectManager.projects)

        mavenProjectManager.waitForPostImportTasksCompletion()
    }

    private fun addingMavenPlugin(
        project: @Nullable Project,
        file: VirtualFile,
        e: AnActionEvent
    ) {
        if (!MavenServerManager.getInstance().isUseMaven2) {

            actionEventPlugin(project, file, e.dataContext)
        }
        val mavenProjectManager = MavenProjectsManager.getInstance(project)
        mavenProjectManager.forceUpdateProjects(mavenProjectManager.projects)

        mavenProjectManager.waitForPostImportTasksCompletion()
    }

    private fun actionEventDependencies(project: Project, file: VirtualFile, dataContext: DataContext) {

        WriteCommandAction.runWriteCommandAction(project) {
                MavenTools.writeDependenciesOnPom(project, file, dataContext,
                    listOf(
                        MavenId("com.backbase.buildingblocks", "events", ""),

                    ))
        }
    }

    private fun actionEventPlugin(project: Project, file: VirtualFile, dataContext: DataContext) {
        val mavenModel = MavenDomUtil
            .getMavenDomProjectModel(project, file)


        WriteCommandAction.runWriteCommandAction(project) {
            createDomPlugin(mavenModel!!.build.plugins, project)

        }
    }


    private fun createFileFromTemplate(name: String, template: FileTemplate, dir: PsiDirectory,
                                       templateValues : Map<String, String>, project: Project) {
        try {
            CreateFileFromTemplateAction.createFileFromTemplate(name, template, dir,
                "", true, templateValues)
            Notification("Backbase notification group", "Define an Event", "Creating file $name-event",
                NotificationType.INFORMATION).notify(project)
        } catch (e: Exception) {
            Notification("Backbase notification group", "Define an Event", "Event $name-event already exist",
                NotificationType.WARNING).notify(project)
        }
    }

    private fun addEventSpec(e: AnActionEvent, project: Project, nameEvent: String) {
        val ideView = e.getData(LangDataKeys.IDE_VIEW)
        val psiDirectory = ideView!!.orChooseDirectory
        val mkdir = MkDirs("src/main/resources/events/tmp.xml", psiDirectory!!)
        val templateChangeLogPersistence =
            FileTemplateManager.getInstance(project).getTemplate("myresourcecreatedevent")
        createFileFromTemplate("$nameEvent-event", templateChangeLogPersistence, mkdir.directory, emptyMap(),
            project)


    }


    fun createDomPlugin(plugins: MavenDomPlugins?, project: Project): MavenDomPlugin? {
        val plugin = plugins!!.addPlugin()
        plugin!!.groupId.stringValue = "com.backbase.codegen"
        plugin.artifactId.stringValue = "jsonschema-events-maven-plugin"
        val execution = plugin.executions.addExecution()
        execution.phase.stringValue = "generate-sources"
        val goals = execution.goals

        val childDescription: DomCollectionChildDescription =
            goals.getGenericInfo().getCollectionChildDescription("goal")!!
        if (childDescription != null) {
            val element: DomElement = childDescription.addValue(goals)
            if (element is MavenDomGoal) {
                element.stringValue = "events-generation"
            }
        }


        val configuration = plugin.configuration
        configuration.ensureTagExists()


        val packageName =  project.name.toLowerCase()
        addElement(configuration, "inputFile", "\${project.basedir}/src/main/resources/events")
        addElement(configuration, "outputFile", "\${project.build.directory}/generated-sources/events")
        addElement(configuration, "basePackageName", "\${project.groupId}")
        addElement(configuration, "packageName", packageName)
        addElement(configuration, "packageVersion", "1")
        addElement(configuration, "useJavaTime", "true")
        addElement(configuration,
            "generatedResourcesDirectory",
            "\${project.build.directory}/generated-resources")
        addElement(
            configuration,
            "generatedSpringFactoriesDir",
            "\${project.build.directory}/generated-spring-factories")

        return plugin
    }

    private fun addElement(
        configuration: @NotNull MavenDomConfiguration,
        name: String,
        value: String
    ) {
        val createChildTag = configuration.xmlTag!!.createChildTag(name, "", value, false)
        configuration.xmlTag!!.addSubTag(createChildTag, false)
    }

    override fun update(e: AnActionEvent) {

        val file = MavenTools.findPomXml(e.dataContext)

        if(file == null) {
            e.presentation.isVisible = false
            return
        }
    }


}