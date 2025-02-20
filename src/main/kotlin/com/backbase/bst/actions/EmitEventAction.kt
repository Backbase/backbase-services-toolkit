package com.backbase.bst.actions

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.psi.PsiJavaFile

class EmitEventAction  : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project

        val template = TemplateSettings.getInstance().getTemplate("emitevent", "backbase")

        val editor = e.getData(CommonDataKeys.EDITOR)

        if (editor != null) {
            TemplateManager.getInstance(project).startTemplate(editor, template!!)
        }

    }

    override fun update(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)

        if (psiFile is PsiJavaFile) {

            val classes = psiFile.classes
            if(!classes.flatMap { it.annotations.toList() }.any { it.qualifiedName == "org.springframework.stereotype.Service" }) {
                e.presentation.isVisible = false
            }
        } else {
            e.presentation.isVisible = false
        }


    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}