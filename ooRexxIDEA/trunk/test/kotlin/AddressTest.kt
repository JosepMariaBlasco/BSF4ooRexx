import TestUtils.Companion.getAst
import TestUtils.Companion.printTree
import org.junit.jupiter.api.Test

class AddressTest {

    @Test
    fun environmentTest(){
        val ooRexxSource =
    """
    address "sh"
    """
        val astNode = getAst(ooRexxSource)
        printTree(astNode)

    }
}