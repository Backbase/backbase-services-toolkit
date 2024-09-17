package com.backbase.bst.wizard.extensions

import com.backbase.bst.BackbaseBundle
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.idea.maven.dom.MavenDomUtil
import org.jetbrains.idea.maven.dom.model.MavenDomParent
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel
import org.jetbrains.idea.maven.model.MavenConstants
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.project.MavenProjectBundle
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenLog
import org.jetbrains.idea.maven.utils.MavenUtil
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.*


class BackbaseExtensionProjectBuilder(
    private val behaviourExtensionsProjectWizard: BehaviourExtensionsProjectWizard,
    private val myProjectId: MavenId,
) {

    private val newProjectCommandName = BackbaseBundle.message("wizard.behaviour.extension.project.command.name")

    fun configure(project: Project, root: VirtualFile, isInteractive: Boolean) {
        val psiFiles = PsiFile.EMPTY_ARRAY
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
            val packageName = myProjectId.groupId
            val directoryPath = packageName?.replace(".", File.separator)
            VfsUtil.createDirectories(
                project.basePath + File.separator + Paths.get(
                    "src",
                    "main",
                    "java"
                ) + File.separator + directoryPath
            )
            VfsUtil.createDirectories(
                project.basePath + File.separator + Paths.get(
                    "src",
                    "main",
                    "resources"
                )
            )
            VfsUtil.createDirectories(
                project.basePath + File.separator + Paths.get(
                    "src",
                    "test",
                    "java"
                )
            )
        } catch (e: IOException) {
            MavenLog.LOG.info(e)
        }

        MavenProjectsManager.getInstance(project).forceUpdateAllProjectsOrFindAllAvailablePomFiles()

        // execute when current dialog is closed (e.g. Project Structure)
        MavenUtil.invokeLater(project) {


        }

    }


    private fun showError(project: Project, e: Throwable) {
        MavenUtil.showError(project, MavenProjectBundle.message("notification.title.failed.to.create.maven.project"), e)
    }

    private fun updateProjectPom(project: Project, pom: VirtualFile) {
        WriteCommandAction.writeCommandAction(project).withName(newProjectCommandName).run<RuntimeException> {
            PsiDocumentManager.getInstance(project).commitAllDocuments()
            val model = MavenDomUtil.getMavenDomProjectModel(project, pom) ?: return@run
            model.name.stringValue = "Backbase ::${project.name.replace("-", " ")}"
            updateMavenParent(model)

            //Add BB service dependency
            var dep = model.dependencies.addDependency()
            dep.groupId.stringValue = behaviourExtensionsProjectWizard.serviceGroupId
            dep.artifactId.stringValue = behaviourExtensionsProjectWizard.serviceArtifactId
            dep.classifier.stringValue = "classes"
            dep.scope.stringValue = "provided"

            // Add Test dependency
            dep = model.dependencies.addDependency()
            dep.groupId.stringValue = "com.backbase.buildingblocks"
            dep.artifactId.stringValue = "service-sdk-starter-test"
            dep.scope.stringValue = "test"

            //Add Backbase Bom dependency
            dep = model.dependencyManagement.dependencies.addDependency()
            dep.groupId.stringValue = "com.backbase"
            dep.artifactId.stringValue = "backbase-bom"
            dep.version.stringValue = behaviourExtensionsProjectWizard.bbVersion
            dep.type.stringValue = "pom"
            dep.scope.stringValue = "import"

            //Add properties
            var tag = model.properties.xmlTag?.createChildTag(
                "docker.image.name", "",
                "harbor.backbase.eu/development/\${project.artifactId}", true
            )
            model.properties.xmlTag?.addSubTag(tag, false)

            tag = model.properties.xmlTag?.createChildTag(
                "docker.image.tag", "",
                "\${project.version}", true
            )
            model.properties.xmlTag?.addSubTag(tag, false)

            tag = model.properties.xmlTag?.createChildTag(
                "docker.base.tag", "",
                behaviourExtensionsProjectWizard.bbVersion, true
            )
            model.properties.xmlTag?.addSubTag(tag, false)

            tag = model.properties.xmlTag?.createChildTag(
                "docker.base.name", "",
                "repo.backbase.com/backbase-docker-releases/" + behaviourExtensionsProjectWizard.serviceArtifactId,
                true
            )
            model.properties.xmlTag?.addSubTag(tag, false)

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

    private fun updateMavenParent(mavenModel: MavenDomProjectModel): MavenDomParent {
        val result = mavenModel.mavenParent
        result.groupId.stringValue = "com.backbase.buildingblocks"
        result.artifactId.stringValue = "backbase-service-extension-starter-parent"
        result.version.stringValue = behaviourExtensionsProjectWizard.ssdkVersion
        result.relativePath.ensureTagExists()

        return result
    }

    private fun getPsiFile(project: Project, pom: VirtualFile): PsiFile? {
        return PsiManager.getInstance(project).findFile(pom)
    }

}