package executor

import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.getPsiFile
import org.junit.jupiter.api.Test

class SymmetricBinaryOperatorsTest {

    @Test
    fun testExample1() {
        val ooRexxSource =
            """
arg1 ~ "+"( arg2 )

    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample2() {
        val ooRexxSource =
            """
arg2 ~ "+OP:RIGHT"( arg1 )

    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample3() {
        val ooRexxSource =
            """
a = .array~of(10,20,30)
100 a                 -- ['100 10','100 20','100 30'] instead of '100 an Array'
a 100                  -- ['10 100','20 100','30 100']
100 || a              -- [10010,10020,10030]          instead of '100an Array'
a || 100              -- [10100,20100,30100]
100 - a                -- [90, 80, 70]
(100+200i) * a         -- [1000+2000i, 2000+4000i, 3000+6000i]

    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

}