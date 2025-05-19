import jub.task1.game.extractReferences
import jub.task1.game.getHtmlDocument
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ParserTests {

    companion object {
        @JvmStatic
        fun htmlPages() = listOf(
            Arguments.of(
                "https://en.wikipedia.org/wiki/Kotlin_(programming_language)aaaaa",
                null,
            ),
            Arguments.of(
                "en.wikipedia.org/wiki/Kotlin_(programming_language)aaaaa",
                null,
            ),
            Arguments.of(
                "https://en.wikipedia.org/wiki/Kotlin_(programming_language)",
                listOf(
                    "while the Kotlin Foundation protects the Kotlin trademark",
                    "On 7 May 2019, Google announced that the Kotlin programming language had become its preferred language",
                    "The Android Kotlin compiler emits Java 8",
                )
            ),
            Arguments.of(
                "en.wikipedia.org/wiki/Kotlin_(programming_language)",
                listOf(
                    "Kotlin has support for the web with Kotlin/JS",
                    "Kotlin 1.5 was released in May 2021",
                    "Andrey Breslav, Kotlin's former lead designer"
                )
            )
        )

        @JvmStatic
        fun referencesData() = listOf(
            Arguments.of(
                "https://en.wikipedia.org/wiki/Kotlin_(programming_language)aaaaa",
                emptyList<String>(),
                0,
            ),
        )
    }

    @ParameterizedTest
    @MethodSource("htmlPages")
    fun getHtmlDocumentTest(url: String, output: List<String>?) {
        val html = getHtmlDocument(url)
        output?.let {
            assertNotNull(
                html,
                "For the valid url: $url the parser must return not null! User can omit https://, but you need to handle this case!"
            )
            output.forEach {
                assertTrue(it in html.toString(), "For the valid url: $url the text $output must be in the output")
            }
        } ?: assertNull(html, "For the invalid url: $url the parser must return null!")
    }

    @ParameterizedTest
    @MethodSource("referencesData")
    fun referencesExtractorTest(url: String, expectedReferences: List<String>, referencesNumber: Int) {
        val html = getHtmlDocument(url)
        val references = extractReferences(html)
        assertTrue(
            references.size == referencesNumber,
            "For thw url: $url you need to extract $referencesNumber references."
        )
        expectedReferences.forEach {
            assertTrue(it in references, "The reference: $it must be found by the $url url.")
        }
    }
}
