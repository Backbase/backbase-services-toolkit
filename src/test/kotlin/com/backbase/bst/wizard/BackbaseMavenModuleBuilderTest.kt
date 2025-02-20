package com.backbase.bst.wizard

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil
import junit.framework.TestCase
import org.jetbrains.annotations.Nullable
import org.jetbrains.idea.maven.model.MavenId
import org.junit.Ignore
import java.io.File

@Ignore
/*
Test ignored temporarily, as it throws "com.intellij.openapi.util.TraceableDisposable$DisposalException: Editor EditorImplEditor EditorImpl[file:///private/var/folders/d8/jn_7qn4n1gv30gct2svbrcfh0000gp/T/unitTest_createProject_2nVdvm8HV6nAnqQMzPCmZAlxLcH/pom.xml] hasn't been released:
hasn't been released:" Exception.
It is a known issue in intellij plugin, there are multiple issues related to our issue
https://youtrack.jetbrains.com/issue/CWM-9356/com.intellij.openapi.util.TraceableDisposableDisposalException-Editor-is-already-disposed
https://youtrack.jetbrains.com/issue/IJPL-71075/com.intellij.openapi.util.TraceableDisposableDisposalException-Editor-EditorImplnull-hasnt-been-released-at-Down-and-Edit

Some Clues: We should verify the pom file creation or availability to the Editor( com.intellij.openapi.editor.impl.EditorImpl)
 */
internal class BackbaseMavenModuleBuilderTest : BasePlatformTestCase() {

    private val GROUP_ID = "com.backbase"
    private val VERSION= "1.0.1"


    fun testCreateProject() {
        val myProjectId = MavenId(GROUP_ID,project.name,VERSION)
        val ssdkMavenId = MavenId("com.backbase.buildingblocks", "service-sdk-starter-core", "17.0.0")
        val builder = BackbaseMavenModuleBuilder(myProjectId, ssdkMavenId)
        val root: VirtualFile? = createAndGetContentEntry()

        builder.configure(project, root!!, false)

        val pom = root.findChild("pom.xml")

        TestCase.assertNotNull("pom.xml is expected", pom!!)

        val psiPom = PsiManager.getInstance(project).findFile(pom)

        assertPom(psiPom)

       val applicationJavaFile = root.findChild("src")!!.findChild("main")!!
            .findChild("java")!!
            .findChild("com")!!.findChild("backbase")!!
            .findChild(project.name.lowercase())!!.findChild("Application.java")!!

        TestCase.assertNotNull(applicationJavaFile)
    }

    private fun assertPom(psiPom: @Nullable PsiFile?) {
        val xmlFile = assertInstanceOf(psiPom, XmlFile::class.java)

        assertFalse(PsiErrorElementUtil.hasErrors(project, xmlFile.virtualFile))

        val rootTag = xmlFile.rootTag!!

        rootTag.assertArtifact(project.name, GROUP_ID, VERSION)

        rootTag.findFirstSubTag("parent")!!
            .assertArtifact("service-sdk-starter-core", "com.backbase.buildingblocks", "17.0.0")

        rootTag.findFirstSubTag("dependencies")!!.findFirstSubTag("dependency")!!
            .assertArtifact("service-sdk-starter-test", "com.backbase.buildingblocks", null, "test")
    }

    private fun XmlTag.assertArtifact( artifactId: String,  groupId:String,  version: String? = null, scope: String? = null) {
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

    private fun createAndGetContentEntry(): VirtualFile? {
        val path = FileUtil.toSystemIndependentName(project.basePath!!)
        File(path).mkdirs()
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
    }

}