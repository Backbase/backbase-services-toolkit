package com.backbase.bst.common.extensions

import com.backbase.bst.common.FileTools
import com.backbase.bst.common.MavenTools
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import org.jetbrains.idea.maven.dom.MavenDomUtil
import java.io.File
import java.nio.file.Paths
import java.util.*

object BehaviourExtensionsUtils {

    fun addClass(
        module: Module,
        project: Project,
        fileTemplateName: String,
        properties: Properties
    ) {

        val mavenModel = MavenDomUtil
            .getMavenDomProjectModel(project, MavenTools.findProjectPom(project)!!)


        val directoryPath: String
        val psiDirectory: PsiDirectory
        if (project.name == module.name) {
            //Single project
            val packageName = mavenModel!!.groupId.value.toString()
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
            val packageName = mavenModel!!.groupId.value.toString()
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
            FileTemplateManager.getInstance(project).getTemplate(fileTemplateName)
        createFileFromTemplate(
            "$fileTemplateName-hook",
            templateChangeLogPersistence,
            psiDirectory,
            HashMap<String, String>(),
            project,
            properties
        )
    }

    private fun createFileFromTemplate(
        name: String, template: FileTemplate, dir: PsiDirectory,
        templateValues: Map<String, String>, project: Project, properties: Properties
    ) {
        try {
            FileTools.createFileFromTemplate(
                name, template, dir,
                "", true, templateValues, properties
            )
            Notification(
                "Backbase notification group", "Generating hook class", "Creating file $name-hook",
                NotificationType.INFORMATION
            ).notify(project)
        } catch (e: Exception) {
            Notification(
                "Backbase notification group", "Generating hook class", "$name-hook already exist",
                NotificationType.WARNING
            ).notify(project)
        }
    }
}