package com.backbase.bst.actions

import com.intellij.testFramework.fixtures.BasePlatformTestCase


internal class SearchGoldenSamplesActionTest : BasePlatformTestCase() {


    fun testActionGoingToBrowser(){

        val txt = "updateScrollParent"
        myFixture.configureByText("txt", txt)

        val action = SearchGoldenSamplesAction()

        myFixture.editor.caretModel.currentCaret.setSelection(0,txt.length)
        myFixture.testAction(action)
    }




}