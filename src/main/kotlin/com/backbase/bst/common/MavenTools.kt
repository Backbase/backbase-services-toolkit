package com.backbase.bst.common

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.idea.maven.dom.MavenDomUtil
import org.jetbrains.idea.maven.dom.model.MavenDomDependencies
import org.jetbrains.idea.maven.dom.model.MavenDomDependency
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel
import org.jetbrains.idea.maven.indices.MavenArtifactSearchResult
import org.jetbrains.idea.maven.model.MavenCoordinate
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.utils.MavenUtil
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil

object MavenTools {

    fun createDomDependency(
        dependencies: MavenDomDependencies?,
        editor: Editor?,
        id: MavenCoordinate
    ): MavenDomDependency? {
        val dep = MavenDomUtil.createDomDependency(dependencies!!, editor)
        dep.groupId.stringValue = id.groupId
        dep.artifactId.stringValue = id.artifactId
        return dep
    }

    fun writeDependenciesOnPom(project: Project, file: VirtualFile, dataContext: DataContext, dependencies: List<MavenId>) {
        val mavenModel = MavenDomUtil
            .getMavenDomProjectModel(project, file)
        val editor = dataContext.getData(CommonDataKeys.EDITOR)
        dependencies.forEach{dependency -> createDomDependency(mavenModel!!.dependencies, editor, dependency)}
    }

    fun findDependencyOnBom(project: Project, file: VirtualFile, dependency: MavenId): Boolean {
        val mavenModel = MavenDomUtil
            .getMavenDomModel(project, file, MavenDomProjectModel::class.java)
        return MavenDomUtil.findProject(mavenModel!!)!!.dependencies.filter{
            dependency.groupId  == it.groupId && dependency.artifactId == it.artifactId
        }.any()
    }


    fun findPluginOnBom(project: Project, file: VirtualFile, plugin: MavenId): Boolean {
        val mavenModel = MavenDomUtil
            .getMavenDomProjectModel(project, file)
        return MavenDomUtil.findProject(mavenModel!!)!!.plugins.filter{
            plugin.groupId  == it.groupId && plugin.artifactId == it.artifactId
        }.any()
    }

    fun findPomXml(dataContext: DataContext): VirtualFile? {
        var file: VirtualFile? = CommonDataKeys.VIRTUAL_FILE.getData(dataContext) ?: return null
        if (file!!.isDirectory) {
            file = MavenUtil.streamPomFiles(MavenActionUtil.getProject(dataContext), file).findFirst().orElse(null)
            if (file == null) return null
        }
        val manager = MavenActionUtil.getProjectsManager(dataContext) ?: return null
        manager.findProject(file) ?: return null
        return file
    }

    fun findVersionsArtifact(project: Project?, groupId: String, artifactId: String) : MavenArtifactSearchResult {
        val searcher = MavenArtifactSearcherMod()

        val result = searcher.search(project, "$groupId:$artifactId:", 1000)

        return result.filter { it.searchResults.artifactId =="service-sdk-starter-core" }.first()
    }

}