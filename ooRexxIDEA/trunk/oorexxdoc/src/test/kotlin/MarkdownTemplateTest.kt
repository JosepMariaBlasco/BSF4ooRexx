import java.io.File
import org.junit.jupiter.api.Test
import org.oorexx.doc.generate.Settings
import org.oorexx.doc.generate.StandaloneAstUtil
import org.oorexx.doc.generate.v2.Filler
import org.oorexx.doc.generate.v2.Generator
import org.oorexx.doc.generate.v2.StandaloneGenerator
import org.oorexx.doc.generate.v2.SupportingFile
import org.oorexx.doc.generate.v2.TemplateBundle
import org.oorexx.doc.generate.v2.TemplateDescriptor
import org.oorexx.doc.generate.v2.TemplateFile
import org.oorexx.doc.generate.v2.TemplateRegistry
import org.oorexx.doc.input.model.OoRexxDocGenerationContext

class MarkdownTemplateTest {

    @Test
    fun testMarkdownTemplate() {
         val generator = StandaloneGenerator()

        val ooRexxDocGenerationContext = OoRexxDocGenerationContext(
            listOf("./src/test/resources/ooRexxDoc-Example"),
            "./src/test/resources/ooRexxDoc-markdown-output",
            mapOf("A" to "B",
                "date" to "\${.now?datetime}"),
            "My Title",
            "<b>the overview</b>",
            Settings(),
            "markdown"
        )

        System.setProperty("ooRexxDoc.templatesDir", "./src/main/resources/ooRexxDoc-templates")

        val markdown = TemplateRegistry.getTemplateNames().first { it == "markdown" }

        val templateBundle = TemplateRegistry.getTemplateByName(markdown)

        generator.generate(ooRexxDocGenerationContext, templateBundle)
    }

}
