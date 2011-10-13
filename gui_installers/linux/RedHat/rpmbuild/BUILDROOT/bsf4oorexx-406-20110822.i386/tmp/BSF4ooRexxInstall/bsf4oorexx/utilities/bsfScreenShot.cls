#!/usr/bin/rexx
/*
   Name:    "rgfScreenShot.cls"
   Author:  Rony G. Flatscher
   Purpose: little package to ease considerably the taking of all kind of screenshots
            in an operating system independent manner
   Date:    2010-02-20, 2010-03-13
   Needs:   BSF4ooRexx, ooRexx 4.0 or higher

   Usage:   require this class, which makes the following public routines available
                 /* routines to take screenshots:  */
            image=fullScreenShot()
            image=screenShot(awtComponent)
            image=takeScreenShot(x, y, width, height)

                  /* convenience routine to save image in a file  */
            call saveScreenShot(image[, filename="defaultImageFileName", filetype="gif"])

            Remarks:

                  - "image" may be .nil, if the awtComponent is off screen
                  - make sure that the awtComponent is in front of all other windows,
                    such that the screenshot can capture it

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2010 Rony G. Flatscher

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.
    -----------------------------------------------------------------------------
*/

if arg()>0 then   /* if any argument given, run this as a test */
do
   fn="A0.test"; type="jpg";
   say "creating a full screen shot, result in file:" fn "save as graphic type:" type
   image=fullScreenShot()
   call saveScreenShot image, fn, type
end



::requires bsf.cls   /* get direct Java support */


/* This routine will take a full screenshot using all pixels of the screen.   */
::routine fullScreenShot public

      /* get the screen size     */
  clzToolkit=.bsf~bsf.loadClass("java.awt.Toolkit")   /* load the Java class  */
  toolkit   =clzToolkit~getDefaultToolkit             /* use static method    */
  screenSize=toolkit~getScreenSize                    /* get Dimension object */

      /* take a screenshot from entire screen   */
  return takeScreenShot(0, 0, screenSize~width, screenSize~height)



/* Routine that expects any java.awt.Component to take a screen shot from. If the
   component is not located on the screen, then .nil is returned, otherwise an
   image object which can be saved in a file using the convenience routine saveScreenShot()
   below.
*/
::routine screenShot public
  use strict arg awtComponent

  signal on any                     /* intercept exceptions */

  point=awtComponent~getLocationOnScreen  /* raises an exception, if component not on screen */
  x=point~x                         /* extract x               */
  y=point~y                         /* extract y               */

  dimension=awtComponent~getSize
  width =dimension~width            /* extract width           */
  height=dimension~height           /* extract height          */

  toolkit=awtComponent~getToolkit   /* get the toolkit         */
  screenSize=toolkit~getScreenSize
  screenWidth =screenSize~width     /* extract screen width    */
  screenHeight=screenSize~height    /* extract screen height   */

  /* component fully on screen?  */
  if x>=0, x+width<=screenWidth, y>=0, y+height<=screenHeight then
     return takeScreenShot(x, y, width, height)

  /* calculatate visibleComponent component's screen co-ordinates and dimensions: x, y, width, height to use   */
  sX=x
  sY=y
  visibleComponentWidth =width
  visibleComponentHeight=height

  if x<0 then                       /* beyond left border?        */
  do
     visibleComponentWidth=width+x  /* subtract off-screen pixels */
     sX=0
  end

  if y<0 then                       /* beyond upper border?       */
  do
     visibleComponentHeight=height+y/* subtract off-screen pixels */
     sY=0
  end

  nrPixels=sX+visibleComponentWidth
  if screenWidth <= nrPixels then   /* is component's width not fully on-screen? */
     visibleComponentWidth=screenWidth-sX    /* adjust nr. of pixels    */

  nrPixels=sY+visibleComponentHeight
-- say "--> screenHeight="screenHeight "nrPixels="nrPixels "sY="sY
  if screenHeight <= nrPixels then  /* is component's height not fully on-screen? */
     visibleComponentHeight=screenHeight-sY  /* adjust nr. of pixels    */

  return takeScreenShot(sX, sY, visibleComponentWidth, visibleComponentHeight)

any:     /* an exception occurred, return .nil     */
  return .nil




/* Routine to take any screen shot, returning an image object. */
::routine takeScreenShot public
  use strict arg x, y, width, height

-- say "x="||x",y="y",width="width",height="height

      /* take the screenshot     */
  robot    =.bsf~new('java.awt.Robot')
  rectangle=.bsf~new("java.awt.Rectangle", x, y, width, height)
  image    =robot~createScreenCapture(rectangle)
  return image




/* Convenience routine to save image in a file in a certain image type.  */
::routine saveScreenShot public
  use strict arg image, filename="defaultImageFileName", imageType="png"

  signal on any

      /* save the screenshot (image) in file */
  lpos=fileName~lastPos(".")        /* maybe a file extension already given?  */
  if lpos>0, filename~subStr(lpos+1)=imageType then/* desired imageType already in file extension? */
     NOP                            /* do nothing  */
  else
     fileName=fileName"."imageType  /* define image type by file extension    */

  jFile=.bsf~new("java.io.File", fileName)   /* create a Java file object     */
  clzImageIO=.bsf~bsf.loadClass("javax.imageio.ImageIO") /* get the Java class*/
  clzImageIO~write(image, imageType, jFile)
  return jFile~getCanonicalFile     /* return canonical file name             */

any:     /* if an exception gets raised, indicate failure   */
  return ""                         /* indicate we could not save the image   */

