import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.assertPsiError
import TestUtils.Companion.getPsiFile
import org.junit.jupiter.api.Test

class AddressTest {

    @Test
    fun environmentTest(){
        val ooRexxSource =
    """
    address "sh"
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }
}