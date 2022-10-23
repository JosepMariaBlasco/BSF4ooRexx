// cf. <https://zetcode.com/gfx/java2d/basicdrawing/>
// TODO: B4R -> resolving constructors, if class is not public
// TODO: define a package (e.g. org.oorexx.handler.jdor ?)
// TODO: pushGC: also save GC related current values?

/* Java classes that constitute the JDOR ("JavaDrawing ooRexx") command handler.

   This enables any programmer to easily create and manipulate bitmaps on any
   operating system (Windows, MacOS, Linux) where Java, ooRexx and BSF4ooRexx is
   available.

   author:  Rony G. Flatscher
   date:    2022-09-05/2022-10-05
   version: 003.20221020
   license: Apache license 2.0

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2022 Rony G. Flatscher

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

/*  changes:
        2022-10-05: - corrected Frame's currX and currY values
                    - corrected return value for IMAGE_SIZE as num+' '+num would add the
                      numbers with the value of the single blank value to num+" "+num to
                      return a string

        2022-10-20: - corrected drawString argument: make sure to preserve leading blanks
                      by just skipping a single blank after the command, taking the remainder
                      as the whole string

        2022-10-21: - corrected WIN_LOCATION: now honors no arguments
                    - new command "drawImageFromVar": allow an image referred to by a Rexx
                      variable to be drawn (to ease drawing images on different JDOR handler
                      instances)
        2022-10-22: - fold DRAW_IMAGE_FROM_VAR into DRAW_IMAGE
                    - corrected LOAD_IMAGE to return dimension instead of image (cf. documentation)
                    - change logic for queryable and setable commands: if setting new
                      value, return previous value
        2022-10-23: - reworkd some WIN_* commands to not automatically create and show JFrame
*/

/*  Sample program in ooRexx (temporarily):

        -- load and add the Java Rexx command handler, using default name: JDOR
        call addJdorHandler
        address jdor            -- change default address environment

        signal on failure name any
        signal on error   name any

        new 500 500             -- create a new bitmap
        winShow                 -- show it in a frame

        goto 10 10
        color orange
        drawString "Hello at:" .dateTime~new
        translate 10 490
        color green
        drawString "Hello at:" .dateTime~new
        translate 490 10
        color red
        drawString "Hello at:" .dateTime~new
        "translate -250 -250"   -- must be quoted (minus causes Rexx to try arithmetics)
        color blue
        drawString "Hello at:" .dateTime~new
        color red
        angle=30
        do i=1 to 360/angle - 1
           rotate angle
           drawString "ooRexx at:" .dateTime~new
        end

        save "testTranslate.png"   -- save as png file
        sleep 2500                 -- halts execution for 2 1/2 seconds
        exit

        any:  -- if command raise failure or error conditions, show them
        co=condition('o')       -- get a directory with the condition infos
           say ppCondition2(co) -- format and show the condition information
           say "---"
           raise propagate      -- now let ooRexx handle this condition

        ::requires "jdor.cls"      -- makes routine addJdorHandler([environmentName]) available
        ::requires "rgf_util2.rex" -- get access to ppCondition2()

*/

package org.oorexx.handlers.jdor;


import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;   // for loading/saving

import org.apache.bsf.BSFException;
import org.rexxla.bsf.engines.rexx.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

// for printing support
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

// ============================================================================
/** Class to allow displaying the current image.
*/
class JavaDrawingFrame extends JFrame
{
    /* static definitions           */
    static final private int prefWidth  = 500;
    static final private int prefHeight = 500;

    /* instance definitions         */
    boolean bDebug= false; // true
    Surface sf = null;

    int currWidth  = prefWidth;
    int currHeight = prefHeight;
    boolean currFrameVisible = true;    // cf. command "winFrame [.true|.false]"

    BufferedImage bufImage = null;


    /** Constructor.
     * @param img the BufferedImage to display
     */
    public JavaDrawingFrame(BufferedImage img)
    {
        bufImage=img;
        initUI();
    }

    /** Set up the UI using predefeined width and height for content pane. */
    private void initUI()
    {
        sf=new Surface();
        add(sf);
        setTitle("JavaDrawingFrame");
        // needs to be followed by pack() to take effect immediately
        currWidth=bufImage.getWidth();
        currHeight=bufImage.getHeight();
        getContentPane().setPreferredSize(new Dimension(currWidth,currHeight));
        pack();

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /** Resizes frame.
     *  @param width  the width in pixel
     *  @param height the width in pixel
     */
    public void resizeSurface (int width, int height)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                doResize(width, height);
            }
        });
    }

    /** Resizes frame (worker method, run on the GUI thread).
     *  @param width  the width in pixel
     *  @param height the width in pixel
     */
    void doResize (int width, int height)
    {
        getContentPane().setPreferredSize(new Dimension(width, height));
        pack();
    }

    // ============================================================================
    /** Surface to draw to. */
    class Surface extends JPanel
    {
        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.drawImage(bufImage,0,0,null);
        }
    }
}



// ============================================================================
public class JavaDrawingHandler implements RexxRedirectingCommandHandler
{
    /* static definitions           */

    // default image sizes
    static final public String version = "005.20221022";
    static final private int prefWidth  = 500;
    static final private int prefHeight = 500;
    static       private int prefImageType = BufferedImage.TYPE_INT_ARGB; // default with alpha (only png supported)

        // a Rexx variable symbol may start with one of the following characters
    static final String startRexxVariableChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_!?";

    static final HashMap<String,Color> predefinedColors     = new HashMap<>();
    // BufferedImage
    static final HashMap<String,Integer> imageTypes         = new HashMap<>();
    static final HashMap<Integer,String> imageTypesInt2Name = new HashMap<>();
    // BasicStroke
    static final HashMap<String,Integer> endCaps            = new HashMap<>();
    static final HashMap<Integer,String> endCapsInt2Name    = new HashMap<>();
    static final HashMap<String,Integer> lineJoins          = new HashMap<>();
    static final HashMap<Integer,String> lineJoinsInt2Name  = new HashMap<>();
    // AlphaComposite
    static final HashMap<String,Integer> alphaComposites         = new HashMap<>();
    static final HashMap<Integer,String> alphaCompositesInt2Name = new HashMap<>();

    static {
        // ----------------------------------------------------------------------
        // Color
        predefinedColors.put("BLACK"        ,  Color.BLACK        );
        predefinedColors.put("BLUE"         ,  Color.BLUE         );
        predefinedColors.put("CYAN"         ,  Color.CYAN         );
        predefinedColors.put("DARK_GRAY"    ,  Color.DARK_GRAY    );
        predefinedColors.put("GRAY"         ,  Color.GRAY         );
        predefinedColors.put("GREEN"        ,  Color.GREEN        );
        predefinedColors.put("LIGHT_GRAY"   ,  Color.LIGHT_GRAY   );
        predefinedColors.put("MAGENTA"      ,  Color.MAGENTA      );
        predefinedColors.put("ORANGE"       ,  Color.ORANGE       );
        predefinedColors.put("PINK"         ,  Color.PINK         );
        predefinedColors.put("RED"          ,  Color.RED          );
        predefinedColors.put("WHITE"        ,  Color.WHITE        );
        predefinedColors.put("YELLOW"       ,  Color.YELLOW       );

        // ----------------------------------------------------------------------
        // BufferedImage
        imageTypes.put("TYPE_CUSTOM"        , BufferedImage.TYPE_CUSTOM          );     // 0                      
        imageTypes.put("TYPE_INT_RGB"       , BufferedImage.TYPE_INT_RGB         );     // 1
        imageTypes.put("TYPE_INT_ARGB"      , BufferedImage.TYPE_INT_ARGB        );     // 2
        imageTypes.put("TYPE_INT_ARGB_PRE"  , BufferedImage.TYPE_INT_ARGB_PRE    );     // 3
        imageTypes.put("TYPE_INT_BGR"       , BufferedImage.TYPE_INT_BGR         );     // 4
        imageTypes.put("TYPE_3BYTE_BGR"     , BufferedImage.TYPE_3BYTE_BGR       );     // 5
        imageTypes.put("TYPE_4BYTE_ABGR"    , BufferedImage.TYPE_4BYTE_ABGR      );     // 6
        imageTypes.put("TYPE_4BYTE_ABGR_PRE", BufferedImage.TYPE_4BYTE_ABGR_PRE  );     // 7
        imageTypes.put("TYPE_USHORT_565_RGB", BufferedImage.TYPE_USHORT_565_RGB  );     // 8
        imageTypes.put("TYPE_USHORT_555_RGB", BufferedImage.TYPE_USHORT_555_RGB  );     // 9
        imageTypes.put("TYPE_BYTE_GRAY"     , BufferedImage.TYPE_BYTE_GRAY       );     //10
        imageTypes.put("TYPE_USHORT_GRAY"   , BufferedImage.TYPE_USHORT_GRAY     );     //11
        imageTypes.put("TYPE_BYTE_BINARY"   , BufferedImage.TYPE_BYTE_BINARY     );     //12
        imageTypes.put("TYPE_BYTE_INDEXED"  , BufferedImage.TYPE_BYTE_INDEXED    );     //13
        // allow for looking up constant names (key) by value
        imageTypes.forEach((K,V)->imageTypesInt2Name.put(V,K));

        // ----------------------------------------------------------------------
        // BasicStroke
        endCaps.put("CAP_BUTT"  , 0);
        endCaps.put("CAP_ROUND" , 1);
        endCaps.put("CAP_SQUARE", 2);
        // allow for looking up constant names (key) by value
        endCaps.forEach((K,V)->endCapsInt2Name.put(V,K));

        lineJoins.put("JOIN_MITER", 0);
        lineJoins.put("JOIN_ROUND", 1);
        lineJoins.put("JOIN_BEVEL", 2);
        // allow for looking up constant names (key) by value
        lineJoins.forEach((K,V)->lineJoinsInt2Name.put(V,K));

        // ----------------------------------------------------------------------
        // AlphaComposite
        alphaComposites.put("CLEAR"   ,  1);
        alphaComposites.put("DST"     ,  9);
        alphaComposites.put("DST_ATOP", 11);
        alphaComposites.put("DST_IN"  ,  6);
        alphaComposites.put("DST_OUT" ,  8);
        alphaComposites.put("DST_OVER",  4);
        alphaComposites.put("SRC"     ,  2);
        alphaComposites.put("SRC_ATOP", 10);
        alphaComposites.put("SRC_IN"  ,  5);
        alphaComposites.put("SRC_OUT" ,  7);
        alphaComposites.put("SRC_OVER",  3);
        alphaComposites.put("XOR"     , 12);
        // allow for looking up constant names (key) by value
        alphaComposites.forEach((K,V)->alphaCompositesInt2Name.put(V,K));
    }


    int nrCommand=0;    // in case many commands, indicates which command may have caused an error or failure condition

    /* instance definitions         */
    JavaDrawingFrame jd = null;         // maintance a singleton or empty
    boolean bDebug=false; // true; // false; // true

    // we draw here and do a drawImage in the Surface paint
    BufferedImage bufImage = null;
    // int           currImageType = prefImageType;
    Graphics2D    bufGC    = null;

    /* printer related values */
    boolean printScaleToPage = false;
    int     printPosX=0, printPosY=0;
    double  printScaleX=1, printScaleY=1;

    /* current values      */
    int currX = 0;
    int currY = 0;
// TODO: if popGC reset currAngle to that one; needs to also push currAngle;
//       not necessary in the first incarnation
    double currAngle = 0;   // if rotate gets used, keeps current angle (in radians)

    int currFontStyle = 0;   // PLAIN by default
    int currFontSize  = 12;  // by default

    boolean currVisible = false;
    boolean currWinUpdate = true;       // update Frame whenever we draw: allows turning updating on/off


    /** Stack for graphic configurations. */
    ArrayDeque <Graphics2D>adGCStack = new ArrayDeque<>();

    /** Stack for images. */
    ArrayDeque <BufferedImage>adImageStack = new ArrayDeque<>();

    /** Maps for definable, loadable resources. Index is name in uppercase. */
    HashMap<String,BufferedImage> hmImages = new HashMap<>();
    /** Contains always the predefined colors (cf. Javadoc of java.awt.Color).  */
    HashMap<String,Color>         hmColors = new HashMap<>(predefinedColors );
    HashMap<String,GradientPaint> hmGradientPaints = new HashMap<>();
    HashMap<String,Font>          hmFonts  = new HashMap<>();
    HashMap<String,Stroke>        hmStrokes= new HashMap<>();


    void reset ()   // reset all current variables and caches
    {
        currX            =  0;
        currY            =  0;
        currAngle        =  0;

        currFontStyle    =  0;  // PLAIN by default
        currFontSize     = 12;

        printScaleToPage = false;
        printPosX        = 0;
        printPosY        = 0;
        printScaleX      = 1;
        printScaleY      = 1;

            // empty stack
        Graphics2D tGC = null;
        if (adGCStack.size()>0)
        {
            tGC = adGCStack.pop();
        }
        while (tGC!=null)
        {
            tGC.dispose();
            adGCStack.pop();
        }
        adImageStack.clear();
        hmImages.clear();
        hmColors=new HashMap<>(predefinedColors );
        hmGradientPaints = new HashMap<>();
        hmFonts.clear();
        hmStrokes.clear();
        if (jd!=null && bufImage!=null)
        {
if (bDebug) System.err.println(" /// ---> resizeSurface(): w="+bufImage.getWidth()+", h="+bufImage.getHeight());
            jd.resizeSurface(bufImage.getWidth(), bufImage.getHeight());   // will use invokeLater()
        }
    }


