call addJdorHandler     -- load and add the Java Rexx command handler, default name: JDOR
address jdor            -- set default environment to the JDOR command handler
width =220              -- define width of image in pixels (there are 72 px to an inch)
height=250              -- define height of image in pixels (there are 72 px to an inch)
new width height        -- create new bitmap with currX=0, currY=0, width=220, height=250
winShow                 -- show frame on screen
   -- draw two lines forming a big X
drawLine width height   -- from currX=0, currY=0
color red               -- set the color to red (preregistered with nickname "RED")
goto width 0            -- currX=220, currY=0
say "--> rc="pp(rc)     -- RC refers to the previous command's result: "220 0" (currX=220, currY=0)
drawLine 0 height       -- from currX=250, currY=0
   -- draw a rectangle at the center of the bitmap
len = 50                -- drawing a rectangle with each side being 50 px (a square)
goto (width-len)/2 (height-len)/2   -- go to the center minus half of the square's side lengths
color green             -- set color to green (preregistered with nickname "GREEN")
fillRect len len        -- fill the square with the green color
stroke str3 3           -- set line stroke to 3 points width, store it with nickname "STR3"
color blue              -- set color to blue (preregistered with nickname "BLUE")
drawRect len len        -- draw the frame with the blue color
   -- draw a string (text) at the lower half of the bitmap
color somePurple 127 46 111   -- define a custom RGB color, store it with nickname "SOMEPURPLE"
say "--> rc="pp(rc)     -- show result from command (the Java color object)
say "    rc~toString:" pp(rc~toString) -- shows the RGB intensities of the color
newY=height-40
lineHeight = 15         -- default font size is 12px, add 3 px space
                        -- define an array of three strings
text=("Hello, Rexx world ...", "... from JDOR ...", "(Rexx command handler)") -- array of 3 strings
do counter c i=0 to 30 by lineHeight   -- iterate over array of strings
   goto 50 (newY+i)     -- set position
   drawString text[c]   -- draw the string
end
                        -- save the current image
saveImage "jdor_doc_sample.png"  -- save image to file "jdor_doc_sample.png"
sleep 3.123             -- sleep for 3.123 seconds

::requires "jdor.cls"   -- Rexx package that defines the public routine addJdorHandler()
