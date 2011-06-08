signal on syntax

do i=1 to 3
   say i/p1(0)
end

exit

syntax:
  co=condition('Object')
  say ppCondition2(co, .true)
  exit -1

p1: procedure
  use arg number
  return p2(number)

p2: procedure
    parse arg number
    return r1(number)

::requires "rgf_util2.rex"
::requires UNO.CLS
::requires BSF.CLS

::routine r1
  use arg number
  return r2(number)

::routine r2
  parse arg number
  return 1/number       -- cause a division by 0 condition