    /** Callback method of this command handler.
     *
     * @param slot opaque argument (needs to be used if invoking the Handler default methods to interact with the Rexx context)
     * @param address the environment name this handler works for
     * @param command the command to process
     */
    public Object handleCommand(Object slot, String address, String command)
    {
        nrCommand++;        // increase counter
if (bDebug)    System.err.println("[JavaDrawingHandler].handleCommand(slot, address=["+address+"]"
                                   + "command # ["+nrCommand+"]"
                                   + "command=["+command+"]"

                                   );
        Object res=null;
        if (isInputRedirected(slot))    // take commands via redirected input
        {
            String currCommand=null;
            while ( (currCommand=readInput(slot)) != null)   // feed commands from redirected input
            {
                res=processCommand(slot, address, command, nrCommand, currCommand);
                if (checkCondition(slot))   // a Rexx condition got set: stop processing, return immediately
                {
                    return null;
                }
            }
        }
        else    // single command
        {
            res=processCommand(slot, address, command, nrCommand, null);
        }
        return res;
    }


// TODO: currently currCommand is not processed, should be a comment; maybe needs to be revisited?

    /** Process single command
                processCommand(slot, address, command, nrCommand, currCommand);
     * @param slot opaque argument (needs to be used if invoking the Handler default methods to interact with the Rexx context)
     * @param address the environment name this handler works for
     * @param command the command to process
     * @param nrCommand counter to number this command
     * @param currCommand command that was received in redirected modus
     */
    Object processCommand(Object slot, String address, String command, int nrCommand, String currCommand)
    {
        String [] arrCommand = null;
        String    mainCommand = command;    // command via address environment
        if (currCommand!=null)
        {
            command=currCommand;        // command from redirected input
        }

        boolean isOR = isOutputRedirected(slot);    // is output redirected ?

        // get the boundaries of the first ten words
        ArrayList<int[]> alWordBoundaries = getWordBoundaries (command, 15);
        arrCommand = getNonBlankWords(command, alWordBoundaries);

        Command cmd = null;
        if (arrCommand.length>0)        // empty string
        {
            cmd=Command.getCommand(arrCommand[0]);    // can be null, if not available
        }

        if (cmd==null)  // unknown command raise error condition
        {
            String commentChars = "-#;/\"'.:!?_";      // regarded as a comment if any used as first non-blank character
            if (arrCommand.length==0 || arrCommand[0].length()==0 || (commentChars).indexOf(arrCommand[0].charAt(0))>=0)   // treat as comment)
            {
                if (isOR)
                {
                   writeOutput(slot, command);  // write unchanged
                }
                return null;
            }
            String errMsg = "unknown command";
            if (isOR)
            {
                writeOutput(slot, "-- FAILURE (unknown command): ["+command+"]");
            }
            return createCondition (slot, nrCommand, command, ConditionType.FAILURE, "-1", errMsg );
        }

            // no image as of yet, not the NEW command, hence create a default image
        if (bufImage==null && !(cmd==Command.NEW))
        {
            bufImage = new BufferedImage(prefWidth, prefHeight, prefImageType);
            bufGC    = (Graphics2D) bufImage.getGraphics();
            bufGC.setColor(Color.BLACK);
            Font currFont = bufGC.getFont();
            currFontStyle = currFont.getStyle();
            currFontSize  = currFont.getSize();
        }

        String  canonical = cmd.mixedCase;;
        Object  resultValue = null;

        // process commands; jd may be null !
        try
        {
            switch (cmd)        // process commands
            {
                // ---------------------- display, control related --- Start
                case WIN_SHOW:      // show frame
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command does not accept any arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        if (currVisible)    // already visible, nothing to do
                        {
                            return null;
                        }
                        if (jd==null)   // implies creation of JavaDrawingFrame and make it visible
                        {
                            jd = new JavaDrawingFrame(bufImage);
                        }
                        jd.setVisible(true);    // make sure frame is visible
                        currVisible=true;
                        break;
                    }

                case WIN_HIDE:      // hide frame
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command does not accept any arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        if (!currVisible || jd==null)   // not visible already, nothing to do
                        {
                            return null;
                        }
                        jd.setVisible(false);
                        currVisible=false;
                        break;
                    }

                case WIN_CLOSE:     // close (exit) JFrame
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command does not accept any arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        if (jd==null)       // nothing to do
                        {
                            return null;
                        }

                        jd.dispatchEvent(new WindowEvent(jd, WindowEvent.WINDOW_CLOSING));
                        jd=null;
                        currVisible=false;
                    }
                    break;

