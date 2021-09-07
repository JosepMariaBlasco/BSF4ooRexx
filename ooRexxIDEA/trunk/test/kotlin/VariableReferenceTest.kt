import TestUtils.Companion.getAst
import TestUtils.Companion.printTree
import org.junit.Assert
import org.junit.Ignore
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class VariableReferenceTest {

    @Test
    fun testReference() {
        val ooRexxSource =
    """
    ::class A
    ::method b
    use arg >a, >b
    
    if a < b then
        say "hi"
    """
        val astNode = getAst(ooRexxSource)
        printTree(astNode)
    }


}