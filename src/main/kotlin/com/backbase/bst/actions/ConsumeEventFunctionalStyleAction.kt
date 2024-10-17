package com.backbase.bst.actions

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.psi.PsiJavaFile

class ConsumeEventFunctionalStyleAction  : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project

        val template = TemplateSettings.getInstance().getTemplate("consumer", "backbase")

        val editor = e.getRequiredData(CommonDataKeys.EDITOR)

        TemplateManager.getInstance(project).startTemplate(editor, template!!)

    }

    override fun update(e: AnActionEvent) {
        val psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE)

        if (psiFile !is PsiJavaFile) e.presentation.isVisible = false
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}