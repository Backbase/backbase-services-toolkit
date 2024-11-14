package com.backbase.bst.common

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.search.GlobalSearchScope

object JavaTools {

    fun addAnnotationToJavaClass(project: Project, qualifiedJavaClassName: String,
                                         title: String,
                                         qualifiedNameAnnotations: List<String>) {
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val psiClass = javaPsiFacade.findClass(qualifiedJavaClassName, GlobalSearchScope.projectScope(project))
        val psiJavaFile = psiClass!!.containingFile as PsiJavaFile
        val classModifierList = psiClass.modifierList
        val annotations =  qualifiedNameAnnotations.filter { !classModifierList!!.hasAnnotation(it) }
        ReadonlyStatusHandler.ensureFilesWritable(project)
        val javaCodeStyleManager = JavaCodeStyleManager.getInstance(project)
        val codeStyleManager = CodeStyleManager.getInstance(project)
        if(annotations.isNotEmpty()) {
            WriteCommandAction.runWriteCommandAction(project) {
                annotations.map { annotationClass -> classModifierList!!.addAnnotation(annotationClass) }
                    .forEach {
                        javaCodeStyleManager.shortenClassReferences(it)
                    }
                javaCodeStyleManager.shortenClassReferences(psiJavaFile)
                codeStyleManager.reformat(psiJavaFile)
            }
            Notification("Backbase notification group", title, "Adding required annotation on Application class",
                NotificationType.INFORMATION).notify(project)
        } else {
            Notification("Backbase notification group", title, "Annotations were previously on Application class",
                NotificationType.WARNING).notify(project)
        }

    }
}