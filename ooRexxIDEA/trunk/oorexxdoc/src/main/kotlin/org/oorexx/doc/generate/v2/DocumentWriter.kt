package org.oorexx.doc.generate.v2

import java.io.File
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.oorexx.doc.input.model.OoRexxDocGenerationContext

class DocumentWriter {
    fun write(ooRexxDocGenerationContext: OoRexxDocGenerationContext,
              templateBundle: TemplateBundle,
              fillWriters: MutableMap<String, StringWriter>) {
        fillWriters.keys.forEach {
            writeFile(DocumentWriterContext(ooRexxDocGenerationContext, templateBundle, fillWriters, it))
        }

        copySupportingFiles(templateBundle, ooRexxDocGenerationContext)
    }

    private fun copySupportingFiles(
        templateBundle: TemplateBundle,
        ooRexxDocGenerationContext: OoRexxDocGenerationContext
    ) {
        templateBundle.supportingFiles?.forEach {
            copyFileOrDirectory(
                File(File(templateBundle.descriptor.baseDir), it.file.path),
                File(File(ooRexxDocGenerationContext.output), it.file.path)
            )
        }
    }

    private fun writeFile(documentWriterContext: DocumentWriterContext) {
        val file = File(File(documentWriterContext.ooRexxDocGenerationContext.output), documentWriterContext.currentFileName)

        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        if (!file.exists()) {
            file.createNewFile()
        }

        file.writeText(documentWriterContext.fileWriters[documentWriterContext.currentFileName].toString())
    }

    private fun copyFileOrDirectory(source: File, destination: File) {
            if (source.isDirectory) {
                if (!destination.exists()) {
                    destination.mkdirs()
                }

                source.listFiles()?.forEach { file ->
                    copyFileOrDirectory(file, File(destination, file.name))
                }
            } else {
                Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
    }

    data class DocumentWriterContext(
            val ooRexxDocGenerationContext: OoRexxDocGenerationContext,
            val templateBundle: TemplateBundle,
            val fileWriters: Map<String, StringWriter>,
            val currentFileName: String
        ) {}
}
