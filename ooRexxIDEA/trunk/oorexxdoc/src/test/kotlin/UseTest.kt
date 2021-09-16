import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.getPsiFile
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
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }


}