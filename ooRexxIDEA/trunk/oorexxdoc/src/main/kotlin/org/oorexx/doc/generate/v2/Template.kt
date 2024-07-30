package org.oorexx.doc.generate.v2

import java.io.File

data class TemplateBundle (
    val descriptor: TemplateDescriptor,
    val templateFiles: List<TemplateFile>,
    val supportingFiles: List<SupportingFile>?,
)
data class TemplateDescriptor(val name: String,
                              val mainTargetFileName: String,
                              val baseDir: String?,
                              val mainTemplateFileName: String
    )
data class SupportingFile(val file: File)
data class TemplateFile(val file: File)
