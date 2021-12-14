import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.getPsiFile
import org.junit.jupiter.api.Test


class SelectTest {

    @Test
    fun selectRefExample1() {
        val ooRexxSource =
            """
balance=100
check=50
balance = balance - check
Select
 when balance > 0 then
 say "Congratulations! You still have" balance "dollars left."
 when balance = 0 then do
 say "Warning, Balance is now zero! STOP all spending."
 say "You cut it close this month! Hope you do not have any"
 say "checks left outstanding."
 end
 Otherwise do
 say "You have just overdrawn your account."
 say "Your balance now shows" balance "dollars."
 say "Oops! Hope the bank does not close your account."
 end
end /* Select */
"""//        https://sourceforge.net/projects/oorexx/files/oorexx-docs/5.0.0beta/rexxref
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun selectRefExample2() {

        val ooRexxSource =
            """
select
 when answer~datatype('w'), answer//2 = 0 Then
 say answer "is even"
 when answer~datatype('w'), answer//2 = 1 Then
 say answer "is odd"
 otherwise
 say answer "is not a number"
end
"""//        https://sourceforge.net/projects/oorexx/files/oorexx-docs/5.0.0beta/rexxref
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun selectRefExample3() {
        val ooRexxSource =
            """
select
 when answer~datatype('w') & answer//2 = 0 Then
 say answer "is even"
 when answer~datatype('w') & answer//2 = 1 Then
 say answer "is odd"
 otherwise
 say answer "is not a number"
end
"""//        https://sourceforge.net/projects/oorexx/files/oorexx-docs/5.0.0beta/rexxref
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun selectCaseTest() {
        val ooRexxSource =
            """
  select case(type)
    -- day of the month from 1 to 31, without leading zeros
    when "d" then do
      haveDate = .true
      self~day = self~getVariableNumber(2, 1, 31)
    end
    -- day of the month from 1 to 31, with leading zeros
    when "dd" then do
      haveDate = .true
      self~day = self~getFixedNumber(2, 1, 31)
    end
    -- day of the year from 1 to 366, without leading zeros
    when "ddd" then do
      haveDate = .true
      self~ordinal = self~getVariableNumber(3, 1, 366)
    end
    -- day of the year from 1 to 366, with leading zeros
    when "dddd" then do
      haveDate = .true
      self~ordinal = self~getFixedNumber(3, 1, 366)
    end
    -- day of the month from 1 to 31, without leading zeros and an ordinal suffix
    when "ddddd" then do
      haveDate = .true
      day = self~getVariableNumber(2, 1, 31)
      self~day = day
      if \self~checkMatch(self~formats~ordinalSuffixes[day]) then do
        self~parsingError
      end
    end
    -- day of the week as a number
    when "D" then do
      self~weekDay = self~getFixedNumber(1, 1, 7)
    end
    -- day of the week as an abbreviated name
    when "DD" then do
      -- the parser provides the list of weekday abbreviations, which can be customized
      self~weekDay = self~checkList(self~formats~dayAbbreviations)
    end
    -- day of the week as a full namename
    when "DDD" then do
      -- the parser provides the list of weekday names, which can be customized
      self~weekDay = self~checkList(self~formats~dayNames)
    end

    otherwise do
      self~templateError(type)
    end
  end
"""//      dateparser.cls
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }


}