                case WIN_LOCATION:  // synonym WINMOVETO   "winLocation [x y]" get or set JFrame location on screen
                    {
                        if (arrCommand.length!=1 && arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        if (jd==null)   // implies creation of JavaDrawingFrame and make it visible
                        {
                            jd = new JavaDrawingFrame(bufImage);
                            jd.setVisible(true);    // make sure frame is visible
                            currVisible=true;
                        }

                        int newX = jd.getX();
                        int newY = jd.getY();

                        resultValue = newX+" "+newY;  // query current value (to be returned if change occurs)

                        if (arrCommand.length==3)
                        {
                            newX = Integer.parseInt(arrCommand[1]);
                            newY = Integer.parseInt(arrCommand[2]);
                            jd.setLocation(newX, newY);
                            canonical = canonical+" "+newX+" "+newY;
                        }

                        if (isOR)
                        {
                           writeOutput(slot, canonical);  // write canonical form
                        }
                        if (arrCommand.length==1)
                        {
                            // return newX+" "+newY;
                            return resultValue;
                        }
                        break;
                    }

                    // new 2022-09-27
                case WIN_ALWAYS_ON_TOP:             // "winAlwaysOnTop [.true | .false]" get or set of this window state
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }
                        if (jd!=null)
                        {
                            resultValue = (jd.isAlwaysOnTop() ? "1" : "0");   // query current value (to be returned if change occurs)
                        }
                        else
                        {
                            resultValue = "0";      // false, does not exist
                        }
                    }
                case WIN_TO_BACK:                   // "winToBack" put window into the back
                case WIN_TO_FRONT:                  // "winToFront" put window into the front
                case WIN_ALWAYS_ON_TOP_SUPPORTED:   // "winAlwaysOnTopSupported" get value
                    {
                        if (cmd!=Command.WIN_ALWAYS_ON_TOP && arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command expects no arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        if (jd==null)   // implies creation of JavaDrawingFrame
                        {
                            jd = new JavaDrawingFrame(bufImage);
                            if (cmd!=Command.WIN_ALWAYS_ON_TOP_SUPPORTED)
                            {
                                jd.setVisible(true);    // make sure frame is visible
                                currVisible=true;
                            }
                            else
                            {
                                currVisible=false;
                            }
                        }

                        switch (cmd)
                        {
                            case WIN_ALWAYS_ON_TOP:
                                {
                                    if (arrCommand.length!=1)
                                    {
                                        String newValue = arrCommand[1];
                                        boolean retBoolValue;

                                        if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                                        {
                                            retBoolValue=getBooleanValue(newValue);  // get value
                                        }
                                        else
                                        {
                                            throw new IllegalArgumentException("the supplied \"frameVisible\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                                        }
                                        canonical=canonical+" "+newValue;
                                        jd.setAlwaysOnTop(retBoolValue);    // set value
                                    }
                                    break;
                                }

                            case WIN_TO_BACK:                   // "winToBack" put window into the back
                                jd.toBack();
                                break;

                            case WIN_TO_FRONT:                  // "winToFront" put window into the front
                                jd.toFront();
                                break;

                            case WIN_ALWAYS_ON_TOP_SUPPORTED:   // "winAlwaysOnTopSupported" get value
                                // retBoolValue=jd.isAlwaysOnTopSupported();
                                resultValue = (jd.isAlwaysOnTopSupported() ? "1" : "0");   // query current value (to be returned if change occurs)
                                break;
                        }

                        if (isOR)
                        {
                           writeOutput(slot, canonical);  // write canonical form
                        }

                        // return retBoolValue ? "1" : "0";
                        return resultValue;
                    }


                case WIN_TITLE:     // winTitle [newTitle]: query or set title
                    {
                        if (jd==null)   // implies creation of JavaDrawingFrame and make it visible
                        {
                            jd = new JavaDrawingFrame(bufImage);
                            jd.setVisible(true);    // make sure frame is visible
                            currVisible=true;
                        }
                        resultValue = jd.getTitle();    // query current value (to be returned if change occurs)
                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot,canonical);
                            }
                            return resultValue;
                        }

                            // to fetch leading blanks we skip over first blank (unlike for other commands where we ignore all blanks after the command)
                        int [] pos = (int []) alWordBoundaries.get(0);
                        String newTitle = command.substring(pos[1]+1); // extract String: skip over first trailing blank

                        if (isOR)
                        {
                            writeOutput(slot,canonical+" "+newTitle);
                        }
                            // no exception, so args o.k.
                        jd.setTitle(newTitle); // will use invokeLater()
                        break;
                    }


                case WIN_FRAME:     // winFrame [.true|.false]: query or set whether frame is visible
                    {
                        if (jd==null)   // implies creation of JavaDrawingFrame and make it visible
                        {
                            jd = new JavaDrawingFrame(bufImage);
                            // jd.setVisible(true);    // make sure frame is visible
                            // currVisible = true;
                            currVisible = false;
                        }

                        resultValue = jd.currFrameVisible ? "1" : "0";  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot,canonical);
                            }
                            return resultValue;
                        }

                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        String newValue = arrCommand[1];
                        boolean newFrameVisible=false;

                        if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                        {
                            newFrameVisible=getBooleanValue(newValue);  // get value
                        }
                        else
                        {
                            throw new IllegalArgumentException("the supplied \"frameVisible\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }

                        // no exception, so args o.k.
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+ (newFrameVisible ? "1" : "0") );
                        }

                        if (jd.currFrameVisible!=newFrameVisible)
                        {
                            // 2022-09-22, cf. <https://stackoverflow.com/questions/8701716/how-to-remove-title-bar-in-jframe>
                            jd.dispose();
                            jd.setUndecorated(!newFrameVisible); // <-- the title bar is removed here
                            jd.pack();
                            jd.currFrameVisible=newFrameVisible;
                            if (currVisible)            // make visible?
                            {
                                jd.setVisible(true);
                            }
                        }
                        break;
                    }

                case WIN_VISIBLE:     // winVisible [.true|.false]: query or set whether frame is visible
                    {
                        resultValue = currVisible ? "1" : "0";  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot,canonical);
                            }
                            return resultValue;
                        }

                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }

                        String newValue = arrCommand[1];
                        boolean newBooleanValue=false;

                        if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                        {
                            newBooleanValue=getBooleanValue(newValue);  // get value
                        }
                        else
                        {
                            throw new IllegalArgumentException("the supplied \"frameVisible\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }

                        if (currVisible==newBooleanValue)   // already at the same state, ignore command, but return "old" value
                        {
                            return resultValue;
                        }

                        // no exception, so args o.k.
                        if (isOR && arrCommand.length==2)
                        {
                            writeOutput(slot,canonical+" "+(newBooleanValue ? "1" : "0"));
                        }

                        if (jd==null)
                        {
                            jd = new JavaDrawingFrame(bufImage);
                            if (newBooleanValue)
                            {
                                jd.setVisible(true);    // make frame is visible
                            }
                        }
                        else if (currVisible!=newBooleanValue)
                        {
                            jd.setVisible(newBooleanValue);
                        }
                        currVisible = newBooleanValue;
                        break;
                    }

                case WIN_UPDATE:     // "winUpdate [.true|.false]" get or set whether Frame should be updated upon draws/changes
                    {
                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot,canonical);
                            }
                            return (currWinUpdate ? "1" : "0");
                        }

                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command accepts 1 argument at the most, received "+(arrCommand.length-1)+" instead");
                        }

                        String newValue = arrCommand[1];
                        boolean newBooleanValue=false;

                        if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                        {
                            newBooleanValue=getBooleanValue(newValue);  // get value
                        }
                        else
                        {
                            throw new IllegalArgumentException("the supplied \"frameVisible\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }
                        // no exception, so args o.k.
                        currWinUpdate = newBooleanValue;
                        String res = (currWinUpdate ? "1" : "0");
                        if (isOR)
                        {
                            writeOutput(slot,canonical+" "+res);
                        }
                        // return res;
                        break;
                    }

                case WIN_SIZE:      // query or set size
                    {
                        if (jd==null)   // implies creation of JavaDrawingFrame and make it visible
                        {
                            jd = new JavaDrawingFrame(bufImage);
                            if (currVisible)
                            {
                                jd.setVisible(true);
                            }
                        }

                        resultValue = jd.currWidth+" "+jd.currHeight;  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }
                        if (arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        int newWidth  = Integer.parseInt(arrCommand[1]);
                        int newHeight = Integer.parseInt(arrCommand[2]);

                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+newWidth+" "+newHeight);
                        }
                            // no exception, so args o.k.
                        jd.resizeSurface(newWidth, newHeight);   // will use invokeLater()
                        break;
                    }

                case WIN_SCREEN_SIZE:   // query screen size
                    {
                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
                            return screenSize.width+" "+screenSize.height;
                        }
                        throw new IllegalArgumentException("this command does not expect any arguments, received "+(arrCommand.length-1)+" instead");
                    }

                case PRINT_SCALE_TO_PAGE: // "printScaleToPage [.true|.false]": query/get or set
                    {
                        resultValue = (printScaleToPage ? "1" : "0");  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot,canonical);
                            }
                            return resultValue;
                        }

                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }

                        String newValue = arrCommand[1];
                        if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                        {
                            printScaleToPage=getBooleanValue(newValue);  // get value
                        }
                        else
                        {
                            throw new IllegalArgumentException("the supplied \"printScaleToPage\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }
                        // return printScaleToPage ? "1" : "0";
                        return resultValue;
                    }

                case PRINT_SCALE:   // "printScale [scaleX [scaleY]": query/get or set scaleX (if no scaleY use scaleX)
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs no or 1 or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        resultValue = printScaleX+" "+printScaleY;  // query current value (to be returned if change occurs)

                        if (arrCommand.length>1)
                        {
                            double tmpScaleX = Double.parseDouble(arrCommand[1]);
                            canonical = canonical + " " + arrCommand[1];

                            if (arrCommand.length==2)   //
                            {
                                printScaleX=tmpScaleX;
                                printScaleY=tmpScaleX;  // use X value also for Y
                            }
                            else // scaleY given
                            {
                                printScaleY = Double.parseDouble(arrCommand[2]);
                                printScaleX = tmpScaleX;
                                canonical = canonical + " " + arrCommand[2];
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot,canonical);
                        }

                        //return printScaleX+" "+printScaleY;
                        return resultValue;
                    }

                case PRINT_POS: // "printPos [X [Y]]: get or set position of left upperhand corner
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command expects no, 1 or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        resultValue = printPosX+" "+printPosY;  // query current value (to be returned if change occurs)

                        int newX = Integer.parseInt(arrCommand[1]);
                        int newY = newX;            // default to newX
                        if (arrCommand.length==3)
                        {
                            newY = Integer.parseInt(arrCommand[2]);
                        }
                            // no exception, so args o.k.
                        printPosX = newX;
                        printPosY = newY;
                        if (isOR)
                        {
                            canonical=canonical+" "+newX+" "+newY;
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        // return res;
                        return resultValue;
                    }

                case PRINT_IMAGE:       // "printImage [nickName]"
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (image's nickname for lookup), received "+(arrCommand.length-1)+" instead");
                        }
                        BufferedImage tmpImage = null;
                        if (arrCommand.length==1)
                        {
                            tmpImage = bufImage;
                        }
                        else
                        {
                            String name = arrCommand[1];        // get  nickname
                            tmpImage = hmImages.get(name.toUpperCase());
                            if (tmpImage==null)
                            {
                                throw new IllegalArgumentException("image with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\"");
                            }
                            canonical = canonical+" "+name;
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        // now print
                        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
                        DocPrintJob job = service.createPrintJob();
                        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;

                        SimpleDoc doc = new SimpleDoc(new JavaDrawingPrintable(tmpImage,this), flavor, null);
                        job.print(doc, null);
                        return null;
                    }

                case SLEEP:     // sleep for msecs (allows for simple animations)
                    {
                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }
                        double secs = Double.parseDouble(arrCommand[1]);

                        if (isOR)
                        {
                           writeOutput(slot, canonical+" "+secs);  // write canonical form
                        }
                        try { Thread.sleep((long)(secs*1000)); } catch (Throwable t) {}

                        return null;
                    }


                    // ---------------------- display, control related --- end
                case SCALE:         // "scale x [y]" set scale for x, y; if y omitted, uses x
                case SHEAR:         // "shear x [y]" set scale for x, y; if y omitted, uses x
                    {
                        int argNum=arrCommand.length;
                        if (argNum<2 || argNum>3)
                        {
                            throw new IllegalArgumentException("this command needs either one or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        double newX = Double.parseDouble(arrCommand[1]);
                        double newY = newX;     // default to X value in case Y value is omitted
                        if (argNum==3)          // Y value supplied, use it
                        {
                            newY=Double.parseDouble(arrCommand[2]);
                        }
                        String strResult= newX+" "+newY;

                        if (cmd==Command.SCALE)
                        {
                            bufGC.scale(newX,newY);
                        }
                        else
                        {
                            bufGC.shear(newX,newY);
                        }
                        if (isOR)
                        {
                           writeOutput(slot, canonical+" "+strResult);  // write canonical form
                        }
                        // return strResult;
                        return null;        // returns null, i.e. RC="0" -> command executed alright
                    }

                case NEW:       // "new [width height [type]]" creates a new BufferedImage
                    {
                        if (arrCommand.length!=1 && arrCommand.length!=3 && arrCommand.length!=4)
                        {
                            throw new IllegalArgumentException("this command needs no, 2 (width height) or 3 (width height type) arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int width      = prefWidth;
                        int height     = prefHeight;
                        int type       = prefImageType;   // (starts out with TYPE_INT_ARGB, i.e. wiht alpha/transparency)

                        if (arrCommand.length>2)
                        {
                            width  = Integer.parseInt(arrCommand[1]);
                            height = Integer.parseInt(arrCommand[2]);
                            if (isOR)
                            {
                                canonical = canonical + " " + width + " " + height;
                            }

                            if (arrCommand.length>3)
                            {
                                String argType = arrCommand[3].toUpperCase();
                                if (startRexxVariableChar.indexOf(argType.charAt(0))>=0)    // a symbolic name?
                                {
                                    if (!imageTypes.containsKey(argType))
                                    {
                                        throw new IllegalArgumentException("unknown value for \"type\" argument");
                                    }
                                    type=imageTypes.get(argType);
                                }
                                else // verbatim int type
                                {
                                    type=Integer.parseInt(arrCommand[3]);
                                    if (!imageTypes.containsValue(type))
                                    {
                                        throw new IllegalArgumentException("unknown value for \"type\" argument");
                                    }
                                }
                                if (isOR)
                                {
                                    canonical = canonical + " " + argType;
                                }
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                            // no exception, so args o.k.
                        if (bufImage!=null)     // make sure all old GCs get disposed
                        {
                            bufImage=null;
                            if (bufGC!=null)
                            {
                                // empty stack, dispose GraphicConfigurations
                                Graphics2D tmpGC = null;
                                if (adGCStack.size()>0)
                                {
                                    tmpGC=adGCStack.pop();
                                }
                                while (tmpGC != null)
                                {
                                    tmpGC.dispose();
                                    tmpGC = adGCStack.pop();
                                }
                                bufGC.dispose();
                                bufGC=null;
                            }
                        }

                        bufImage=new BufferedImage(width, height, type);    // create new image
                        bufGC=(Graphics2D) bufImage.getGraphics();          // get its GraphicsConfiguration
                        bufGC.setColor(Color.BLACK);            // make sure color is set to black
                        if (jd!=null)                           // update Frame if it exists
                        {
                            jd.bufImage=bufImage;
                        }
                    }
                    break;

                case PREFERRED_IMAGE_TYPE:      // "preferredImageType [type]": query or set: integer or constant name
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (preferred image type), received "+(arrCommand.length-1)+" instead");
                        }
                        resultValue = ""+prefImageType;  // query current value (to be returned if change occurs)

                        if (arrCommand.length==2)
                        {
                            int    type;
                            String newType = arrCommand[1].toUpperCase();
                            if (startRexxVariableChar.indexOf(newType.charAt(0))>=0)    // a symbolic name?
                            {
                                if (!imageTypes.containsKey(newType))
                                {
                                    throw new IllegalArgumentException("unknown value for \"type\" argument");
                                }
                                type=imageTypes.get(newType);
                            }
                            else // verbatim int type
                            {
                                type=Integer.parseInt(newType);
                                if (!imageTypes.containsValue(type))
                                {
                                    throw new IllegalArgumentException("unknown value for \"type\" argument");
                                }
                            }
                            if (isOR)
                            {
                                canonical = canonical + " " + arrCommand[1];  // uses supplied argument value
                            }
                            prefImageType=type;
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        // return "" + prefImageType;
                        return resultValue;
                    }

                case IMAGE_TYPE:      // "imageType [nickName]"
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (image's nickname for lookup), received "+(arrCommand.length-1)+" instead");
                        }
                        if (arrCommand.length==1)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return ""+bufImage.getType();
                        }

                        String name = arrCommand[1];        // get  nickname
                        BufferedImage img = hmImages.get(name.toUpperCase());
                        if (img==null)
                        {
                            throw new IllegalArgumentException("image with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\"");
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+name);
                        }
                        return ""+img.getType();
                    }

                case IMAGE_SIZE:      // "imageSize [nickName]"
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (image's nickname for lookup), received "+(arrCommand.length-1)+" instead");
                        }
                        BufferedImage tmpImg = bufImage;        // current image
                        if (arrCommand.length==2)               // load image
                        {
                            String name = arrCommand[1];        // get  nickname
                            tmpImg = hmImages.get(name.toUpperCase());
                            if (tmpImg==null)
                            {
                                throw new IllegalArgumentException("image with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\"");
                            }
                            if (isOR)
                            {
                                canonical = canonical+" "+name;
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return tmpImg.getWidth()+" "+tmpImg.getHeight();
                    }

                    // create a RexxStringTable, fill it with current settings, stacks, maps and return it;
                    // if optional nameCtxtVariable supplied, store it as a context variable in addition
                case GET_STATE:     // "getstate [ctxtVariableName]" returns a StringTable with current variables, stacks and HashMaps;
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (nameOfContextVariable), received "+(arrCommand.length-1)+" instead");
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        RexxProxy rop = (RexxProxy) newStringTable(slot);   // get a StringTable
                        rop.sendMessage2("SETENTRY", "currX"      , currX);
                        rop.sendMessage2("SETENTRY", "currY"      , currY);
                        rop.sendMessage2("SETENTRY", "currAngle"  , currAngle);

                        rop.sendMessage2("SETENTRY", "prefImageType"   , prefImageType );   // currently preferred image type

                        rop.sendMessage2("SETENTRY", "image"           , bufImage );
                        rop.sendMessage2("SETENTRY", "GC"              , bufGC    );

                        rop.sendMessage2("SETENTRY", "imageWidth"  , bufImage.getWidth());
                        rop.sendMessage2("SETENTRY", "imageHeight" , bufImage.getHeight());
                        rop.sendMessage2("SETENTRY", "imageType"   , bufImage.getType());

                        rop.sendMessage2("SETENTRY", "currFontStyle"   , currFontStyle);
                        rop.sendMessage2("SETENTRY", "currFontSize"    , currFontSize);

                        rop.sendMessage2("SETENTRY", "currVisible"     , currVisible);
                        rop.sendMessage2("SETENTRY", "currWinUpdate"   , currWinUpdate);    // if false inhibits updates of Frame

                        // current print related settings
                        rop.sendMessage2("SETENTRY", "printScaleToPage" , (printScaleToPage ? "1" : "0") );
                        rop.sendMessage2("SETENTRY", "printPosX"        , printPosX);
                        rop.sendMessage2("SETENTRY", "printPosY"        , printPosY);
                        rop.sendMessage2("SETENTRY", "printScaleX"      , printScaleX);
                        rop.sendMessage2("SETENTRY", "printScaleY"      , printScaleY);

                        // current GraphicConfiguration
                        rop.sendMessage2("SETENTRY", "gc.background" , bufGC.getBackground());
                        rop.sendMessage2("SETENTRY", "gc.color"      , bufGC.getColor()     );
                        rop.sendMessage2("SETENTRY", "gc.composite"  , bufGC.getComposite() );
                        rop.sendMessage2("SETENTRY", "gc.font"       , bufGC.getFont()      );
                        rop.sendMessage2("SETENTRY", "gc.paint"      , bufGC.getPaint()     );
                        rop.sendMessage2("SETENTRY", "gc.stroke"     , bufGC.getStroke()    );

                        // stack and HashMaps
                        rop.sendMessage2("SETENTRY", "gcStack"    , adGCStack            ); // pushGC
                        rop.sendMessage2("SETENTRY", "imageStack" , adImageStack         ); // pushImage
                        rop.sendMessage2("SETENTRY", "colors"     , hmColors             );
                        rop.sendMessage2("SETENTRY", "gradiantPaints", hmGradientPaints  );
                        rop.sendMessage2("SETENTRY", "fonts"      , hmFonts              );
                        rop.sendMessage2("SETENTRY", "strokes"    , hmStrokes            );
                        rop.sendMessage2("SETENTRY", "images"     , hmImages             );

                        if (arrCommand.length==2)   // assign to Rexx variable in RexxContext
                        {
                            String contextVariableName = arrCommand[1];
                            setContextVariable(slot, contextVariableName, rop); // set context variable
                        }
                        return rop;
                    }

                case COLOR:         // "color  [colorNickName [r g b [a]]" query current color or set + define new color
                    {
                        if (arrCommand.length!=1 && arrCommand.length!=2 && arrCommand.length!=5
                                                 && arrCommand.length!=6 )
                        {
                            throw new IllegalArgumentException("this command needs either no, 1, 4 or exactly 5 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        Color color=null;
                        int red, green, blue, alpha;

                        resultValue = bufGC.getColor();  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }

                        String colorNickName = arrCommand[1];   // get color name
                        if (isOR)
                        {
                            canonical=canonical+" "+colorNickName;
                        }
                        if (arrCommand.length==2)
                        {
                            color = hmColors.get(colorNickName.toUpperCase());
                            if (color==null)
                            {
                                String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-2", errMsg );
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            bufGC.setColor(color);
                            // return color;
                            return resultValue;     // return previous color
                        }

                        red   = Integer.parseInt(arrCommand[2]);
                        green = Integer.parseInt(arrCommand[3]);
                        blue  = Integer.parseInt(arrCommand[4]);
                        if (arrCommand.length==6)
                        {
                            alpha = Integer.parseInt(arrCommand[5]);
                            color = new Color(red, green, blue, alpha);
                            if (isOR)
                            {
                                canonical = canonical + " " + red + " " + green + " " + blue + " " + alpha;
                            }
                        }
                        else
                        {
                            color = new Color(red, green, blue);
                            if (isOR)
                            {
                                canonical = canonical + " " + red + " " + green + " " + blue;
                            }
                        }
                        hmColors.put(colorNickName.toUpperCase(),color);
                        bufGC.setColor(color);
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        // return color;
                        return resultValue;     // return previous color
                    }

                case GRADIENT_PAINT:         // "gradientPaint paintNickName [x1 y1 colorName1 x2 y2 colorName2 [cyclic]]": sets or defines and sets gradientPaint
                    {
                        int argNum = arrCommand.length;
                        if (argNum!=2 && argNum!=8 && argNum!=9)
                        {
                            throw new IllegalArgumentException("this command needs either one, 7 or 8 arguments, received "+(argNum-1)+" instead");
                        }

                        resultValue = bufGC.getPaint();  // query current value (to be returned if change occurs)

                        String paintNickName = arrCommand[1];   // get nick name
                        GradientPaint gradientPaint=null;

                        if (isOR)
                        {
                            canonical=canonical+" "+paintNickName;
                        }
                        if (arrCommand.length==2)   // fetch from cache and setPaint()
                        {
                            gradientPaint = hmGradientPaints.get(paintNickName.toUpperCase());
                            if (gradientPaint==null)
                            {
                                String errMsg="gradientPaint with the supplied nickname \""+paintNickName+"\" is not registered";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-3", errMsg );
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            bufGC.setPaint(gradientPaint);
                            // return gradientPaint;
                            return resultValue;
                        }

                        Color color1=null, color2=null;
                        float x1, y1, x2, y2;
                        x1   = Float.parseFloat(arrCommand[2]);
                        y1   = Float.parseFloat(arrCommand[3]);

                        String colorNickName1 = arrCommand[4];
                        color1 = hmColors.get(colorNickName1.toUpperCase());
                        if (color1==null)
                        {
                            String errMsg="argument colorName1 with the supplied nickname \""+colorNickName1+"\" is not registered";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-4", errMsg );
                        }

                        x2   = Float.parseFloat(arrCommand[5]);
                        y2   = Float.parseFloat(arrCommand[6]);
                        String colorNickName2 = arrCommand[7];
                        color2 = hmColors.get(colorNickName2.toUpperCase());
                        if (color2==null)
                        {
                            String errMsg="argument colorName2 with the supplied nickname \""+colorNickName2+"\" is not registered";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (nickname2 argument): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-5", errMsg );
                        }
                        if (isOR)
                        {
                            canonical=canonical+" "+x1+" "+y1+" "+colorNickName1+x2+" "+y2+" "+colorNickName2;
                        }

                        if (argNum==8)
                        {
                            gradientPaint = new GradientPaint(x1, y1, color1, x2, y2, color2);
                        }
                        else
                        {
                            boolean cyclic=false;
                            String newValue = arrCommand[8];
                            if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                            {
                                cyclic=getBooleanValue(newValue);  // get value
                            }
                            else
                            {
                                throw new IllegalArgumentException("the supplied \"cyclic\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                      + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                            }

                            if (isOR)
                            {
                                canonical=canonical+" "+(cyclic ? "1" : "0");
                            }

                            gradientPaint = new GradientPaint(x1, y1, color1, x2, y2, color2, cyclic);
                        }

                        hmGradientPaints.put(paintNickName.toUpperCase(),gradientPaint);
                        bufGC.setPaint(gradientPaint);
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        // return gradientPaint;
                        return resultValue;
                    }

                case SET_PAINT_MODE:    // "setPaintMode", cf. Graphics#setPaintMode
                    {
                        if (arrCommand.length==1)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            bufGC.setPaintMode();   // invoke method
                            return null;
                        }
                        throw new IllegalArgumentException("this command needs exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                    }

// 2022-09-23: nice gradient samples: <http://www.java2s.com/Code/Java/2D-Graphics-GUI/GradientPaintdemo.htm>
                case PAINT:         // "paint [colorNickName|gradientPaintNickName]" query or set paint to a color or a gradientPaint
                    {
                        resultValue = bufGC.getPaint();  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }
                        if (arrCommand.length==2)
                        {
                            String nickName = arrCommand[1];   // get color name
                            canonical=canonical+" "+nickName;
                            Paint paint=null;
                            paint=hmGradientPaints.get(nickName.toUpperCase()); // first consult gradientPaints
                            if (paint==null)    // now try the registered colors
                            {
                                paint=hmColors.get(nickName.toUpperCase());
                            }
                            if (paint==null)    // neither found
                            {
                                String errMsg="neither paint nor gradientPaint with the supplied nickname \""+nickName+"\" is registered";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-6", errMsg );
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            bufGC.setPaint(paint);
                            // return paint;
                            return resultValue;
                        }
                        throw new IllegalArgumentException("this command needs either no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                    }

                case BACKGROUND:     // "background  [colorNickName]" query current background color or set
                    {
                        resultValue = bufGC.getBackground();  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)   // return current setting (via RC)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }
                        if (arrCommand.length==2)
                        {
                            String colorNickName = arrCommand[1];   // get color name
                            canonical=canonical+" "+colorNickName;
                            Color  color=hmColors.get(colorNickName.toUpperCase());
                            if (color==null)
                            {
                                String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-7", errMsg );
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            bufGC.setBackground(color);
                            // return color;
                            return resultValue;
                        }
                        throw new IllegalArgumentException("this command needs either no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                    }

                case XOR_MODE:  // "xorMode colorNickName": set XORMode to color
                    {
                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }
                        String colorNickName = arrCommand[1];   // get color name
                        canonical=canonical+" "+colorNickName;
                        Color  color=hmColors.get(colorNickName.toUpperCase());
                        if (color==null)
                        {
                            String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-8", errMsg );
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        bufGC.setXORMode(color);
                        return null;
                    }

                case COMPOSITE:     // "composite [rule [alpha]]": query current or get and set new
                    {
                        int argNum=arrCommand.length;
                        if (argNum>3)
                        {
                            throw new IllegalArgumentException("this command needs either no, one (rule) or two (rule alpha) arguments, received "+(argNum-1)+" instead");
                        }
                        resultValue = bufGC.getComposite();    // query current value (to be returned if change occurs)

                        if (argNum==1)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }

                        String ruleName   = arrCommand[1].toUpperCase();   // get name
                        int    intRule    = -1;
                        if (isOR)
                        {
                            canonical=canonical+" "+ruleName;
                        }

                        if (startRexxVariableChar.indexOf(ruleName.charAt(0))>=0)    // a symbolic name?
                        {
                            if (!alphaComposites.containsKey(ruleName))
                            {
                                throw new IllegalArgumentException("unknown value for \"rule\" argument");
                            }
                            intRule=alphaComposites.get(ruleName);
                        }
                        else // verbatim int type
                        {
                            int val=Integer.parseInt(ruleName);
                            if (!alphaComposites.containsValue(val))
                            {
                                throw new IllegalArgumentException("unknown value for \"rule\" argument");
                            }
                            intRule=alphaComposites.get(ruleName);
                        }

                        AlphaComposite ac = null;
                        if (argNum==3)
                        {
                            float alpha = Float.parseFloat(arrCommand[2]);
                            if (isOR)
                            {
                                canonical=canonical+" "+alpha;
                            }
                            ac=AlphaComposite.getInstance(intRule,alpha);
                        }
                        else
                        {
                            ac=AlphaComposite.getInstance(intRule);
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        bufGC.setComposite(ac);
                        // return ac;
                        return resultValue;
                    }

// 2022-09-29 -->
/* --
                case CLIP_NEW:      //  ?? "clipNew x y w h" -> setClip(x y w h)
                    {
                        if (arrCommand.length!=5)
                        {
                            throw new IllegalArgumentException("this command expects exactly 4 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int newX      = Integer.parseInt(arrCommand[1]);
                        int newY      = Integer.parseInt(arrCommand[2]);
                        int newWidth  = Integer.parseInt(arrCommand[3]);
                        int newHeight = Integer.parseInt(arrCommand[4]);

                            // no exception, so args o.k.
                        String res = newX+" "+newY+" "+newWidth+" "+newHeight;
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+res);
                        }
                        bufGC.setClip(newX, newY, newWidth, newHeight);
                        return res;
                    }
-- */

                case CLIP_REMOVE:   //  "removeClip" -> setClip(null)
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command expects no arguments, received "+(arrCommand.length-1)+" instead");
                        }
                            // no exception, so args o.k.
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        bufGC.setClip(null);    // remove clip
                        return null;
                    }

                case CLIP:          // query or set "clip [x y w h]" -> get (getClipBounds()) or clipRect(x y w h): returns .nil if no clip set
                    {
                        if (arrCommand.length!=1 && arrCommand.length!=5)
                        {
                            throw new IllegalArgumentException("this command expects no or exactly 4 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        resultValue=null;

                        Rectangle r = bufGC.getClipBounds();
                        if (r!=null)
                        {
                            resultValue = r.x+" "+r.y+" "+r.width+" "+r.height;
                        }
                        else    // indicate no clip
                        {
                            resultValue = getNil(slot);
                        }

                        if (arrCommand.length>1)    // clip area supplied
                        {
                            int newX      = Integer.parseInt(arrCommand[1]);
                            int newY      = Integer.parseInt(arrCommand[2]);
                            int newWidth  = Integer.parseInt(arrCommand[3]);
                            int newHeight = Integer.parseInt(arrCommand[4]);
                            resultValue = newX+" "+newY+" "+newWidth+" "+newHeight;
                            canonical = canonical+" "+resultValue;
                            bufGC.clipRect(newX, newY, newWidth, newHeight);
                        }
                            // no exception, so args o.k.
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }
// <---

                case DRAW_POLYLINE: // "drawPolyline []xPoints []yPoints nPoints
                case DRAW_POLYGON:  // "drawPolygon  []xPoints []yPoints nPoints
                case FILL_POLYGON:  //  "fillPolygon  []xPoints []yPoints nPoints
                    {
                        if (arrCommand.length!=4)
                        {
                            throw new IllegalArgumentException("this command needs exactly 3 (xPointsArray yPointsArray nPoints) arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        int [] xPoints=null, yPoints=null;
                        int    nPoints=0;

                        String strXPoints=arrCommand[1];
                        if (startRexxVariableChar.indexOf(strXPoints.charAt(0))>=0) // a Rexx variable?
                        {
                            xPoints=(int []) getContextVariable(slot, strXPoints);
                        }
                        else    // a Rexx array expression: comma separated list of ints in parentheses (no blanks!)
                        {
                            xPoints=RexxArrayExpressionToIntArray(strXPoints,0);
                        }

                        String strYPoints=arrCommand[2];
                        if (startRexxVariableChar.indexOf(strYPoints.charAt(0))>=0) // a Rexx variable?
                        {
                            yPoints=(int []) getContextVariable(slot, strYPoints);
                        }
                        else    // a Rexx array expression: comma separated list of floats in parentheses (no blanks!)
                        {
                            yPoints=RexxArrayExpressionToIntArray(strYPoints,0);
                        }

                        String strNPoints=arrCommand[3];
                        nPoints = Integer.parseInt(strNPoints);

// System.err.println("strXPoints=["+strXPoints+"] strYPoints=["+strYPoints+"] strNPoints=["+strNPoints+"]");
// System.err.println("\txPoints: "+Arrays.deepToString(Arrays.asList(xPoints).toArray()) );
// System.err.println("\tyPoints: "+Arrays.deepToString(Arrays.asList(yPoints).toArray()) );
// System.err.println("\tnPoints: ["+nPoints+"]");
                        if (isOR)
                        {
                            canonical=canonical+" "+strXPoints+" "+strYPoints+" "+strNPoints;
                            writeOutput(slot, canonical);
                        }
// System.err.println("\tcanonical: "+canonical+" "+strXPoints+" "+strYPoints+" "+strNPoints+"\n");
// System.err.println("... cmd: "+cmd);

                        switch (cmd)
                        {
                            case DRAW_POLYLINE:
                                bufGC.drawPolyline(xPoints, yPoints, nPoints);
                                break;
                            case DRAW_POLYGON:
                                bufGC.drawPolygon(xPoints, yPoints, nPoints);
                                break;
                            case FILL_POLYGON:
                                bufGC.fillPolygon(xPoints, yPoints, nPoints);
                                break;
                        }
                        break;
                    }

                case STROKE:        // "stroke [strokeNickName [width [cap join [miterlimit [floatDashArray floatDash] ]]]" query or set stroke
                    {
                        int argNum=arrCommand.length;
                        if (argNum==4 || argNum>8)
                        {
                            throw new IllegalArgumentException("this command needs either no, 2 (strokeNickName width), 4 (strokeNickName width cap join), 4 or 6 arguments (strokeNickName width cap join miterlimit arrDashes dashPhase), received "+(argNum-1)+" instead");
                        }

                        resultValue = bufGC.getStroke();  // query current value (to be returned if change occurs)

                        if (argNum==1)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }

                        String strokeNickName = arrCommand[1];   // get name
                        if (isOR)
                        {
                            canonical=canonical+" "+strokeNickName;
                        }
                        Stroke stroke=null;
                        if (argNum==2)      // fetch stored stroke
                        {
                            stroke = hmStrokes.get(strokeNickName.toUpperCase());
                            if (stroke==null)
                            {
                                String errMsg="stroke with the supplied nickname \""+strokeNickName+"\" is not registered";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-9", errMsg );
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            bufGC.setStroke(stroke);
                            // return stroke;
                            return resultValue;
                        }

                        int width, cap, join; // , miterlimit;
                        float miterlimit;
                        width = Integer.parseInt(arrCommand[2]);
                        if (isOR)
                        {
                            canonical=canonical+" "+width;
                        }
                        if (argNum==3)
                        {
                            stroke = new BasicStroke(width);
                        }
                        else
                        {
                            // CAP
                            String tmpCap = arrCommand[3].toUpperCase();
                            if (startRexxVariableChar.indexOf(tmpCap.charAt(0))>=0)    // a symbolic name?
                            {
                                if (!imageTypes.containsKey(tmpCap))
                                {
                                    throw new IllegalArgumentException("unknown value for \"cap\" argument");
                                }
                                cap=endCaps.get(tmpCap);
                            }
                            else // verbatim int type
                            {
                                cap=Integer.parseInt(tmpCap);
                                if (!endCaps.containsValue(cap))
                                {
                                    throw new IllegalArgumentException("unknown value for \"cap\" argument");
                                }
                            }

                            // JOIN
                            String tmpJoin = arrCommand[4].toUpperCase();
                            if (startRexxVariableChar.indexOf(tmpJoin.charAt(0))>=0)    // a symbolic name?
                            {
                                if (!imageTypes.containsKey(tmpJoin))
                                {
                                    throw new IllegalArgumentException("unknown value for \"join\" argument");
                                }
                                join=lineJoins.get(tmpJoin);
                            }
                            else // verbatim int type
                            {
                                join=Integer.parseInt(tmpJoin);
                                if (! lineJoins.containsValue(cap))
                                {
                                    throw new IllegalArgumentException("unknown value for \"join\" argument");
                                }
                            }
                            if (isOR)
                            {
                                canonical=canonical+" "+tmpCap+" "+tmpJoin;
                            }
                            if (argNum==5)
                            {
                                stroke = new BasicStroke(width, cap, join);
                            }
                            else
                            {
                                miterlimit = Float.parseFloat(arrCommand[5]);
                                if (isOR)
                                {
                                    canonical=canonical+" "+miterlimit;
                                }
                                float [] dash=null;
                                float dashPhase;
                                if (argNum>6) // dash float array and dashPhase
                                {
                                    String strDash=arrCommand[6];
                                    if (startRexxVariableChar.indexOf(strDash.charAt(0))>=0) // a Rexx variable?
                                    {
                                        dash=(float []) getContextVariable(slot, strDash);
                                    }
                                    else    // a Rexx array expression: comma separated list of floats in parentheses (no blanks!)
                                    {
                                        dash=RexxArrayExpressionToFloatArray(strDash,0);
                                    }

                                    String strDashPhase=arrCommand[7];
                                    dashPhase=Float.parseFloat(strDashPhase);
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+strDash+" "+strDashPhase;
                                    }
                                    stroke = new BasicStroke(width, cap, join, miterlimit, dash, dashPhase);
                                }
                                else
                                {
                                    stroke = new BasicStroke(width, cap, join, miterlimit);
                                }
                            }
                        }
                        hmStrokes.put(strokeNickName.toUpperCase(),stroke);
                        bufGC.setStroke(stroke);
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        // return stroke;
                        return resultValue;
                    }

                case FONT_STYLE:    // "fontStyle [0=PLAIN | 1=BOLD | 2=ITALIC | 3=BOLD+ITALIC]
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (fontStyle), received "+(arrCommand.length-1)+" instead");
                        }

                        resultValue = "" + currFontStyle;   // query current value (to be returned if change occurs)

                        if (arrCommand.length==2)
                        {
                            int newFontStyle = Integer.parseInt(arrCommand[1]);
                                // no exception, so args o.k.
                            if (newFontStyle<0 || newFontStyle>3)
                            {
                                throw new IllegalArgumentException("illegal fontStyle ["+newFontStyle+"]: must be one of 0 (PLAIN), 1 (BOLD), 2 (ITALIC) or 3 (BOLD+ITALIC)");
                            }
                            currFontStyle = newFontStyle;
                            if (isOR)
                            {
                                canonical=canonical+" "+newFontStyle;
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }

                case FONT_SIZE:     // "fontSize [size]"
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (fontSize), received "+(arrCommand.length-1)+" instead");
                        }

                        resultValue = "" + currFontSize;   // query current value (to be returned if change occurs)

                        if (arrCommand.length==2)
                        {
                            int newFontSize = Integer.parseInt(arrCommand[1]);
                                // no exception, so args o.k.
                            if (newFontSize<=0)
                            {
                                throw new IllegalArgumentException("illegal fontSize ["+newFontSize+"]: must not be 0 or less.");
                            }
                            currFontSize = newFontSize;
                            if (isOR)
                            {
                                canonical=canonical+" "+newFontSize;
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }

                case FONT:          // "font   [fontNickName [name]" query or set font;  uses currFontStyle, currFontSize; "Dialog", "DialogInput", "Serif", "SansSerif", "Monospaced"
                    {
                        resultValue = bufGC.getFont();   // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }
                        Font font=null;
                        String fontNickName = arrCommand[1];   // get font name
                        if (isOR)
                        {
                            canonical = canonical + " " + fontNickName;
                        }
                        if (arrCommand.length==2)
                        {
                            font = hmFonts.get(fontNickName.toUpperCase());
                            if (font==null)
                            {
                                String errMsg="font with the supplied nickname \""+fontNickName+"\" is not registered";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-10", errMsg );
                            }
                            bufGC.setFont(font);
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            // return font;
                            return resultValue;
                        }

                        // o.k. Font name may contain blanks so use start of third word and all what remains
                        String fontName = command.substring(alWordBoundaries.get(2)[0]);
                        font = new Font(fontName, currFontStyle, currFontSize);
                        if (font==null)
                        {
                            String errMsg="font \""+fontName+"\" could not be loaded";
                            if (isOR)
                            {
                                writeOutput(slot, "-- ERROR (fontname argument): ["+command+"]");
                            }
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-11", errMsg );
                        }
                        hmFonts.put(fontNickName.toUpperCase(),font);
                        bufGC.setFont(font);
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        // return font;
                        return resultValue;
                    }

                case RESET:     // "reset" synonym: "clear", clears everything, resets
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command does not take any arguments");
                        }
                        reset();        // reset all current variables and caches
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return null;
                    }

                    // ----------------------

                case POS:       // query current coordinates or goto x, y co-ordinate
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs no, 1 or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        resultValue = currX+" "+currY;   // query current value (to be returned if change occurs)
                        if (arrCommand.length>1)
                        {
                            int newX = Integer.parseInt(arrCommand[1]);
                            int newY = newX;    // default to newX in case Y is not supplied
                            if (arrCommand.length==3)
                            {
                                newY = Integer.parseInt(arrCommand[2]);
                            }
                                // no exception, so args o.k.
                            currX = newX;
                            currY = newY;
                            if (isOR)
                            {
                                canonical=canonical+" "+currX+" "+currY;
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        // return currX+" "+currY;
                        return resultValue;
                    }

                case DRAW_LINE:     // "drawLine toX toY"
                    {
                        if (arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int toX = Integer.parseInt(arrCommand[1]);
                        int toY = Integer.parseInt(arrCommand[2]);
                        if (isOR)
                        {
                            canonical=canonical+" "+toX+" "+toY;
                            writeOutput(slot,canonical);
                        }
                            // no exception, so args o.k.
                        bufGC.drawLine(currX,currY,toX,toY);
                    }
                    break;

                case DRAW_STRING:   // "drawString text"
                    {
                        // ArrayList<int[]> al = getWordBoundaries(command);   // parse command return word boundaries
                        ArrayList<int[]> al = alWordBoundaries;
                        if (al.size()<2)
                        {
                            throw new IllegalArgumentException("this command needs 1 argument (the string to be drawn)");
                        }
                            // no exception, so args o.k.
                            // to fetch leading blanks we skip over first blank (unlike for other commands where we ignore all blanks after the command)
                        int [] pos = (int []) al.get(0);
                        String str = command.substring(pos[1]+1); // extract String: skip over first trailing blank
                        if (isOR)
                        {
                            writeOutput(slot,canonical+" "+str);
                        }
                        bufGC.drawString(str,currX,currY);
                    }
                    break;

                case DRAW_OVAL:     // "drawOval width height"
                case FILL_OVAL:
                    {
                        if (arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int width  = Integer.parseInt(arrCommand[1]);
                        int height = Integer.parseInt(arrCommand[2]);
                            // no exception, so args o.k.
                        if (cmd==Command.DRAW_OVAL)
                        {
                            bufGC.drawOval(currX,currY,width,height);
                        }
                        else
                        {
                            bufGC.fillOval(currX,currY,width,height);
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+width+" "+height);
                        }
                    }
                    break;

                case DRAW_ROUND_RECT:    // "drawRoundRect width height arcWidth arcHeight"
                case FILL_ROUND_RECT:    // "fillRoundRect width height arcWidth arcHeight"
                    if (arrCommand.length!=5)
                    {
                        throw new IllegalArgumentException("this command needs exactly 4 arguments, received "+(arrCommand.length-1)+" instead");
                    }
                case DRAW_RECT:          // "drawRect width height"
                case FILL_RECT:          // "fillRect width height"
                case CLEAR_RECT:
                    {
                        boolean isRoundRect = (cmd==Command.DRAW_ROUND_RECT || cmd==Command.FILL_ROUND_RECT );
                        if (!isRoundRect && arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int width  = Integer.parseInt(arrCommand[1]);
                        int height = Integer.parseInt(arrCommand[2]);
                        canonical = canonical+" "+width+" "+height;

                        int arcWidth=-1, arcHeight=-1;
                        if (isRoundRect)
                        {
                            arcWidth  = Integer.parseInt(arrCommand[3]);
                            arcHeight = Integer.parseInt(arrCommand[4]);
                            canonical = canonical+" "+arcWidth+" "+arcHeight;
                        }
                            // no exception, so args o.k.
                        switch (cmd)
                        {
                            case DRAW_RECT:
                                bufGC.drawRect(currX,currY,width,height);
                                break;
                            case FILL_RECT:
                                bufGC.fillRect(currX,currY,width,height);
                                break;
                            case CLEAR_RECT:
                                bufGC.clearRect(currX,currY,width,height);
                                break;
                            case DRAW_ROUND_RECT:    // "drawRoundRect width height arcWidth arcHeight"
                                bufGC.drawRoundRect(currX,currY,width,height,arcWidth,arcHeight);
                                break;
                            case FILL_ROUND_RECT:    // "fillRoundRect width height arcWidth arcHeight"
                                bufGC.fillRoundRect(currX,currY,width,height,arcWidth,arcHeight);
                                break;
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                    }
                    break;

                case DRAW_3D_RECT:  // "draw3DRect width height raised"
                case FILL_3D_RECT:  // "fill3DRect width height raised"
                    {
                        if (arrCommand.length!=4)
                        {
                            throw new IllegalArgumentException("this command needs exactly 3 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int width  = Integer.parseInt(arrCommand[1]);
                        int height = Integer.parseInt(arrCommand[2]);
                        String strRaised = arrCommand[3];
                        boolean raised=false;
                        if (checkBooleanValue(strRaised))   // a valid BSF4ooRexx850 boolean value?
                        {
                            raised=getBooleanValue(strRaised);  // get value
                        }
                        else
                        {
                            throw new IllegalArgumentException("the supplied \"raised\" argument \""+strRaised+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }
                            // no exception, so args o.k.
                        if (cmd==Command.DRAW_RECT)
                        {
                            bufGC.draw3DRect(currX,currY,width,height,raised);
                        }
                        else
                        {
                            bufGC.fill3DRect(currX,currY,width,height,raised);
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+width+" "+height+" "+raised);
                        }
                    }
                    break;

                case DRAW_ARC:  //   "drawArc  width height startAngle arcAngle"
                case FILL_ARC:  //   "fillArc  width height startAngle arcAngle"
                    {
                        if (arrCommand.length!=5)
                        {
                            throw new IllegalArgumentException("this command needs exactly 4 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int width      = Integer.parseInt(arrCommand[1]);
                        int height     = Integer.parseInt(arrCommand[2]);
                        int startAngle = Integer.parseInt(arrCommand[3]);
                        int arcAngle   = Integer.parseInt(arrCommand[4]);
                            // no exception, so args o.k.
                        if (cmd==Command.DRAW_ARC)
                        {
                            bufGC.drawArc(currX,currY,width,height,startAngle,arcAngle);
                        }
                        else
                        {
                            bufGC.fillArc(currX,currY,width,height,startAngle,arcAngle);
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+width+" "+height+" "+startAngle+" "+arcAngle);
                        }
                    }
                    break;

                case LOAD_IMAGE:    // "load nickName filename", loads an image -> returns its dimension
                    {
                        // ArrayList<int[]> al = getWordBoundaries(command);   // parse command return word boundaries
                        ArrayList<int[]> al = alWordBoundaries;
                        int size = al.size();
                        if (size<3)
                        {
                            throw new IllegalArgumentException("this command needs 2 arguments (imageNickName filename), received "+(size-1)+" instead");
                        }
                        String nickName = arrCommand[1];

                        int pos[] = (int []) al.get(2);     // filenames may contain blanks, use everything left
                        String fileName = (String) command.substring(pos[0]); // filename may contain blanks, allow for them
                        File file = new File(fileName);
                        BufferedImage img = ImageIO.read(file);
                        hmImages.put(nickName.toUpperCase(),img);   // save in HashMap
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+nickName+" "+fileName);
                        }
                        // return img;
                        return img.getWidth()+" "+img.getHeight();
                    }

                case GET_IMAGE:    // "getImage [nickName]" returns image or nickName-stored image
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (image's nickname for lookup), received "+(arrCommand.length-1)+" instead");
                        }

                        if (arrCommand.length==1)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return bufImage;
                        }

                        String nickName = arrCommand[1];        // get  nickname
                        BufferedImage img = hmImages.get(nickName.toUpperCase());
                        if (img==null)
                        {
                            throw new IllegalArgumentException("image with the name \""+nickName+"\" not found, you must first use \"loadImage "+nickName+" filename\"");
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+nickName);
                        }
                        return img;
                    }

                case GET_GC:    // "getGC"    returns current GC (GraphicsContext)
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command must not have an argument, received "+(arrCommand.length-1)+" instead");
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return bufGC;
                    }

                case SAVE_IMAGE:  // "save filename", saves current image to file
                    {
                        // ArrayList<int[]> al = getWordBoundaries(command);   // parse command return word boundaries
                        ArrayList<int[]> al = alWordBoundaries;
                        int size = al.size();
                        if (size<2)
                        {
                            throw new IllegalArgumentException("this command needs 1 argument (filename)");
                        }

                        int pos[] = (int []) al.get(1);
                        String fileName = (String) command.substring(pos[0]);   // filename may contain blanks, allow for them
                        String extension = "png";   // default to PNG
                        int lastPos = fileName.lastIndexOf('.');
                        if (lastPos>1 && lastPos<fileName.length())    // extract file type
                        {
                            String ext = fileName.substring(lastPos+1);
                            if (ext.length()>0)
                            {
                                extension = ext;    // use filename's extension as type
                            }
                        }
                        File file = new File(fileName);
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+fileName);
                        }
                        boolean writeSuccess=ImageIO.write(bufImage, extension, file);
                        if (!writeSuccess)
                        {
                            throw new RuntimeException("\""+canonical+"\": fileName=["+fileName+"] hence extension=["+extension+"] failed, try extension \"png\" instead");
                        }
                        return (writeSuccess ? "1" : "0");
                    }

                case ROTATE:       // "rotate [theta [x y]]" query or set rotation
                    {
                        ArrayList<int[]> al = alWordBoundaries;
                        int argNum = al.size();

                        resultValue = ""+currAngle;   // query current value (to be returned if change occurs)

                        if (argNum==1)    // query currAngle
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }

                        if (argNum!=2 && argNum!=4)
                        {
                            throw new IllegalArgumentException("this command needs no, one or exactly three arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        double theta = 0, radiansTheta = 0;
                        if (argNum>1) // at least theta given
                        {
                            theta = Double.parseDouble(arrCommand[1]);
                            if (isOR)
                            {
                                canonical=canonical+" "+theta;
                            }
                            currAngle = (currAngle + theta) % 360;
                            radiansTheta = Math.toRadians(theta);  // turn degree to radians
                            if (radiansTheta!=0)
                            {
                                if (argNum==2)    // just rotate by theta
                                {
                                    bufGC.rotate(radiansTheta);
                                }
                                else    // if 3 arguments, fetch arg 2 (x) and 3 (y)
                                {
                                    int x = Integer.parseInt(arrCommand[2]);
                                    int y = Integer.parseInt(arrCommand[3]);
                                    bufGC.rotate(radiansTheta, x, y);
                                }
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+theta);
                        }
                        // return ""+theta;
                        return resultValue;
                    }


                    // rgf, 2022-10-21: command to allow drawing an image referred to by a rexx Variable
                case DRAW_IMAGE_FROM_REXX_VAR:   // "drawImageFromVar rexxVarName [bkgColor] | rexxVarName width height [bkgColor] | rexxVarName width height srcX1 srcY1 srcX2 srcY2 [bkgColor]"
                case DRAW_IMAGE:   // "drawImage nickName [bkgColor] | nickName width height [bkgColor] | nickName width height srcX1 srcY1 srcX2 srcY2 [bkgColor]"
                    {
                        int argNum = arrCommand.length;
                        if (argNum!=2 && argNum!=3 && argNum!=4 && argNum!=5 && argNum!= 8 && argNum!=9)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2, 3, 4, 5, 8 or 9 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        String name = arrCommand[1];
                        if (isOR)
                        {
                            canonical=canonical+" "+name;
                        }

                           // get Image
                        Image img = null;
                        if (cmd==Command.DRAW_IMAGE)   // fetch from the image registry
                        {
                            img = hmImages.get(name.toUpperCase());
                            if (img==null)
                            {
                                throw new IllegalArgumentException("image with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\" or \"pushImage "+name+"\"");
                            }
                        }
                        else                            // fetch from the denoted Rexx variable
                        {
                            img = (Image) getContextVariable(slot, name);
                            if (img==null)
                            {
                                throw new IllegalArgumentException("Rexx variable named \""+name+"\" does not refer to an image");
                            }
                        }

                        int width=0, height=0, srcX1=0, srcY1=0, srcX2=0, srcY2=0;
                        String colorNickName=null;
                        Color bkgColor=null;
                        Object [] args=null;
                        switch (argNum)       // fetch color if supplied
                        {
                            case 3: // bkgColor
                                colorNickName=arrCommand[2];
                                break;
                            case 5: // bkgColor
                                colorNickName=arrCommand[4];
                                break;
                            case 9: // bkgColor
                                colorNickName=arrCommand[8];
                                break;
                        }
                        if (colorNickName!=null)    // fetch the color
                        {
                            bkgColor = hmColors.get(colorNickName.toUpperCase());
                            if (bkgColor==null)
                            {
                                String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-12", errMsg );
                            }
                        }
                        switch (argNum)
                        {
                            case 2:
                                bufGC.drawImage(img,currX,currY, null);  // use the ImageObserver signature
                                break;
                            case 3: // bkgColor
                                bufGC.drawImage(img,currX,currY, bkgColor, null);  // use the ImageObserver signature
                                if (isOR)
                                {
                                    canonical=canonical+colorNickName;
                                }
                                break;

                            case 4:
                            case 5: // bkgColor
                            case 8:
                            case 9: // bkgColor
                                width = Integer.parseInt(arrCommand[2]);
                                height= Integer.parseInt(arrCommand[3]);
                                if (argNum>5)
                                {
                                    srcX1 = Integer.parseInt(arrCommand[4]);
                                    srcY1 = Integer.parseInt(arrCommand[5]);
                                    srcX2 = Integer.parseInt(arrCommand[6]);
                                    srcY2 = Integer.parseInt(arrCommand[7]);
                                }
                                if (isOR)
                                {
                                    canonical=canonical+" "+width+" "+height;
                                    if (argNum==5)        // bkgColor
                                    {
                                        canonical=canonical+" "+colorNickName;
                                    }
                                    else if (argNum>5)
                                    {
                                        canonical=canonical+" "+srcX1+" "+srcY1+" "+srcX2+" "+srcY2;;
                                        if (argNum==9)    // bkgColor
                                        {
                                            canonical=canonical+" "+colorNickName;
                                        }
                                    }
                                }
                                if (argNum==4)
                                {
                                    bufGC.drawImage(img,currX,currY, width, height, null);  // use the ImageObserver signature
                                }
                                else if (argNum==5)       // bkgColor
                                {
                                    bufGC.drawImage(img,currX,currY, width, height, bkgColor, null);  // use the ImageObserver signature
                                }
                                else if (argNum==8)
                                {
                                    bufGC.drawImage(img,currX,currY,width,height,srcX1,srcY1,srcX2,srcY2, null);  // use the ImageObserver signature
                                }
                                else                    // bkgColor
                                {
                                    bufGC.drawImage(img,currX,currY,width,height,srcX1,srcY1,srcX2,srcY2, bkgColor,null);  // use the ImageObserver signature
                                }
                                break;
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }

                case TRANSLATE: // "translate x [y]" move origin; if y omitted, uses x
                    {
                        if (arrCommand.length<2 && arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs 1 or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        Integer newX = Integer.parseInt(arrCommand[1]);
                        Integer newY = newX;    // default to newX
                        if (arrCommand.length==3)
                        {
                            newY = Integer.parseInt(arrCommand[2]);
                        }
                            // no exception, so args o.k.
                        bufGC.translate(newX,newY);
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+newX+" "+newY);
                        }
                        // return newX+" "+newY;
                        return null;        // RC="0", command executed alright
                    }

                case PUSH_GC:   // pushes current GC, does a create() & assigns the new one as current
                case POP_GC:    // pops and & assigns as current
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command does not expect arguments, received "+(arrCommand.length-1)+" argument(s)");
                        }

                        resultValue = bufGC;   // query current value (to be returned if change occurs)

                        if (cmd==Command.PUSH_GC)
                        {
                            Graphics2D tmpG2D = bufGC;
                            adGCStack.push(bufGC);
                            bufGC=(Graphics2D) bufGC.create();  // work on an independent copy from now on
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            // return tmpG2D;
                        }
                        else    // POP_GC
                        {
                            Graphics2D d2d=null;
                            if (adGCStack.size()>0)
                            {
                                d2d=adGCStack.pop();
                            }
                            if (d2d==null)
                            {
                                String errMsg="did not return a GC (stack empty)";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (stack empty): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-13", errMsg );
                            }
                            bufGC.dispose();        // dispose current GC
                            bufGC=d2d;              // replace with previous GC
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            // return bufGC;
                            resultValue=bufGC;  // old one is disposed, so cannot use that anymore
                        }
                        return resultValue;
                    }

                    // PUSH_IMAGE: "pushImage [nickName]" push current image displayed, if nickName store it in image HashMap as well
                case PUSH_IMAGE:   // pushes current one, does a create() & assigns the new one as current
                case POP_IMAGE:    // pops and & assigns as current
                    {
                        if ( cmd==Command.POP_IMAGE && arrCommand.length!=1 )
                        {
                            throw new IllegalArgumentException("this command does not expect arguments, received "+(arrCommand.length-1)+" argument(s)");
                        }
                        else if ( cmd==Command.PUSH_IMAGE && arrCommand.length>2 )
                        {
                            throw new IllegalArgumentException("this command expects 1 argument at the most (nickName), received "+(arrCommand.length-1)+" argument(s)");
                        }


                        if (cmd==Command.PUSH_IMAGE)
                        {
                            BufferedImage img = copyImage(bufImage);
                            adImageStack.push(img); // save current image
                            if (arrCommand.length>1)    // nickName supplied, store image in HashMap
                            {
                                String nickName = arrCommand[1];
                                hmImages.put(nickName.toUpperCase(),img);   // save in HashMap
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return img;     // return pushed (copy) image
                            // return resultValue;
                        }
                        else    // POP_IMAGE
                        {
                            BufferedImage img=null;
                            if (adImageStack.size()>0)
                            {
                                img=adImageStack.pop();   // get image from stack
                            }
                            if (img==null)                          // nothing left on stack?
                            {
                                String errMsg="did not return an image (empty stack)";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (empty stack): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-14", errMsg );
                            }
                            bufGC.drawImage(img,0,0,null);
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            resultValue = img;     // return popped img, not the draw result
                            break;  // update UI if shown
                        }
                    }


                default:        // unknown/unhandled command, raise failure condition (should never arrive here)
                    if (isOR)
                    {
                        writeOutput(slot, "-- FAILURE (unimplemented command): ["+command+"]");
                    }
                    return createCondition (slot, nrCommand, command, ConditionType.FAILURE, "-17", "unimplemented command");
            }
        }
        catch (Throwable t)     // raise error condition if args etc. were causing exceptions
        {
            if (isOR)
            {
                writeOutput(slot, "-- ERROR (argument): ["+command+"]");
            }
            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-16", t.toString() );
        }

        // draws/changes have occurred, redraw if we have a JavaDrawingFrame instance to show to user
        if (jd!=null && currWinUpdate)
        {
            jd.sf.repaint();  // trigger a paintComponent() invocation
        }
        // return null;
        return resultValue;
    }

    private enum ConditionType
    {
        ERROR,
        FAILURE
    };

    /** Create and raise condition in a uniform way. Includes the line number
     *  the name of the executable and the type of the executable.
     *
     *  @param scope
     *  @param nrCommand number of command
     *  @param command in error or failure
     *  @param errCategory 0 raises an error condition, failure condition else
     *  @param rc return code to return
     *  @param errMsg error message to be supplied
     *
     */
    String createCondition (Object slot, int nrCommand, String command, ConditionType errCategory, String rc, String errMsg)
    {
        try
        {
            RexxProxy context = (RexxProxy) getCallerContext(slot);         // get .context
            String   strLineNr  = (String) context.sendMessage0("LINE");    // get line nr

            RexxProxy executable = (RexxProxy) context.sendMessage0("executable");  // get object
            String strExecutable = (String) ((RexxProxy) executable.sendMessage0("CLASS")) .sendMessage0("ID"); // get class name

            String strName       = (String) context.sendMessage0("NAME");   // get class name

            String strErrCategory = "ERROR";        // default to "ERROR"
            if (errCategory==ConditionType.FAILURE) // failure!
            {
                strErrCategory   = "FAILURE";
            }
            String workErrMsg = String.format("%-7s %3s command # %3d [%s] line # %s in %s [%s] cause: [%s]",
                                               strErrCategory,
                                               rc,
                                               nrCommand,
                                               command,
                                               strLineNr,
                                               strExecutable.toLowerCase(),
                                               getFileName(strName),
                                               errMsg
                                             );
            if (isErrorRedirected(slot))
            {
                writeError(slot, workErrMsg);
            }
            Object [] additional = new Object[]{workErrMsg,command,rc,context};
            raiseCondition(slot, strErrCategory, command, additional, rc);
        }
        catch (BSFException bse)    // fallback in case this happens
        {
            String workErrMsg = String.format("%s-8s %4s: unexpected exception [%s] thrown in Java handler's createCondition(): %s rc=[%s] for command # [%3n] command=[%s] errMsg=[%s]",
                                              "FAILURE",
                                              "-99",
                                              bse.toString(),
                                              errCategory==ConditionType.ERROR ? "ERROR" : "FAILURE",
                                              rc,
                                              nrCommand,
                                              command,
                                              errMsg
                                            );
            if (isErrorRedirected(slot))
            {
                writeError(slot, errMsg);
            }
            raiseCondition(slot, "FAILURE", command, new Object[]{workErrMsg}, rc);
        }
        return "-99";
    }

    /** Extract filename and return it according to the operating system. Condition object will
     *  have the fully qualified file name in the additional array.
     *
     * @param name file name, possibly with full path
     * @return filename after last file separator character
     */
    static String getFileName(String name)
    {
        try // safety belt in case weird names get used
        {
            int lastPos=name.lastIndexOf(System.getProperty("file.separator")); // use system's file separator
            if (lastPos>-1)
            {
                return name.substring(lastPos+1);
            }
        }
        catch (Throwable t) {};
        return name;
    }


    // cf. <https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage> as of 2022-09-03
    static BufferedImage copyImage(BufferedImage src)
    {
        BufferedImage b = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Graphics g = b.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return b;
    }


    /** This will create a word array from the supplied ArrayList returned by getWordBoundaries.
     *
     * @param s String
     * @param al ArrayList returned by getWordBoundaries
     * @return array of words
     */
    public static String[] getNonBlankWords(String s, ArrayList<int[]> al)
    {
    //    ArrayList<int[]> al = getWordBoundaries(s);
        if (al.size()>0)
        {
            String [] arrString = new String[al.size()];
            for (int i=0;i<al.size();i++)
            {
                int[] pos=(int[]) al.get(i);
                arrString[i] = s.substring(pos[0], pos[1]);
            }
            return arrString;
        }
        return new String[0];

    }


    /** Parses the string and returns an ArrayList of int[] denoting the start and the end
     *  of each word in the string delimited by ' ' or '\t' the Rexx whitespace characters.
     *
     * @param s the string to parse
     * @param maxNumberOfWords if -1 processes always entire string, otherwise stops after having
     *                         created maxNumberOfWords items
     * @return an ArrayList of int[] arrays
     */
    public static ArrayList<int[]> getWordBoundaries (String s, int maxNumberOfWords)
    {
        ArrayList <int[]> al = new ArrayList<>();
        if (s==null)
        {
            return al;
        }

        int len=s.length();
        int p=0, start=-1;                   // start and end positions in string
        for (p=0; p<len && maxNumberOfWords!=0; p++)
        {
            char c = s.charAt(p);
            if ( c==' ' || c=='\t' || c=='\n')  // whitespace?
            {
                if (start>=0)   // over a word
                {
                    al.add(new int[]{start,p});
                    start=-1;
                    maxNumberOfWords--;     // one word completed
                }
            }
            else    // not a whitespace
            {
                if (start==-1)  // a new word?
                {
                    start=p;
                }
            }
        }

        if (start>=0)       // pending word
        {
            al.add(new int[]{start,len});
        }
        return al;
    }

    /** Allow "1", ".true", "true" in any case to resolve to a boolean true;
     *  allow "0", ".false", "false" in any case to resolve to a boolean false;
     *
     * @param value the string that represents a BSF4ooRexx850 boolean value
     * @return true if string represents true, false else
     */
    public static boolean checkBooleanValue(String value)
    {
        switch (value.toUpperCase())
        {
            case "0":
            case "1":
            case ".FALSE":
            case ".TRUE":
            case "FALSE":
            case "TRUE":
                return true;
            default:
                return false;
        }
    }

    /** Determines the boolean value of a BSF4ooRexx850 boolean string and returns it.
     *
     *
     * @param value the string containing a BSF4ooRexx850 boolean rendering
     * @return true if "1", ".TRUE", "TRUE" and false else
     */
    public static boolean getBooleanValue(String value)
    {
        switch (value.toUpperCase())
        {
            case "1":
            case ".TRUE":
            case "TRUE":
                return true;
            default:
                return false;
        }
    }


// ----------------------------------------------------------

   final static boolean bStaticDebug=false; // true;
    /** ooRexx array expression open delimiter. */
    static char arrayOpenDeli ='('; // Rexx-like
    /** ooRexx array expression close delimiter. */
    static char arrayCloseDeli=')'; // Rexx-like

    // a string e.g. of " {a,b , c } "
    /** Turns an ooRexx array expression (a string) starting at the supplied position
     *  into a Java <code>int</code> array and returns it.
     *
     * @param value the string containing an ooRexx array expression
     * @param pos the starting position in the string to look for the ooRexx array expression
     * @return float array or <code>null</code> if not a valid ooRexx array expression
     */
    static float[] RexxArrayExpressionToFloatArray(final String value, final int pos)
    {
        int start=value.indexOf(arrayOpenDeli,pos);     // find starting delimiter
        if (start==-1)
        {
            return null;
        }
        int end=value.indexOf(arrayCloseDeli,start)+1;  // find closing delimiter
        if (end==0)
        {
            return null;
        }
if (bStaticDebug)
{
    System.err.println("RexxArrayExpressionToIntArray(): value=["+value+"], pos=["+pos+"]");
    System.err.println("RexxArrayExpressionToIntArray(): start  /end  =["+value.substring(start,end)+"]");
    System.err.println("RexxArrayExpressionToIntArray(): start+1/end-1=["+value.substring(start+1,end-1)+"]");
}
        String [] parts=value.substring(start+1,end-1).split(",");
if (bStaticDebug)
{
    System.err.println("RexxArrayExpressionToIntArray(): parts.length =["+parts.length+"] (after split)");
    System.err.println("RexxArrayExpressionToIntArray(): parts="+Arrays.deepToString(parts));
}
        float [] res=new float[parts.length];       // create array of needed size
        for (int i=0;i<parts.length;i++)        // fill array
        {
            res[i]=Float.parseFloat(strip(parts[i]));
        }
        return res;
    }



    // a string e.g. of " {a,b , c } "
    /** Turns an ooRexx array expression (a string) starting at the supplied position
     *  into a Java <code>int</code> array and returns it.
     *
     * @param value the string containing an ooRexx array expression
     * @param pos the starting position in the string to look for the ooRexx array expression
     * @return int array or <code>null</code> if not a valid ooRexx array expression
     */
    static int[] RexxArrayExpressionToIntArray(final String value, final int pos)
    {
        int start=value.indexOf(arrayOpenDeli,pos);     // find starting delimiter
        if (start==-1)
        {
            return null;
        }
        int end=value.indexOf(arrayCloseDeli,start)+1;  // find closing delimiter
        if (end==0)
        {
            return null;
        }
if (bStaticDebug)
{
    System.err.println("RexxArrayExpressionToIntArray(): value=["+value+"], pos=["+pos+"]");
    System.err.println("RexxArrayExpressionToIntArray(): start  /end  =["+value.substring(start,end)+"]");
    System.err.println("RexxArrayExpressionToIntArray(): start+1/end-1=["+value.substring(start+1,end-1)+"]");
}
        String [] parts=value.substring(start+1,end-1).split(",");
if (bStaticDebug)
{
    System.err.println("RexxArrayExpressionToIntArray(): parts.length =["+parts.length+"] (after split)");
    System.err.println("RexxArrayExpressionToIntArray(): parts="+Arrays.deepToString(parts));
}
        int [] res=new int[parts.length];       // create array of needed size
        for (int i=0;i<parts.length;i++)        // fill array
        {
            res[i]=Integer.parseInt(strip(parts[i]));
        }
        return res;
    }

    /** Removes leading and trailing blanks from integer value.
     *
     * @param value string to work on
     * @return string without leading and trailing blanks
     */
    // assume integer value may be enclosed in space character
    static String strip(String value)
    {
        int start = 0;
        int len=value.length();
        for ( ; start<len; start++)     // find first non-blank
        {
            if (value.charAt(start)!=' ') break;    // arrived at non-blank
        }
        if (start>=len)     // o.k. empty string or blanks only
        {
            return value;   // return unchanged parseInt() will throw an exception
        }

        int end   = start;              // proceed from current position
        for ( ; end<len;end++)          // find next blank, if any
        {
            if (value.charAt(end)==' ') break;    // arrived at a trailing blank
        }
if (bStaticDebug)
{
    System.out.println("\tvalue                     =["+value+"] start="+start+", end="+end);
    System.out.println("\tvalue.substring(start,end)=["+value.substring(start,end)+"]");
}
        return value.substring(start,end);  // return digits only so Integer.parseInt() does not barf
    }

        // create an ooRexx like array expression
    /** Returns a Rexx array expression from received <code>int</code> array.
     * @param intArray int array to work on
     * @return rendered Rexx array expression
     */
    static String intArrayToRexxArrayExpression(int [] intArray)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(arrayOpenDeli);
        if (intArray != null)
        {
            int last=intArray.length-1;
            for (int i=0;i<intArray.length;i++)
            {
                sb.append(""+intArray[i]);
                if (i<last) sb.append(',');
            }
        }
        sb.append(arrayCloseDeli);
        return sb.toString();
    }


}




// ============================================================================
// 2022-09-24 cf. <https://docs.oracle.com/javase/8/docs/technotes/guides/2d/spec/j2d-print.html>,
//                <http://www.java2s.com/Tutorial/Java/0261__2D-Graphics/Printanimageout.htm>
//                <https://coderanch.com/t/502123/java/Print-simple-jpeg-image-java>

/** Class to allow printing images via the JDOR command handler.
*/
class JavaDrawingPrintable implements Printable
{
    BufferedImage      img=null;    // image to be printed
    JavaDrawingHandler jdh=null;    // invoking instance, use its printer related settings

    /** Constructor to fetch the image to print and the handler instance to use its printer related settings.
     * @param workImage the image to be printerd
     * @param jdh the JavaDrawingHandler requesting printing, use its printer related settings
     */
    JavaDrawingPrintable(BufferedImage img, JavaDrawingHandler jdh)
    {
        this.img=img;
        this.jdh=jdh;
    }


    /** Callback method.
     * @param g Graphics instance
     * @param pf PageFormat instance
     * @param pageIndex page to print (can be supplied multiple times)
     */
    public int print(Graphics g, PageFormat pf, int pageIndex)
    {
        if (pageIndex>0)
        {
            return Printable.NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D) g;

        // make sure we move 0,0 to the left upperhand corner of the imageable area
        g.translate((int) (pf.getImageableX()), (int) (pf.getImageableY()));

        if (jdh.printScaleToPage)
        {
            double pageWidth   = pf.getImageableWidth();
            double pageHeight  = pf.getImageableHeight();
            double imageWidth  = img.getWidth();
            double imageHeight = img.getHeight();
            double scaleX      = pageWidth / imageWidth;
            double scaleY      = pageHeight / imageHeight;
            double scaleFactor = Math.min(scaleX, scaleY);
            g2d.scale(scaleFactor, scaleFactor);
            g.drawImage(img, 0, 0, null);
        }
        else
        {
            g2d.scale(jdh.printScaleX, jdh.printScaleY);
            g.drawImage(img, jdh.printPosX, jdh.printPosY, null);
        }
        return Printable.PAGE_EXISTS;
    }
}

// ============================================================================
/** Define the commands we process, support the string renderings.
 *  Note: this sample can be extended to support drawImage, drawArc, drawPolygon,
 *        etc., cf. java.awt.Graphics and java.awt.Graphics2D
 */
enum Command {
    // define the enum values for the commands available to Rexx programs,
    // supplying the strings associated with them                   // ? synonyms too

    // java.awt.Graphics or java.awt.Graphics2D related
    BACKGROUND                  ( "background"               ) ,    //   "background  [colorNickName]" query current background color or set
    CLEAR_RECT                  ( "clearRect"                ) ,    //   "clear width height": uses current color to clear background
    COLOR                       ( "color"                    ) ,    //   "color  [colorNickName [r g b [a]]" query current color or set + define new color
    COMPOSITE                   ( "composite"                ) ,    //   "composite [rule [alpha]]": get current or get and set new;

// 20220929: do not (yet) support Shapes, maybe in a future update
//    CLIP_NEW                    ( "clipNew"                  ) ,    //  ?? "clipNew x y w h" -> setClip(x y w h)
    CLIP_REMOVE                 ( "clipRemove"               ) ,    //   "clipRemove" -> setClip(null)
    CLIP                        ( "clip"                     ) ,    //   "clip [x y w h]" -> get (getClipBounds()) or clipRect(x y w h)

    DRAW_3D_RECT                ( "draw3DRect"               ) ,    //   "draw3DRect width height raised" - 20220906
    DRAW_ARC                    ( "drawArc"                  ) ,    //   "drawArc  width height startAngle arcAngle"
    DRAW_IMAGE                  ( "drawImage"                ) ,    //   "drawImage nickName [bkgColor] | nickName width height [bkgColor] | nickName width height srcX1 srcY1 srcX2 srcY2 [bkgColor]"

    DRAW_IMAGE_FROM_REXX_VAR    ( "drawImageFromVar"         ) ,    // "drawImageFromVar rexxVarName [bkgColor] | rexxVarName width height [bkgColor] | rexxVarName width height srcX1 srcY1 srcX2 srcY2 [bkgColor]"

    DRAW_LINE                   ( "drawLine"                 ) ,    //   "drawLine toX toY"
    DRAW_OVAL                   ( "drawOval"                 ) ,    //   "drawOval width height"
    DRAW_POLYGON                ( "drawPolygon"              ) ,    //   "drawPolygon  []xPoints []yPoints nPoints
    DRAW_POLYLINE               ( "drawPolyline"             ) ,    //   "drawPolyline []xPoints []yPoints nPoints
    DRAW_RECT                   ( "drawRect"                 ) ,    //   "drawRect width height"
    DRAW_ROUND_RECT             ( "drawRoundRect"            ) ,    //   "drawRoundRect width height arcWidth arcHeight"
    DRAW_STRING                 ( "drawString"               ) ,    //   "drawString text"

    FILL_3D_RECT                ( "fill3DRect"               ) ,    //   "fill3DRect width height raised" - 20220906
    FILL_ARC                    ( "fillArc"                  ) ,    //   "fillArc  width height startAngle arcAngle"
    FILL_OVAL                   ( "fillOval"                 ) ,    //   "fillOval width height"
    FILL_POLYGON                ( "fillPolygon"              ) ,    //   "fillPolygon  []xPoints []yPoints nPoints
    FILL_RECT                   ( "fillRect"                 ) ,    //   "fillRect width height"
    FILL_ROUND_RECT             ( "fillRoundRect"            ) ,    //   "fillRoundRect width height arcWidth arcHeight"
    FONT                        ( "font"                     ) ,    //   "font   [fontNickName [name]]" query or set font using curr type and size
    GRADIENT_PAINT              ( "gradientPaint"            ) ,    //   "gradientPaint paintNickName [x1 y1 colorName1 x2 y2 colorName2 [cyclic]]": set or define and set gradientPaint
    PAINT                       ( "paint"                    ) ,    //   "paint [gradiantPaintNickName|colorNickName]" query or set paint (for background) // 2022-09-22, paint, as of 20220922 cf. <http://underpop.online.fr/j/java/help/colors-graphics-programming.html.gz>
    POS                         ( "pos"                      ) ,    //   "pos [x y]" query or set position, synonyms: "goto"
    ROTATE                      ( "rotate"                   ) ,    //   "rotate [theta [x y]]" query or set rotation
    SCALE                       ( "scale"                    ) ,    //   "scale x [y]" set scale for x, y; if y omitted, uses x
    SET_PAINT_MODE              ( "setPaintMode"             ) ,    //   "setPaintMode": cf. Grahpics#setPaintMode()
    SHEAR                       ( "shear"                    ) ,    //   "shear x [y]" set scale for x, y; if y omitted, uses x
    SLEEP                       ( "sleep"                    ) ,    //   "sleep msecs" (uses Thread.sleep())
    STROKE                      ( "stroke"                   ) ,    //   "stroke [strokeNickName [width [cap join [miterlimit [[]floatDash floatDashPhase]]]]" query or set stroke
    TRANSLATE                   ( "translate"                ) ,    //   "translate x [y]" move origin; if y omitted, uses x
    XOR_MODE                    ( "xorMode"                  ) ,    //   "xorMode colorNickName": sets xorMode to supplied color

    // ------------------------------
    // commands for control, maintenance logic
    PREFERRED_IMAGE_TYPE        ( "preferredImageType"       ) ,    //   "preferredImageType [type]": get or set preferred image type, type can be integer or name of BufferedImage TYPE constant
    NEW                         ( "new"                      ) ,    //   "new [width height [type]]" creates a new BufferedImage
    RESET                       ( "reset"                    ) ,    //   "reset" synonym: "clear", clears everything, resets

    FONT_SIZE                   ( "fontSize"                 ) ,    //   "fontSize size
    FONT_STYLE                  ( "fontStyle"                ) ,    //   "fontStyle 0=PLAIN | 1=BOLD | 2=ITALIC | 3=BOLD+ITALIC

    GET_GC                      ( "GC"                       ) ,    //   "GC"    returns current GC (GraphicsContext)
    GET_IMAGE                   ( "image"                    ) ,    //   "image [nickName]" returns image or nickName-stored image
    GET_STATE                   ( "getState"                 ) ,    //   "getState [ctxtVariableName]" returns a StringTable with current variables, stacks and HashMaps;

    IMAGE_SIZE                  ( "imageSize"                ) ,    //   "imageSize [imageNickName]": returns width and height
    IMAGE_TYPE                  ( "imageType"                ) ,    //   "imageType [nickName]": get current image's type, if nickName supplied return its image type
    LOAD_IMAGE                  ( "loadImage"                ) ,    //   "loadImage imageNickName filename", loads an image -> returns its dimension
    SAVE_IMAGE                  ( "saveImage"                ) ,    //   "saveImage filename", saves current image to file

    PUSH_GC                     ( "pushGC"                   ) ,    //   "pushGC" push current gc
    POP_GC                      ( "popGC"                    ) ,    //   "popGC"  pop and set previous gc

    PUSH_IMAGE                  ( "pushImage"                ) ,    //   "pushImage [nickName]" push current image displayed
    POP_IMAGE                   ( "popImage"                 ) ,    //   "popImage"  pop and display previous image

    PRINT_IMAGE                 ( "printImage"               ) ,    //   "printImage [nickName]": print image, either current or the one by nickname
    PRINT_POS                   ( "printPos"                 ) ,    //   "printPos [X Y]: get or set position of left upperhand corner; honored if printScaleToPage is false
    PRINT_SCALE                 ( "printScale"               ) ,    //   "printScale [scaleX [scaleY]": get or set scaleX (if no scaleY use scaleX); honored if printScaleToPage is false
    PRINT_SCALE_TO_PAGE         ( "printScaleToPage"         ) ,    //   "printScaleToPage [.true|.false]": get or set

    WIN_CLOSE                   ( "winClose"                 ) ,    // S "winClose" synonym: "exit" closes JavaDrawingFrame instance
    WIN_FRAME                   ( "winFrame"                 ) ,    //   "winFrame [.true|.false]": query or set JFrame's frame according to argument
    WIN_HIDE                    ( "winHide"                  ) ,    //   "winHide" hide JavaDrawingFrame

    WIN_LOCATION                ( "winLocation"              ) ,    //   (synonym: WINMOVETO)"winLocation [x y]" get or set location on screen
    WIN_TO_BACK                 ( "winToBack"                ) ,    //   "winToBack" put window into the back
    WIN_TO_FRONT                ( "winToFront"               ) ,    //   "winToFront" put window into the front
    WIN_ALWAYS_ON_TOP           ( "winAlwaysOnTop"           ) ,    //   "winAlwaysOnTop [.true | .false]" get or set of this window state
    WIN_ALWAYS_ON_TOP_SUPPORTED ( "winAlwaysOnTopSupported"  ) ,    //   "winAlwaysOnTopSupported" get value

    WIN_SCREEN_SIZE             ( "winScreenSize"            ) ,    //   "winScreenSize" query screen's width and size
    WIN_SHOW                    ( "winShow"                  ) ,    //   "winShow" show JavaDrawingFrame
    WIN_SIZE                    ( "winSize"                  ) ,    //   "winSize [width height]" query or set position
    WIN_TITLE                   ( "winTitle"                 ) ,    //   "winTitle [newtitle]": get or set JFrame's title
    WIN_UPDATE                  ( "winUpdate"                ) ,    //   "winUpdate [.true|.false]" get or set whether Frame should be updated upon draws/changes
    WIN_VISIBLE                 ( "winVisible"               ) ,    //   "winVisible [.true|.false]" get or set visibility

    NO_OP                       ( "noOp"                     )      // last element, doing nothing
                               ;


    /**  HashMap that allows to retrieve the enum type given a string, irrespectible of the case the string was supplied.
     *   Cf. {@link #getCommand(String mixedCase)}.
     */
    final static HashMap<String,Command> upperCase2Command=new HashMap<String,Command>(64);

    static   // use static block to add synonym strings to upperCase2Command Map
    {
        // add all types to HashMap (in declaration order)
         for (Command it : Command.values())
         {
             upperCase2Command.put(it.upperCase, it);
         }

         // add synonyms         new name      enum type
         upperCase2Command.put( "CLEAR"      , RESET     ) ;
         upperCase2Command.put( "GOTO"       , POS       ) ;
         upperCase2Command.put( "LOCATION"   , POS       ) ;    // 2022-10-23: new alias
         upperCase2Command.put( "WINMOVETO"  , WIN_LOCATION ) ;
    }


    final String mixedCase;
    /** Returns the String value in mixed case.
     *
     * @return  the String value in mixed case.
     */
    public String getMixedCase()
    {
        return mixedCase;
    }

    final String upperCase;
    /** Returns the String value in upper case.
     *
     * @return  the String value in upper case.
     */
    public String getUpperCase()
    {
        return upperCase;
    }

     // note: no access to static fields allowed in constructor (Enum values get created before static block
    Command(String mixedCase)
    {
        this.mixedCase=mixedCase;
        this.upperCase=mixedCase.toUpperCase();
    }

    /** Returns the Command corresponding to the supplied String.
     *
     * @param mixedCase a String value in any case corresponding to the command from Rexx
     *
     * @return the enum Command corresponding to the supplied String, <code>null</code> if not defined
     */
    static public Command getCommand(String mixedCase)
    {
        if (mixedCase==null)
        {
            return null;
        }
        return upperCase2Command.get(mixedCase.toUpperCase());
    }


    public static void main(String[] args) {
        System.err.println("... arrived in main, args.length="+args.length+", DRAW_LINE="+DRAW_LINE);
        System.err.println("... arrived in main, args.length="+args.length+", DRAW_LINE.toString()="+DRAW_LINE.toString());
        System.err.println("... arrived in main, args.length="+args.length+", getCommand(\"NOT_AVAIL\")="+getCommand("NOT_AVAIL"));
        System.err.println("... arrived in main, args.length="+args.length+", getCommand(\"cLeAr\")  ="+getCommand("cLeAr"));
        System.err.println("... arrived in main, args.length="+args.length+", getCommand(\"drawOval\")="+getCommand("drawOval"));

        for (int i=0; i<args.length; i++)
        {
            System.err.println("... ... ... .. main, args["+i+"/"+args.length+"]: getCommand("+args[i]+")=["+getCommand(args[i])+"]");
        }
    }
}

