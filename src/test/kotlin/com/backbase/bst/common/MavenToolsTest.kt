package com.backbase.bst.common

import com.intellij.ide.DataManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil
import junit.framework.TestCase
import org.jetbrains.annotations.Nullable
import org.jetbrains.idea.maven.model.MavenId


@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MavenToolsTest : BasePlatformTestCase()  {


    fun testAddDependencyToPom(){
        val virtualFile = myFixture.copyFileToProject("pom.xml", "pom.xml")

        val dataContext = DataManager.getInstance().dataContext

        WriteCommandAction.runWriteCommandAction(project) {
            MavenTools.writeDependenciesOnPom(
                project, virtualFile, dataContext,
                listOf(
                    MavenId("com.backbase", "test", "1.0.0")
                )
            )
        }

        val psiPom = PsiManager.getInstance(project).findFile(virtualFile)

        assertPom(psiPom)

    }

    private fun XmlTag.assertArtifact(artifactId: String, groupId:String, version: String? = null, scope: String? = null) {
        val currentGroupId = this.findFirstSubTag("groupId")!!.value.trimmedText
        val currentArtifactId = this.findFirstSubTag("artifactId")!!.value.trimmedText

        TestCase.assertEquals(groupId, currentGroupId)
        TestCase.assertEquals(artifactId, currentArtifactId)

        if (!version.isNullOrEmpty()) {
            val currentVersion = this.findFirstSubTag("version")!!.value.trimmedText
            TestCase.assertEquals(version, currentVersion)
        }
        if (!scope.isNullOrEmpty()) {
            val currentScope = this.findFirstSubTag("scope")!!.value.trimmedText
            TestCase.assertEquals(scope, currentScope)
        }
    }

    private fun assertPom(psiPom: @Nullable PsiFile?) {
        val xmlFile = assertInstanceOf(psiPom, XmlFile::class.java)

        assertFalse(PsiErrorElementUtil.hasErrors(project, xmlFile.virtualFile))

        val rootTag = xmlFile.rootTag!!

        rootTag.assertArtifact("thisisfun", "com.backbase", "1.0.0")

        rootTag.findFirstSubTag("parent")!!
            .assertArtifact("service-sdk-starter-core", "com.backbase.buildingblocks", "17.0.0")

        rootTag.findFirstSubTag("dependencies")!!.findFirstSubTag("dependency")!!
            .assertArtifact("test", "com.backbase")
    }

    override fun getTestDataPath() = "src/test/testData/addDependencyToPom"

}