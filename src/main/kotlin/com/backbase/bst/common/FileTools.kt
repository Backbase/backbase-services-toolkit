package com.backbase.bst.common

import com.intellij.ide.actions.CreateFileAction
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.ide.fileTemplates.actions.CreateFromTemplateActionBase
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.util.IncorrectOperationException
import org.apache.velocity.runtime.parser.ParseException
import org.jetbrains.idea.maven.utils.MavenLog
import java.util.*

object FileTools {

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
            val pointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiFile)
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
                return pointer.element
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
}