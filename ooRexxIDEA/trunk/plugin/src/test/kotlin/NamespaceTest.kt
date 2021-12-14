import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.getPsiFile
import org.junit.jupiter.api.Test


class NamespaceTest {

    @Test
    fun namespaceForArrayMessageTermTest() {
        val ooRexxSource =
            """
o=rexx:array~new -- REXX: ooRexx-Klasse
say "o~class:" o~class "| o~items:" o~items

::requires 'Listing5a.rex' namespace ix
"""
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

}