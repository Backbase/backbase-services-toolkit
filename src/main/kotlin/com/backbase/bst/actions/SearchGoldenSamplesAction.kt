package com.backbase.bst.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.util.io.URLUtil

class SearchGoldenSamplesAction  : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val ediTorRequiredData = e.getData(CommonDataKeys.EDITOR)
        val caretModel = ediTorRequiredData?.caretModel
        val selectedText = caretModel?.currentCaret?.selectedText

        val gitHubQuery = "https://github.com/Backbase/golden-sample-services/search?q=%s&type=code"
        val searchBy = String.format(
            gitHubQuery, URLUtil.encodeURIComponent(
                selectedText!!
            )
        )
        BrowserUtil.browse(searchBy)
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val caretModel = editor?.caretModel
        val selectedText = caretModel?.currentCaret?.selectedText
        if (selectedText == null || selectedText.isEmpty()) {
            e.presentation.isVisible = false
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}