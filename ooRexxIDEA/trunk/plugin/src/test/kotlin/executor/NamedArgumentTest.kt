package executor

import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.getPsiFile
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.oorexx.ide.util.ExecutorParserUtil

class NamedArgumentTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            ExecutorParserUtil.active = true
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            ExecutorParserUtil.active = false
        }
    }

    @Test
    fun testNamedArgument() {
        val ooRexxSource =
            """
put(nam2e:"one")

    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testNamedArgumentTwo() {
        val ooRexxSource =
            """
put(name:"one", uuio:1)

    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testArgument() {
        val ooRexxSource =
            """
put("one", 1)

    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testUse() {
        val ooRexxSource =
            """
use strict auto named arg by, initial=.nil
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }
}