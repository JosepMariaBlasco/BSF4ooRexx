import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.getPsiFile
import org.junit.jupiter.api.Test

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
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }


}