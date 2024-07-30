package org.oorexx.doc.generate.v2

import com.intellij.openapi.application.ApplicationManager
import java.util.Locale
import java.util.function.Function
import java.util.function.Predicate
import org.oorexx.doc.analyse.CommentContext
import org.oorexx.doc.analyse.PackageAnalyzer
import org.oorexx.doc.analyse.model.OoRexxPackage
import org.oorexx.doc.generate.AstUtil
import org.oorexx.doc.generate.AstUtilBase
import org.oorexx.doc.generate.AstWrapper
import org.oorexx.doc.generate.StandaloneAstUtil
import org.oorexx.doc.input.model.OoRexxDocGenerationContext
import org.oorexx.doc.util.IdGenerator
import org.oorexx.ide.file.OoRexxFile

class StandaloneGenerator : Generator(StandaloneAstUtil())
class IntegratedGenerator : Generator(AstUtil())

open class Generator(val astUtilBase: AstUtilBase) {

    fun generate(ooRexxDocGenerationContext: OoRexxDocGenerationContext,
                 templateBundle: TemplateBundle) {
        IdGenerator.reset()

        println("using $templateBundle")

        val filler = Filler()

        val ooRexxPackages = getOoRexxPackages(ooRexxDocGenerationContext)

        val fillWriters = filler.fill(ooRexxDocGenerationContext, templateBundle, ooRexxPackages)

        val docWriter = DocumentWriter()

        docWriter.write(ooRexxDocGenerationContext, templateBundle, fillWriters)
    }

    fun getOoRexxPackages(ooRexxDocGenerationContext: OoRexxDocGenerationContext): List<OoRexxPackage> {
        return astUtilBase
            .getAbstractSyntaxTrees(ooRexxDocGenerationContext.getInputs())
            .parallelStream()
            .map<OoRexxPackage>(Function<AstWrapper, OoRexxPackage?> { psiFile: AstWrapper ->
                val ooRexxPackage =
                    arrayOfNulls<OoRexxPackage>(1)
                ApplicationManager.getApplication().runReadAction {
                    val psi = psiFile.psiFile.node.psi
                    val commentContext =
                        CommentContext(psi as OoRexxFile)
                    ooRexxPackage[0] =
                        PackageAnalyzer(ooRexxDocGenerationContext.getSettings(), commentContext)
                            .analyze(psiFile.file, psi as OoRexxFile)
                }
                ooRexxPackage[0]
            })
            .filter(Predicate<OoRexxPackage> { obj: OoRexxPackage -> obj.hasElements() })
            .sorted(Comparator.comparing<OoRexxPackage, String> { o: OoRexxPackage ->
                o.name.lowercase(Locale.getDefault())
            })
            .toList()
    }
}
