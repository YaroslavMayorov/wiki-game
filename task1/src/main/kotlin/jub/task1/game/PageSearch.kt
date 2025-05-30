// Please, delete it after adding your implementation
@file:Suppress("UnusedPrivateMember")

package jub.task1.game

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

typealias PagePath = Pair<String, List<String>>
typealias PageQueue = ConcurrentLinkedQueue<PagePath>
typealias PathRef = AtomicReference<List<String>?>

// Stores the number of steps and a list of all links to the destination page
// The path must include the final page
data class SearchPath(
    val steps: Int,
    val path: List<String>,
)

data class SearchContext(
    val visited: MutableSet<String>,
    val foundPath: PathRef,
    val dispatcher: CoroutineDispatcher,
    val searchDepth: Int
)

class PageSearch(
    private val finalPage: String = KOTLIN_PAGE,

    ) {
    fun search(startPage: String, searchDepth: Int, threadsCount: Int): SearchPath = runBlocking {
        require(searchDepth > 0) { "Search depth must be > 0" }
        require(threadsCount > 0) { "Threads count must be > 0" }

        if (startPage == finalPage) return@runBlocking SearchPath(0, listOf(finalPage))

        val dispatcher = newFixedThreadPoolContext(threadsCount, "MyPool")
        val visitedPageUrls = ConcurrentHashMap.newKeySet<String>()
        val currentLevel = PageQueue()
        currentLevel.add(startPage to listOf(startPage))
        val nextLevel = PageQueue()
        val shortestUrlPathToTarget = PathRef(null)

        processLevels(
            currentLevel = currentLevel,
            nextLevel = nextLevel,
            context = SearchContext(visitedPageUrls, shortestUrlPathToTarget, dispatcher, searchDepth)
        )

        dispatcher.close()
        buildResult(shortestUrlPathToTarget)
    }

    private suspend fun processLevels(
        currentLevel: PageQueue,
        nextLevel: PageQueue,
        context: SearchContext
    ) {
        for (depth in 0 until context.searchDepth) {
            context.foundPath.get()?.let { return }
            processCurrentLevel(depth, currentLevel, nextLevel, context)
            currentLevel.clear()
            currentLevel.addAll(nextLevel)
            nextLevel.clear()
        }
    }

    private suspend fun processCurrentLevel(
        depth: Int,
        currentLevel: PageQueue,
        nextLevel: PageQueue,
        context: SearchContext
    ) = coroutineScope {
        for ((page, path) in currentLevel) {
            launch(context.dispatcher) {
                context.foundPath.get() ?: processPage(
                    page,
                    path,
                    depth,
                    nextLevel,
                    context
                )
            }
        }
    }

    private fun processPage(
        page: String,
        path: List<String>,
        depth: Int,
        nextLevel: PageQueue,
        context: SearchContext
    ) {
        val doc = getHtmlDocument(page)
        val links = extractReferences(doc)
        for (link in links) {
            context.foundPath.get()?.let {
                return
            }
            processLink(link, path, depth, nextLevel, context)
        }
    }

    private fun processLink(
        link: String,
        path: List<String>,
        depth: Int,
        nextLevel: PageQueue,
        context: SearchContext
    ) {
        if (!context.visited.add(link)) return

        val newPath = path + link

        if (link == finalPage) {
            context.foundPath.compareAndSet(null, newPath)
            return
        }

        if (depth + 1 < context.searchDepth) {
            nextLevel.add(link to newPath)
        }
    }

    private fun buildResult(foundPath: PathRef): SearchPath {
        val result = foundPath.get()
        return result?.let {
            SearchPath(result.size - 1, result)
        } ?: SearchPath(NOT_FOUND, emptyList())
    }

    companion object {
        const val KOTLIN_PAGE = "https://en.wikipedia.org/wiki/Kotlin_(programming_language)"
        const val NOT_FOUND = -1
    }
}

