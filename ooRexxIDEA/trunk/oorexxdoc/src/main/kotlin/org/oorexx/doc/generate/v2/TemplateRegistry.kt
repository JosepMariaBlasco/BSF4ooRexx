package org.oorexx.doc.generate.v2

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.io.File
import java.lang.reflect.Type
import java.util.Collections.emptyList
import kotlin.io.path.absolutePathString

class TemplateRegistry {
    companion object {
        const val defaultTemplateName: String = "classic"
        var enableJarLoading: Boolean = true
        private val loadedTemplates = mutableListOf<TemplateBundle>()

        fun getTemplates() : List<TemplateBundle> {
            if(loadedTemplates.isEmpty()) {
                init()
            }

            return loadedTemplates
        }

        private fun init() {
            loadedTemplates.clear()

            val templates = mutableSetOf<TemplateBundle>()

            if(enableJarLoading) {
                loadDefaultFromJar(templates)
            }

            loadFromPath(templates, System.getenv("OOREXXDOC_TEMPLATE_HOME"))
            loadFromPath(templates, System.getProperty("user.home"), "ooRexxDoc/templates")
            loadFromPath(templates, System.getProperty("ooRexxDoc.templatesDir"))

            if (templates.isEmpty()) {
                throw RuntimeException("Template registry cannot be empty. There are no templates available.")
            }

            for (template in templates) {
                if(loadedTemplates.none {it.descriptor.name == template.descriptor.name}) {
                    loadedTemplates.add(template)
                }
            }
        }

        private fun loadFromPath(templates: MutableSet<TemplateBundle>, path: String?, child: String = "") {
            if(path == null) {
                return
            }

            val file = if(child.isBlank()) {
                File(path)
            } else {
                File(path, child)
            }

            templates.addAll(loadFolder(file))
        }

        fun getTemplateNames() : List<String> {
            val templates = getTemplates()
            return templates.map { it.descriptor.name }
        }

        fun getTemplateByName(name: String) : TemplateBundle {
            val templates = getTemplates()
            return templates.first { it.descriptor.name == name }
        }

        private fun loadFolder(file: File): List<TemplateBundle> {
            if(!file.exists() || !file.isDirectory) {
                return emptyList()
            }

            return file.listFiles()!!.filter { it.isDirectory }.map { loadTemplateBundle(it) }.filterNotNull()
        }

        private fun loadDefaultFromJar(templateFiles: MutableSet<TemplateBundle>)  {
            val path = JarUtil.copyFromJarToTmp("ooRexxDoc-templates")

            loadFromPath(templateFiles, path.absolutePathString())
        }

        private fun loadTemplateBundle(file: File): TemplateBundle? {
            val templateJsonName = "template.json"

            val templateJson = file.listFiles()?.firstOrNull { it != null &&  it.isFile && it.name == templateJsonName } ?: return null

            val templateBundle = GsonBuilder().registerTypeAdapter(File::class.java, FileDeserializer())
                .create().fromJson(templateJson.readText(), TemplateBundle::class.java)

            val updatedDescriptor = templateBundle.descriptor.copy(baseDir = file.absolutePath)

            return templateBundle.copy(descriptor = updatedDescriptor)
        }
    }
}

class FileDeserializer : JsonDeserializer<File> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): File {
        return File(json.asString)
    }
}
