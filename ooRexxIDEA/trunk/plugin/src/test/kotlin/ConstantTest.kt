import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.getPsiFile
import org.junit.jupiter.api.Test


class ConstantTest {

    @Test
    fun constantsWithNumberAsName() {
        val ooRexxSource =
            """
::constant 0 48
::constant 1 49
::constant 2 50
::constant 3 51
::constant 4 52
::constant 5 53
::constant 6 54
::constant 7 55
::constant 8 56
::constant 9 57
"""
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun constantsExampleRef() {
        val ooRexxSource =
            """
::class MathConstants public
::constant pi 3.14159265
::constant author "Isaac Asimov"
::constant absolute_zero -273.15
::constant e ( rxcalcexp(1) )
::constant eSquare (self~e ** 2)
::constant primes (2, 3, 5, 7, 11, 13)
::class Search
::constant caseless
::requires rxmath library
"""//        https://sourceforge.net/projects/oorexx/files/oorexx-docs/5.0.0beta/rexxref
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun constantsExample2Ref() {
        val ooRexxSource =
            """
say "Pi is" .MathConstants~pi -- displays "Pi is 3.14159265"
instance = .MathConstants~new
say "Pi is" instance~pi -- also displays "Pi is 3.14159265"
say .Search~caseless -- "CASELESS"
::class MathConstants public
::constant pi 3.14159265
::class Search
::constant caseless
"""//        https://sourceforge.net/projects/oorexx/files/oorexx-docs/5.0.0beta/rexxref
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

}