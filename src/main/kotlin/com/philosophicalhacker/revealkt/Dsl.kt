package com.philosophicalhacker.revealkt

import java.io.File

sealed class Slide
class ContentSlide : Slide() {
    var text: String? = null
    var image: String? = null

    override fun toString() = """
            <section data-markdown ${image?.let { "data-background-image=$it" }}>
                <textarea data-template>
                    ${text?.let { it } ?: ""}
                </textarea>
            </section>
    """.trimMargin()
}

class CodeSlide : Slide() {
    var language: String? = null
    private var source: Source? = null

    override fun toString() = """
        <section>
            <pre><code ${language?.let { "class=$it" } ?: ""} data-trim data-noescape>
                |||$source
            </code></pre>
        </section>
    """.trimMargin("|||")

    fun source(raw: String, highlightLine: Int? = null) {
        source = Source(raw, highlightLine)
    }

    class Source(private val raw: String, private val highlightLine: Int?) {
        override fun toString() = highlightLine?.let {
            val lines = raw.lines().toMutableList()
            lines[highlightLine] = "<mark>${lines[highlightLine].trimMargin()}</mark>"
            return lines.joinToString("\n")
        } ?: raw
    }
}

class Presentation {
    val slides = mutableListOf<Slide>()

    fun slide(init: ContentSlide.() -> Unit): Unit {
        val slide = ContentSlide()
        init(slide)
        slides.add(slide)
    }

    fun code(init: CodeSlide.() -> Unit): Unit {
        val slide = CodeSlide()
        init(slide)
        slides.add(slide)
    }

    override fun toString() = slides
            .map(Slide::toString)
            .reduce { acc, s -> acc + s }
}

fun presentation(init: Presentation.() -> Unit) {
    val presentation = Presentation()
    init(presentation)
    val outputString = getHtmlPrefix() + presentation + getHtmlSuffix()
    val presentationDir = File("presentation")
    if (!presentationDir.exists()) {
        val exec = Runtime.getRuntime().exec("git clone https://github.com/hakimel/reveal.js.git presentation")
        println(exec.errorStream.bufferedReader().readText())
    }
    val file = File("presentation", "index.html")
    file.writeText(outputString)
}

private fun getHtmlSuffix() = getResourceString("suffix.html")

private fun getHtmlPrefix() = getResourceString("prefix.html")

private fun getResourceString(name: String) = Presentation::class.java
        .classLoader
        .getResourceAsStream(name)
        .bufferedReader()
        .readText()


fun main(args: Array<String>) {
    presentation {
        slide {
            text = "# Stop Yak Shaving"
        }
        slide {
            image = "https://media.licdn.com/mpr/mpr/p/4/005/0a6/1ce/04eede4.jpg"
        }
        slide {
            text = "Create tech presentations with a DSL like this..."
        }
        code {
            language = "kotlin"
            source("""
                presentation {
                    slide {
                        text = "# Stop Yak Shaving"
                    }
                }
            """)
        }
        slide { text = "Make slides that step through code easily like this..." }
        val stepThroughCode = """
            val stepThroughCode = \"\"\"
                code {
                    language = "kotlin"
                    source(\"\"\"
                        println ("Stop Yak Shaving")
                    \"\"\")
                }
            \"\"\"
            code {
                language = "kotlin"
                source(stepThroughCode, 0)
            }
            code {
                language = "kotlin"
                source(stepThroughCode, 2)
            }
            code {
                language = "kotlin"
                source(stepThroughCode, 3)
            }
        """.trimMargin()
        code {
            language = "kotlin"
            source(stepThroughCode, 0)
        }
        code {
            language = "kotlin"
            source(stepThroughCode, 2)
        }
    }
}