package jub.task1.game

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException

private val forbiddenTemplates = listOf(
    "File:",
    "Wikipedia:",
    "Help:",
    "Template:",
    "Category:",
    "Special:",
    "Portal:",
    "User:",
    "MediaWiki:",
    "Draft",
    "TimedText",
    "Module:",
    "Media:",
    "Template_talk:",
    "Talk:",
    "Main_Page"
)

@Suppress("TooGenericExceptionCaught", "SwallowedException")
fun getHtmlDocument(url: String): Document? {
    // You can use Jsoup.connect to get a document
    // See - https://jsoup.org/cookbook/input/load-document-from-url
    return try {
        val checkedUrl = if (!url.contains("https://")) "https://$url" else url
        Jsoup.connect(checkedUrl).get()
    } catch (e: IOException) {
        null
    }
}

fun extractReferences(html: Document?): List<String> {
    // You can use html.select to find the references
    // See the link to find usage examples: https://www.tabnine.com/code/java/methods/org.jsoup.nodes.Element/select
    // Don't forget to create the full link, e.g. /wiki/JetBrains --> https://en.wikipedia.org/wiki/JetBrains

    val links: Elements = html?.select("[href^=/wiki/]") ?: return listOf()
    val host = "https://en.wikipedia.org"
    return links
        .map { host + it.attr("href") }
        .filter { link -> forbiddenTemplates.all { forbiddenTemplate -> !link.contains(forbiddenTemplate) } }
        .distinct()
}
