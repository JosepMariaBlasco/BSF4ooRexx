import TestUtils.Companion.getAst
import TestUtils.Companion.printTree
import org.junit.Ignore
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
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