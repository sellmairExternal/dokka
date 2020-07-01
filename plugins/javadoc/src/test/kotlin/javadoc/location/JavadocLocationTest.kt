package javadoc.location

import javadoc.pages.JavadocClasslikePageNode
import javadoc.pages.JavadocPackagePageNode
import javadoc.renderer.JavadocContentToHtmlTranslator
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.ExternalDocumentationLinkImpl
import org.jetbrains.dokka.javadoc.JavadocPlugin
import org.jetbrains.dokka.model.firstChildOfType
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.testApi.testRunner.AbstractCoreTest
import org.junit.jupiter.api.Test

class JavadocTest : AbstractCoreTest() {

    private fun locationTestInline(testHandler: (RootPageNode, DokkaContext) -> Unit) {
        fun externalLink(link: String) = DokkaConfiguration.ExternalDocumentationLink
            .Builder(link)
            .build() as ExternalDocumentationLinkImpl

        val config = dokkaConfiguration {
            format = "javadoc"
            passes {
                pass {
                    sourceRoots = listOf("jvmSrc/")
                    externalDocumentationLinks = listOf(
                        externalLink("https://docs.oracle.com/javase/8/docs/api/"),
                        externalLink("https://kotlinlang.org/api/latest/jvm/stdlib/")
                    )
                    analysisPlatform = "jvm"
                }
            }
        }
        testInline(
            """
            |/jvmSrc/javadoc/Test.kt
            |package javadoc
            |class Test() : Serializable, Cloneable {
            |   fun test() {}
            |   fun test2(s: String) {}
            |   fun <T> test3(t: T) {}
            |}
        """.trimIndent(),
            config,
            cleanupOutput = false,
            pluginOverrides = listOf(JavadocPlugin())
        ) { renderingStage = testHandler }
    }

    @Test
    fun `resolved signature with external links`() {

        locationTestInline { rootPageNode, dokkaContext ->
            val transformer = htmlTranslator(rootPageNode, dokkaContext)
            val testClass = rootPageNode.firstChildOfType<JavadocPackagePageNode>()
                .firstChildOfType<JavadocClasslikePageNode>()
            assert(
                "<a href=https://docs.oracle.com/javase/8/docs/api/java/lang/Cloneable.html>java.lang.Cloneable</a>"
                        == transformer.htmlForContentNode(testClass.signature.supertypes!!, null)
            )
        }
    }

    @Test
    fun `resolved signature to no argument function`() {

        locationTestInline { rootPageNode, dokkaContext ->
            val transformer = htmlTranslator(rootPageNode, dokkaContext)
            val testClassNode = rootPageNode.firstChildOfType<JavadocPackagePageNode>()
                .firstChildOfType<JavadocClasslikePageNode>()
            val testFunctionNode = testClassNode.methods.first()
            assert(
                """<a href=Test.html#test-->test</a>()"""
                        == transformer.htmlForContentNode(
                    testFunctionNode.signature.signatureWithoutModifiers,
                    testClassNode
                )
            )
        }
    }

    @Test
    fun `resolved signature to one argument function`() {

        locationTestInline { rootPageNode, dokkaContext ->
            val transformer = htmlTranslator(rootPageNode, dokkaContext)
            val testClassNode = rootPageNode.firstChildOfType<JavadocPackagePageNode>()
                .firstChildOfType<JavadocClasslikePageNode>()
            val testFunctionNode = testClassNode.methods[1]
            assert(
                """<a href=Test.html#test2-kotlin.String->test2</a>(<a href=https://docs.oracle.com/javase/8/docs/api/java/lang/String.html>java.lang.String</a> <a href=.html>s</a>)"""
                        == transformer.htmlForContentNode(
                    testFunctionNode.signature.signatureWithoutModifiers,
                    testClassNode
                )
            )
        }
    }

    @Test
    fun `resolved signature to generic function`() {

        locationTestInline { rootPageNode, dokkaContext ->
            val transformer = htmlTranslator(rootPageNode, dokkaContext)
            val testClassNode = rootPageNode.firstChildOfType<JavadocPackagePageNode>()
                .firstChildOfType<JavadocClasslikePageNode>()
            val testFunctionNode = testClassNode.methods[2]
            assert(
                """<a href=Test.html#test3-T->test3</a>&lt;T&gt;(<a href=.html>T</a> <a href=.html>t</a>)"""
                        == transformer.htmlForContentNode(
                    testFunctionNode.signature.signatureWithoutModifiers,
                    testClassNode
                )
            )
        }
    }

    private fun htmlTranslator(rootPageNode: RootPageNode, dokkaContext: DokkaContext) = JavadocContentToHtmlTranslator(
        dokkaContext.plugin<JavadocPlugin>().querySingle { locationProviderFactory }
            .getLocationProvider(rootPageNode),
        dokkaContext
    )
}