import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.grammar.LightPsi
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.oorexx.ide.parser.standalone.OoRexxStandaloneParserDefinition


class TestUtils {
    companion object {
        fun printTree(psiElement: PsiElement, level: Int = 0) {
            if(level == 0){
                println("\t".repeat(level)+psiElement.toString())
            }
            for (child in psiElement.children) {
                println("\t".repeat(level)+child.toString())
                if (child.children.isNotEmpty()) {
                    printTree(child, level = level + 1)
                }
            }
        }

        fun getPsiFile(ooRexxSource: String) = LightPsi.parseFile("temporary.rexx", ooRexxSource, OoRexxStandaloneParserDefinition())!!

        fun assertNoPsiError(psiFile: PsiFile) {
            printTree(psiFile)

            val error = PsiTreeUtil.findChildOfType(psiFile, PsiErrorElement::class.java)

            assertNull(error)
        }

        fun assertPsiError(psiFile: PsiFile) {
            printTree(psiFile)

            val error = PsiTreeUtil.findChildOfType(psiFile, PsiErrorElement::class.java)

            assertNotNull(error)
        }
    }
}