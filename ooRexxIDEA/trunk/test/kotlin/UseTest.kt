import TestUtils.Companion.getAst
import TestUtils.Companion.printTree
import org.junit.jupiter.api.Test

class UseTest {

    @Test
    fun testReference() {
        val ooRexxSource =
    """
    ::method init                 -- constructor
    use local allChars stream tmp len1  -- local variables (all others are attributes)
    use strict arg fileName
    """
        val astNode = getAst(ooRexxSource)
        printTree(astNode)
    }


}