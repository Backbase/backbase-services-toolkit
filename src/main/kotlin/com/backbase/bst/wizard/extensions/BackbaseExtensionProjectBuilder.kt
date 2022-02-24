package com.backbase.bst.wizard.extensions

import com.backbase.bst.BackbaseBundle
import com.intellij.ide.actions.CreateFileAction
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.ide.fileTemplates.actions.CreateFromTemplateActionBase
import com.intellij.ide.util.EditorHelper
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.IncorrectOperationException
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.reflect.DomCollectionChildDescription
import org.apache.velocity.runtime.parser.ParseException
import org.jetbrains.idea.maven.dom.MavenDomUtil
import org.jetbrains.idea.maven.dom.model.*
import org.jetbrains.idea.maven.model.MavenConstants
import org.jetbrains.idea.maven.model.MavenCoordinate
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.project.MavenProjectBundle
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenLog
import org.jetbrains.idea.maven.utils.MavenUtil
import java.io.IOException
import java.util.*


class BackbaseExtensionProjectBuilder(val projectWizard: BehaviourExtensionsProjectWizard, val myProjectId: MavenId) {

    private val newProjectCommandName = BackbaseBundle.message("wizard.behaviour.extension.project.command.name")

    fun configure(project: Project, root: VirtualFile, isInteractive: Boolean) {
        val psiFiles =  PsiFile.EMPTY_ARRAY
        val pom = WriteCommandAction.writeCommandAction(project, *psiFiles).withName(newProjectCommandName)
            .compute<VirtualFile, RuntimeException> {
                var file: VirtualFile? = null
                try {
                    file = root.findChild(MavenConstants.POM_XML)
                    file?.delete(this)
                    file = root.createChildData(this, MavenConstants.POM_XML)
                    MavenUtil.runOrApplyMavenProjectFileTemplate(project, file, myProjectId, isInteractive)
                } catch (e: IOException) {
                    showError(project, e)
                    return@compute file
                }
                updateProjectPom(project, file)
                file
            } ?: return

        val manager = MavenProjectsManager.getInstance(project)
        manager.addManagedFilesOrUnignore(listOf(pom))
        try {
            VfsUtil.createDirectories(root.path + "/src/main/java")
            VfsUtil.createDirectories(root.path + "/src/main/resources")
            VfsUtil.createDirectories(root.path + "/src/test/java")

            //Create java main file
            val projectName = project.name.toLowerCase().replace("-", "")
            val packageName = myProjectId.groupId + "." + projectName
            val directoryPath = packageName.replace(".", "/")
            val directory = VfsUtil.createDirectories(root.path + "/src/main/java/" + directoryPath)

            WriteCommandAction.runWriteCommandAction(project) {

                val templateChangeLogPersistence = FileTemplateManager.getInstance(project).getTemplate("application")
                val psiDirectory = PsiManager.getInstance(project).findDirectory(directory)
                val fileTemplateManager = FileTemplateManager.getInstance(project)
                val properties = fileTemplateManager.defaultProperties
                properties.setProperty("PACKAGE_NAME", packageName)
                createFileFromTemplate("Application", templateChangeLogPersistence, psiDirectory!!, properties)

                addApplicationYaml(project, projectName, root)
            }
        } catch (e: IOException) {
            MavenLog.LOG.info(e)
        }

        MavenProjectsManager.getInstance(project).forceUpdateAllProjectsOrFindAllAvailablePomFiles()

        // execute when current dialog is closed (e.g. Project Structure)
        MavenUtil.invokeLater(project, ModalityState.NON_MODAL) {
            if (!pom.isValid) {
                showError(project, RuntimeException("Project is not valid"))
                return@invokeLater
            }
            EditorHelper.openInEditor(getPsiFile(project, pom)!!)

        }

    }

    private fun addApplicationYaml(
        project: Project,
        applicationName: String,
        root: VirtualFile,
    ) {
        val directory = VfsUtil.createDirectories(root.path + "/src/main/resources/")
        val template = FileTemplateManager.getInstance(project).getTemplate("applicationprop")
        val psiDirectory = PsiManager.getInstance(project).findDirectory(directory)
        val fileTemplateManager = FileTemplateManager.getInstance(project)
        val properties = fileTemplateManager.defaultProperties
        properties.setProperty("APPLICATION_NAME", applicationName)
        createFileFromTemplate("application", template, psiDirectory!!, properties)
    }

    private fun showError(project: Project, e: Throwable) {
        MavenUtil.showError(project, MavenProjectBundle.message("notification.title.failed.to.create.maven.project"), e)
    }

