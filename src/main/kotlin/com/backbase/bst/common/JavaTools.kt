package com.backbase.bst.common

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
                                         qualifiedNameAnnotations: List<String>) {
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val psiClass = javaPsiFacade.findClass(qualifiedJavaClassName, GlobalSearchScope.projectScope(project))
        val psiJavaFile = psiClass!!.containingFile as PsiJavaFile
        val classModifierList = psiClass.modifierList
        ReadonlyStatusHandler.getInstance(project).ensureFilesWritable()
        val javaCodeStyleManager = JavaCodeStyleManager.getInstance(project)
        val codeStyleManager = CodeStyleManager.getInstance(project)
        WriteCommandAction.runWriteCommandAction(project) {
            qualifiedNameAnnotations.map{annotationClass -> classModifierList!!.addAnnotation(annotationClass)}
                .forEach{
                   javaCodeStyleManager.shortenClassReferences(it)
                }
            javaCodeStyleManager.shortenClassReferences(psiJavaFile)
            codeStyleManager.reformat(psiJavaFile)
        }
    }
}