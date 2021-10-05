package com.backbase.backbaseservicestoolkit.actions

import com.intellij.ide.BrowserUtil
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase


internal class SearchGoldenSamplesActionTest : BasePlatformTestCase() {


    fun testActionGoingToBrowser(){

        val txt = "updateScrollParent"
        val psiFile = myFixture.configureByText("txt", txt)

        val action = SearchGoldenSamplesAction()

//        val e = AnActionEvent.createFromDataContext(ActionPlaces.EDITOR_GUTTER, null, );


        myFixture.editor.caretModel.currentCaret.setSelection(0,txt.length)
        val presentation = myFixture.testAction(action)
    }




}