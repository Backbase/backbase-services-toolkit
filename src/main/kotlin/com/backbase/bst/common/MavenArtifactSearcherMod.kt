package com.backbase.bst.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.WaitFor
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.concurrency.Promise
import org.jetbrains.idea.maven.indices.MavenArtifactSearchResult
import org.jetbrains.idea.maven.indices.MavenSearcher
import org.jetbrains.idea.maven.onlinecompletion.MavenCompletionProviderFactory
import org.jetbrains.idea.maven.onlinecompletion.model.MavenRepositoryArtifactInfo
import org.jetbrains.idea.reposearch.DependencySearchService
import org.jetbrains.idea.reposearch.RepositoryArtifactData
import org.jetbrains.idea.reposearch.SearchParameters
import java.util.function.Consumer

class MavenArtifactSearcherMod : MavenSearcher<MavenArtifactSearchResult>() {
    override fun searchImpl(project: Project, pattern: String, maxResult: Int): List<MavenArtifactSearchResult> {
        if (StringUtil.isEmpty(pattern)) {
            return emptyList()
        }
        val searchResults: MutableList<MavenRepositoryArtifactInfo> = ArrayList()
        val searchService = DependencySearchService.getInstance(project)

        val providers = MavenCompletionProviderFactory().getProviders(project)

        searchService.setProviders(providers, providers)

        val asyncPromise = searchService.fulltextSearch(pattern, SearchParameters(false, false),
            Consumer { mdci: RepositoryArtifactData? ->
                if (mdci is MavenRepositoryArtifactInfo) {
                    searchResults.add(mdci)
                }
            })
        object : WaitFor(1000) {
            override fun condition(): Boolean {
                return asyncPromise.state != Promise.State.PENDING
            }
        }
        return processResults(searchResults)
    }

    companion object {
        private fun processResults(searchResults: List<MavenRepositoryArtifactInfo>): List<MavenArtifactSearchResult> {
            return ContainerUtil.map(
                searchResults
            ) { info: MavenRepositoryArtifactInfo? ->
                MavenArtifactSearchResult(
                    info!!
                )
            }
        }
    }
}
