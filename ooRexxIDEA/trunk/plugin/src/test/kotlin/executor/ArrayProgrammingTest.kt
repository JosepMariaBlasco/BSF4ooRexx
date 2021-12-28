package executor

import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.getPsiFile
import org.junit.jupiter.api.Test

class ArrayProgrammingTest {

    @Test
    fun ofTest() {
        val ooRexxSource =
            """
.array~new(2,3)~of(1,2)
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun ofRangeTest() {
        val ooRexxSource =
            """
.array~new(2,3)~of(1~2)
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun arrayTest() {
        val ooRexxSource =
            """
.array~new(2,3)
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun arrayOfClosureTest() {
        val ooRexxSource =
            """
.array~of { 1 * 1 }
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

}