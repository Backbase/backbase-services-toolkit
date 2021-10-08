package com.backbase.bst.actions

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