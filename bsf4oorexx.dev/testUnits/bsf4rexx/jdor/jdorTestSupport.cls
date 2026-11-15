/*
   name:             jdorTestSupport.cls
   author:           Josep Maria Blasco
   version:          0.1.0
   date:             2026-10-14
   license:          ASF 2.0 (Apache Software License)
   purpose:          support routines for the JDOR test groups
   hint:             the same test groups are meant to run against the Java
                     JDOR handler (BSF4ooRexx) and against the JDOR handler
                     of the browser build of ooRexx/WASM, whose BSF.CLS
                     emulates what JDOR programs use; hence only
                     .bsf~new(handler) and BsfCommandHandler("add", ...)
                     are used to set up a handler, and assertions are made
                     on numbers, strings returned as RC and getters of
                     returned objects, never on their string representations
*/

::requires "BSF.CLS"

/** Gives the ADDRESS environment envName (default JDOR) a brand-new JDOR
    handler and returns it. Each test method calls this first, so that no
    state leaks from one test to the next. */
::routine jdorTest.fresh public
  use strict arg envName = "JDOR"
  h = .bsf~new("org.oorexx.handlers.jdor.JavaDrawingHandler")
  call BsfCommandHandler "add", envName, h
  return h

/** Returns the pixel at (x, y) of the current image of environment envName,
    as Java's BufferedImage~getRGB returns it: a signed 32-bit ARGB integer
    (-65536 = opaque red, 0 = fully transparent). */
::routine jdorTest.pixel public
  use strict arg x, y, envName = "JDOR"
  address value envName
  'image'
  return rc~getRGB(x, y)

/** Given a line that the handler wrote to a redirected ERROR target, returns
    just the message proper: the text inside "cause: [...]", without the
    Java exception class ("java.lang.IllegalArgumentException: ") or the
    browser build's prefix ("JDOR (web): "). Lines without "cause: [" are
    returned unchanged. */
::routine jdorTest.cause public
  use strict arg line
  p = line~pos("cause: [")
  if p = 0 then return line
  msg = line~substr(p + 8)
  if msg~endsWith("]") then msg = msg~left(msg~length - 1)
  if msg~startsWith("JDOR (web): ") then msg = msg~substr(13)
  parse var msg first ": " rest
  if rest \== "", first~words = 1, -
     (first~endsWith("Exception") | first~endsWith("Error")), first~contains(".") then
    msg = rest
  return msg

/** The signed 32-bit integer that getRGB returns for an ARGB colour. */
::routine jdorTest.argb public
  use strict arg r, g, b, a = 255
  numeric digits 12
  v = ((a * 256 + r) * 256 + g) * 256 + b
  if v >= 2**31 then v = v - 2**32
  return v

/** Splits a getRGB value into its components: returns "r g b a".
    Caution: a negative literal such as -2130771968 written in the caller is
    an arithmetic expression and gets rounded to the caller's NUMERIC DIGITS
    (9 by default); quote it ('-2130771968') or use jdorTest.argb instead. */
::routine jdorTest.rgba public
  use strict arg pixel
  numeric digits 12
  if pixel < 0 then pixel = pixel + 2**32
  a = pixel % 2**24
  r = pixel % 2**16 // 256
  g = pixel % 256 // 256
  b = pixel // 256
  return r g b a

/** .true if every component of pixel is within tolerance of r g b a. */
::routine jdorTest.near public
  use strict arg pixel, r, g, b, a = 255, tolerance = 2
  parse value jdorTest.rgba(pixel) with pr pg pb pa
  return abs(pr - r) <= tolerance & abs(pg - g) <= tolerance & -
         abs(pb - b) <= tolerance & abs(pa - a) <= tolerance
