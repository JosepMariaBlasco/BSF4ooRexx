import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.getPsiFile
import org.junit.jupiter.api.Test


class ForwardTest {

    @Test
    fun forwardMessageTest() {
        val ooRexxSource =
            """
::ROUTINE clr.import PUBLIC
  USE strict ARG typeName, rexxName4local=.nil
 say space("    text with   extra  spaces ")

  if .CLRLogger~doLogging then    -- if not OFF, produce information
     .CLRLogger~trace(pp.line(.line) "CLR.CLS/clr.import(), typeName="pp(typeName)", rexxName4local="pp(rexxName4local))

  return .clr~clr.import(typeName, rexxName4local)

"""
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun forwardClassTest() {
        val ooRexxSource =
            """
        ::CLASS 'RegularExpression' PUBLIC
        
        ::METHOD New CLASS
          Signal On Syntax
          Forward Class (Super)
        
        Syntax:
          Raise Propagate
        
        /* Library name RXREGEXP must be in lower case for UNIX */
        ::METHOD INIT EXTERNAL "LIBRARY rxregexp RegExp_Init"
        ::METHOD UNINIT EXTERNAL "LIBRARY rxregexp RegExp_Uninit"
        ::METHOD PARSE EXTERNAL "LIBRARY rxregexp RegExp_Parse"
        ::METHOD MATCH EXTERNAL "LIBRARY rxregexp RegExp_Match"
        ::METHOD POS EXTERNAL "LIBRARY rxregexp RegExp_Pos"
        
        ::METHOD POSITION
          expose !POS
          use strict arg -- no args allowed
          return !POS
"""
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

}