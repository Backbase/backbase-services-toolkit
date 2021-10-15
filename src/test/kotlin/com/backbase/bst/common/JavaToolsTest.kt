package com.backbase.bst.common

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiManager
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
internal class JavaToolsTest : BasePlatformTestCase() {

    fun testAddingJavaAnnotation() {
        val virtualFile = myFixture.copyFileToProject("com/backbase/test2w/Application.java", "/com/backbase/test2w/Application.java")

        WriteCommandAction.runWriteCommandAction(project) {
            JavaTools.addAnnotationToJavaClass(project, "com.backbase.test2w.Application", listOf("org.springframework.boot.autoconfigure.domain.EntityScan"))
        }

        val text = PsiManager.getInstance(project).findFile(virtualFile)!!.text

        TestCase.assertTrue(text.contains("EntityScan"))

    }

    override fun getTestDataPath() = "src/test/testData/addingJavaAnnotation"

}