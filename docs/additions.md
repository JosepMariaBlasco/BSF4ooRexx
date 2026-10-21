# Additions

This branch, `additions`, collects what is added to BSF4ooRexx on top of
`main` without changing the existing code: new tests, probes and notes.
Changes to existing code live on their own branches (for instance
`workaround-2122`). Each entry says what was added, why, and how it was
checked.

## Test group `CommandHandlerConditions` (21/10/2026)

`bsf4oorexx.dev/testUnits/bsf4rexx/additions/`: a Java command handler
(`additions.ConditionRaisingCommandHandler`, registered with
`BsfCommandHandler("ADD", ...)`) raises `USER MINE`, `NOTREADY` or
`ERROR` with `raiseCondition()`; the group checks that each reaches the
Rexx program as itself, trapped by `SIGNAL ON`, by `CALL ON`, or not
trapped, with and without `::OPTIONS ERROR SYNTAX` (helpers
`commandsPlain.rex` and `commandsErrorSyntax.rex`, identical but for the
option). With the option, only an untrapped `ERROR` becomes SYNTAX 98.970.

Why: ooRexx #2119. Up to r13281, with `::OPTIONS ERROR SYNTAX` any
condition a command handler raised became SYNTAX 98.970 unless a
`SIGNAL ON` trap caught it at once. Measured: on ooRexx r13278 the group
has 2 failures (`test_errorSyntax_callOn`, `test_errorSyntax_untrapped`:
`SYNTAX 98.970` instead of the condition); on r13282, 7 tests, 0
failures. The full suite on r13282 with `--legacy-lib-name`: 68/68
groups, 1,020 tests, 0 failures.

## Test group `HaltThroughCommandHandler` (21/10/2026)

`bsf4oorexx.dev/testUnits/bsf4rexx/additions/HaltThroughCommandHandler.testGroup`
documents what happens today when the Rexx code of a command handler is
halted. The handler is written in Rexx, wrapped with
`BsfCreateRexxProxy(..., "org.rexxla.bsf.engines.rexx.AbstractDirectCommandHandler")`
(as in `samples/Java/handlers/commandHandlers/30_java_starter850`) and
registered with `BsfCommandHandler("ADD", ...)`; its `handleCommand` runs
`RAISE HALT`. The program that issued the command gets **SYNTAX 40.900**
(`rexx_command_handler_entry()`, error 6, a Java exception) whether it
traps HALT with `SIGNAL ON`, with `CALL ON`, or not at all; the message
carries the original "Error 4.1: Program interrupted with HALT condition".
This is BSF4ooRexx's design (any error inside a handler is 40.900); the
group records it, it does not propose to change it. Measured on ooRexx
r13282: 4 tests, 0 failures; full suite 69/69 groups, 1,024 tests.