    private fun updateProjectPom(project: Project, pom: VirtualFile) {
        WriteCommandAction.writeCommandAction(project).withName(newProjectCommandName).run<RuntimeException> {
            PsiDocumentManager.getInstance(project).commitAllDocuments()
            val model = MavenDomUtil.getMavenDomProjectModel(project, pom) ?: return@run
            model.name.stringValue = "Backbase ::${project.name.replace("-", " ")}"
            var parentId : MavenId = MavenId("com.backbase.buildingblocks", "backbase-service-extension-starter-parent", projectWizard.ssdkVersion)
            updateMavenParent(model, parentId)
            val dep1: MavenCoordinate = MavenId("com.backbase.buildingblocks", "service-sdk-starter-test", "")
            val dep = MavenDomUtil.createDomDependency(
                model.dependencies,
                EditorHelper.openInEditor(getPsiFile(project, pom)!!)
            )
            dep.groupId.setStringValue(dep1.groupId)
            dep.artifactId.setStringValue(dep1.artifactId)
            dep.scope.setValue("test")

            //createDomPlugin(model.build.plugins, project)

            CodeStyleManager.getInstance(project)
                .reformat(getPsiFile(project, pom)!!)
            val pomFiles: MutableList<VirtualFile> = ArrayList(2)
            pomFiles.add(pom)
            unblockAndSaveDocuments(project, *pomFiles.toTypedArray())
        }
    }

    private fun unblockAndSaveDocuments(project: Project, vararg files: VirtualFile) {
        val fileDocumentManager = FileDocumentManager.getInstance()
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        for (file in files) {
            val document = fileDocumentManager.getDocument(file) ?: continue
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document)
            fileDocumentManager.saveDocument(document)
        }
    }
    fun updateMavenParent(mavenModel: MavenDomProjectModel, parentId: MavenId): MavenDomParent? {
        val result = mavenModel.mavenParent
        result.groupId.stringValue = parentId.groupId
        result.artifactId.stringValue = parentId.artifactId
        result.version.stringValue = parentId.version
        result.relativePath.ensureTagExists()
        return result
    }

    private fun getPsiFile(project: Project, pom: VirtualFile): PsiFile? {
        return PsiManager.getInstance(project).findFile(pom)
    }

    private fun createFileFromTemplate(name: String, template: FileTemplate, dir: PsiDirectory, properties: Properties) {
        createFileFromTemplate(
            name,
            template,
            dir,
            "",
            false,
            HashMap<String, String>(),
            properties
        )
    }

    fun createFileFromTemplate(
        name: String?,
        template: FileTemplate,
        dir: PsiDirectory,
        defaultTemplateProperty: String?,
        openFile: Boolean,
        liveTemplateDefaultValues: Map<String, String>,
        properties: Properties?): PsiFile? {
        var name = name
        var dir = dir
        if (name != null) {
            val mkdirs = CreateFileAction.MkDirs(name, dir)
            name = mkdirs.newName
            dir = mkdirs.directory
        }
        val project = dir.project
        try {
            val psiFile = FileTemplateUtil
                .createFromTemplate(template, name, properties, dir)
                .containingFile

            val virtualFile = psiFile.virtualFile
            if (virtualFile != null) {
                if (openFile) {
                    if (template.isLiveTemplateEnabled) {
                        CreateFromTemplateActionBase.startLiveTemplate(psiFile, liveTemplateDefaultValues)
                    } else {
                        FileEditorManager.getInstance(project).openFile(virtualFile, true)
                    }
                }
                if (defaultTemplateProperty != null) {
                    PropertiesComponent.getInstance(project).setValue(defaultTemplateProperty, template.name)
                }
                return psiFile
            }
        } catch (e: ParseException) {
            throw IncorrectOperationException("Error parsing Velocity template: " + e.message, e as Throwable)
        } catch (e: IncorrectOperationException) {
            throw e
        } catch (e: Exception) {
            MavenLog.LOG.info(e)
        }
        return null
    }

    fun createDomPlugin(plugins: MavenDomPlugins?, project: Project): MavenDomPlugin? {
        val plugin = plugins!!.addPlugin()
        plugin!!.groupId.stringValue = "org.springframework.boot"
        plugin.artifactId.stringValue = "spring-boot-maven-plugin"
        val execution = plugin.executions.addExecution()
        val goals = execution.goals

        val childDescription: DomCollectionChildDescription =
            goals.genericInfo.getCollectionChildDescription("goal")!!

        val element: DomElement = childDescription.addValue(goals)
        if (element is MavenDomGoal) {
            element.stringValue = "build-info"
        }

        return plugin
    }

}