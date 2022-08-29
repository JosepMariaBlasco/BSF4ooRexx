/* This Rexx program needs to be called by another program that
   preconfigures the Rexx interpreter to contain two command
   handlers, one for the environment "ONE" the other for "DEUX" */
line = "="~copies(79)
say line
say "current environment:" pp(address())
say

"hello"
say "answer to command" pp(sourceline(.line-1)) "was:" pp(rc) ".rs="pp(.rs)
say

"ping"
say "answer to command" pp(sourceline(.line-1)) "was:" pp(rc) ".rs="pp(.rs)
say

"this is an unknown command"
say "answer to command" pp(sourceline(.line-1)) "was:" pp(rc) ".rs="pp(.rs)
say

address one info
say "answer to command" pp(sourceline(.line-1)) "was:" pp(rc) ".rs="pp(.rs)
say line

address deux "hello"
say "answer to command" pp(sourceline(.line-1)) "was:" pp(rc) ".rs="pp(.rs)
say

say "changing current environment to:" pp("deux")
address deux
say "current environment:" pp(address())
say

"hello"
say "answer to command" pp(sourceline(.line-1)) "was:" pp(rc) ".rs="pp(.rs)
say

"ping"
say "answer to command" pp(sourceline(.line-1)) "was:" pp(rc) ".rs="pp(.rs)
say

"this is an unknown command"
say "answer to command" pp(sourceline(.line-1)) "was:" pp(rc) ".rs="pp(.rs)
say


info
say "answer to command" pp(sourceline(.line-1)) "was:" pp(rc) ".rs="pp(.rs)
say line

::requires "BSF.CLS"    -- get ooRexx-Java bridge
