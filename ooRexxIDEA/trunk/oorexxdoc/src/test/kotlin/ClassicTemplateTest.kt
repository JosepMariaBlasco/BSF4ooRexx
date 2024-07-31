import java.io.File
import org.junit.jupiter.api.Test
import org.oorexx.doc.generate.Settings
import org.oorexx.doc.generate.StandaloneAstUtil
import org.oorexx.doc.generate.v2.Generator
import org.oorexx.doc.generate.v2.StandaloneGenerator
import org.oorexx.doc.generate.v2.TemplateRegistry
import org.oorexx.doc.input.model.OoRexxDocGenerationContext

class ClassicTemplateTest {

    @Test
    fun testClassicTemplate() {
        TemplateRegistry.enableJarLoading = false
        val generator = StandaloneGenerator()

        val ooRexxDocGenerationContext = OoRexxDocGenerationContext(
            listOf("./src/test/resources/ooRexxDoc-Example"),
            "./src/test/resources/ooRexxDoc-classic-output",
            mapOf("A" to "B"),
            "My Title",
            "<b>the overview</b>",
            Settings(),
            "classic"
        )

        System.setProperty("ooRexxDoc.templatesDir", "./src/main/resources/ooRexxDoc-templates")

        val templateBundle = TemplateRegistry.getTemplateByName("classic")

        generator.generate(ooRexxDocGenerationContext, templateBundle)
    }

}
