/* Routines that issue a command to the environment BSFADDCHC (a Java
   ConditionRaisingCommandHandler) and say what came back: the condition
   name, "SYNTAX" and its code, or "none".  With ::OPTIONS ERROR SYNTAX.
   Used by CommandHandlerConditions.testGroup. */

::routine es.signalOn public
  use arg cmd
  signal on user mine name got
  signal on notready name got
  signal on error name got
  signal on syntax name syn
  address BSFADDCHC cmd
  return "none"
got: return condition("C") "rc="rc
syn: return "SYNTAX" condition("O")~code

::routine es.callOn public
  use arg cmd
  res = "none"
  signal on syntax name syn
  call on user mine name got
  call on notready name got
  address BSFADDCHC cmd
  return res "rc="rc
got: res = condition("C"); return
syn: return "SYNTAX" condition("O")~code

::routine es.untrapped public
  use arg cmd
  signal on syntax name syn
  address BSFADDCHC cmd
  return "none rc="rc
syn: return "SYNTAX" condition("O")~code
::options error syntax
