import com.intellij.lang.ASTNode
import org.intellij.grammar.LightPsi
import org.oorexx.ide.parser.standalone.OoRexxStandaloneParserDefinition

class TestUtils {
    companion object {
        fun printTree(astNode: ASTNode, level: Int = 0) {
            for (child in astNode.getChildren(null)) {
                println("\t".repeat(level)+child.toString())
                if (child.getChildren(null).isNotEmpty()) {
                    printTree(child, level = level + 1)
                }
            }
        }

        fun getAst(ooRexxSource: String) = LightPsi.parseText(ooRexxSource.trim(), OoRexxStandaloneParserDefinition())
    }
}