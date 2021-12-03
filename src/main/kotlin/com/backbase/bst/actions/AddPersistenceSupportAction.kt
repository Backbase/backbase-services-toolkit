package com.backbase.bst.actions

import com.backbase.bst.common.JavaTools
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
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.server.MavenServerManager
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil

class AddPersistenceSupportAction : DumbAwareAction(){

    private val title = "Adding Persistence Support."

    override fun actionPerformed(e: AnActionEvent) {

        val project = MavenActionUtil.getProject(e.dataContext) ?: return

        val persistenceDialog = PersistenceDialog(project)
        persistenceDialog.show()

        if (persistenceDialog.exitCode === DialogWrapper.CANCEL_EXIT_CODE) {
            return
        }


        if(persistenceDialog.addPomDependencies) {
            val file: VirtualFile = MavenTools.findPomXml(e.dataContext) ?: return

            if (!MavenServerManager.getInstance().isUseMaven2) {
                actionAddPersistenceDependencies(project, file, e.dataContext)
            }
            val mavenProjectManager = MavenProjectsManager.getInstance(project)
            mavenProjectManager.forceUpdateProjects(mavenProjectManager.projects)

            mavenProjectManager.waitForImportFinishCompletion()

        }

        if(persistenceDialog.addLiquibaseFile){
            addLiquibaseFile(e, project)
        }

        if(persistenceDialog.addApplicationClassPersistenceSupport){
            JavaTools.addAnnotationToJavaClass(e.project!!,
                "com.backbase." + project.name.toLowerCase() + ".Application",
                title,
                listOf(
                    "org.springframework.data.jpa.repository.config.EnableJpaRepositories",
                    "org.springframework.boot.autoconfigure.domain.EntityScan"
                )
            )

        }
    }

    private fun actionAddPersistenceDependencies(project: Project, file: VirtualFile, dataContext: DataContext) {
        val dependencies = listOf(
            MavenId("com.backbase.buildingblocks", "persistence", ""),
            MavenId("com.backbase.buildingblocks", "service-sdk-starter-mapping", ""),
            MavenId("org.springframework.boot", "spring-boot-starter-cache", "")
        ).filter {
            !MavenTools.findDependencyOnBom(project, file, it)
        }

        if(dependencies.isNotEmpty()) {
            WriteCommandAction.runWriteCommandAction(project) {
                MavenTools.writeDependenciesOnPom(
                    project, file, dataContext,
                    dependencies
                )
            }

            Notification(
                "Backbase notification group", title, "Adding Maven dependencies on pom.xml",
                NotificationType.INFORMATION
            ).notify(project);
        } else {
            Notification(
                "Backbase notification group", title, "Maven dependencies were previously on pom.xml",
                NotificationType.WARNING
            ).notify(project);
        }
    }


    private fun createFileFromTemplate(name: String, template: FileTemplate, dir: PsiDirectory, project: Project) {
        try {
            CreateFileFromTemplateAction.createFileFromTemplate(name, template, dir, "", false)
        } catch (e: Exception){
            Notification(
                "Backbase notification group", title, "The file $name already exist",
                NotificationType.WARNING
            ).notify(project);
        }
    }

    private fun addLiquibaseFile(e: AnActionEvent, project: Project) {
        val ideView = e.getData(LangDataKeys.IDE_VIEW)
        val psiDirectory = ideView!!.orChooseDirectory
        val mkdir = MkDirs("src/main/resources/db/changelog/tmp.xml", psiDirectory!!)
        val templateChangeLogPersistence =
            FileTemplateManager.getInstance(project).getTemplate("changelogpersistenceexample")
        createFileFromTemplate("db.changelog-persistence", templateChangeLogPersistence, mkdir.directory, project)
        val templateChangeLog = FileTemplateManager.getInstance(project).getTemplate("changelogexample")
        createFileFromTemplate("db.changelog-1.0.0", templateChangeLog, mkdir.directory, project)
    }

    override fun update(e: AnActionEvent) {

        val file = MavenTools.findPomXml(e.dataContext)

        if(file == null) {
            e.presentation.isVisible = false
            return;
        }
    }


}