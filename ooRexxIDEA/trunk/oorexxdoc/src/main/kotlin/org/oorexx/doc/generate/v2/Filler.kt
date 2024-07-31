package org.oorexx.doc.generate.v2

import freemarker.cache.FileTemplateLoader
import freemarker.cache.StringTemplateLoader
import freemarker.cache.TemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import java.io.File
import java.io.StringWriter
import org.oorexx.doc.analyse.model.OoRexxPackage
import org.oorexx.doc.input.model.OoRexxDocGenerationContext

class Filler {

    companion object {
        val fileMapping = mutableMapOf<String, StringWriter>()
    }

    fun fill(ooRexxDocGenerationContext: OoRexxDocGenerationContext,
             templateBundle: TemplateBundle,
             ooRexxPackages: List<OoRexxPackage>): MutableMap<String, StringWriter> {
        fileMapping.clear()

        val fillContext = FillContext(ooRexxDocGenerationContext, templateBundle, ooRexxPackages, null,
            loadTemplates(templateBundle))

        fill(
            fillContext,
            templateBundle.descriptor.mainTemplateFileName,
            templateBundle.descriptor.mainTargetFileName,
            null
        )

        return fileMapping
    }

    fun loadTemplates(templateBundle: TemplateBundle): Map<String, Template> {
        val baseDir = File(templateBundle.descriptor.baseDir)
        val configuration = getConfiguration(FileTemplateLoader(baseDir))

        return templateBundle.templateFiles.associate { it.file.name to configuration.getTemplate(it.file.path) }
    }

    private fun getConfiguration(templateLoader: TemplateLoader): Configuration {
        val configuration = Configuration(Configuration.VERSION_2_3_23)
        configuration.templateLoader = templateLoader

        configuration.defaultEncoding = "UTF-8"
        configuration.templateExceptionHandler = TemplateExceptionHandler.DEBUG_HANDLER

        return configuration
    }

    fun fill(fillContext: FillContext, templateName: String, fileName: String, currentElement: Any?) {
        val stringWriter = StringWriter()

        val data = mutableMapOf(
            "filler" to this,
            "fillContext" to FillContext(fillContext.ooRexxDocGenerationContext,
                fillContext.templateBundle,
                fillContext.ooRexxPackages,
                currentElement,
                fillContext.templateMapping)
        )

        fillContext.templateMapping[templateName]!!.process(data, stringWriter)

        fileMapping[fileName] = stringWriter
    }


    fun evaluate(eval: String) : String {
        try {
            val stringWriter = StringWriter()
            val templateLoader = StringTemplateLoader()
            val evalName = "eval"
            templateLoader.putTemplate(evalName, eval)
            getConfiguration(templateLoader).getTemplate(evalName).process(mapOf<String, String>(), stringWriter)

            return stringWriter.toString()
        } catch (e: Exception) {
            e.printStackTrace()

            return eval
        }
    }
}
