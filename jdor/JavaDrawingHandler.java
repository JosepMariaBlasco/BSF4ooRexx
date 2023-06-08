// cf. <https://zetcode.com/gfx/java2d/basicdrawing/>

// TODO: B4R -> resolving constructors, if class is not public


/* Java classes that constitute the JDOR ("JavaDrawing ooRexx") command handler.

   This enables any programmer to easily create and manipulate bitmaps on any
   operating system (Windows, MacOS, Linux) where Java, ooRexx and BSF4ooRexx is
   available.

   author:  Rony G. Flatscher
   date:    2022-09-05/2022-10-05
   version: 100.20230606
   license: Apache license 2.0

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2022-2023 Rony G. Flatscher

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
        2022-10-22: - fold DRAW_IMAGE_FROM_VAR into DRAW_IMAGE implementation
                    - corrected LOAD_IMAGE to return dimension instead of image (cf. documentation)
                    - change logic for queryable and setable commands: if setting new
                      value, return previous value
        2022-10-23: - reworked some WIN_* commands to not automatically create and show JFrame
        2022-10-26: - allow float values to define colors in addition to int values
                    - add synonym POSITION for POS command
        2022-10-27: - added command "copyImage"
        2022-10-28: - translate, scale and shear now return values at time of invocation
                    - new command "transform" to allow querying, setting and resetting Graphics2D's
                      AffineTransform
        2022-10-29: - "transform": uses default AffineTransform() for reset, "reset" argument can
                                   be reduced to first letter ('r' or 'R'), setting argument values
                                   now allows to use the Rexx anonymous variable "." (the dot) which
                                   causes no change in the respective argument's value (its current
                                   value gets reused)
                    - fixed errors in areas tested for the first time (like testing caps or linend values)
                    - added string2int(strValue): allows for supplying float and double values but
                      return an int value rounded with Math.round() in places where floats may be
                      produced (e.g. calculating positions, lengths, widths) but Graphics[2D] methods
                      expect int values instead
        2022-10-30: - add public instance fields "bUseNames4canonical" (true -> replace ints with
                      constant names, else int value) and "bUseInt4numbers" (true -> use int value,
                      else number as supplied)
                    - "font": fixed bug if using one of the operating system independent (logical)
                              fonts named "Dialog", "DialogInput", "Serif", "SansSerif", "Monospaced":
                              now honors also fontSize and fontStyle
        2022-10-31: - fix COMPOSITE (correct int value caused exception)
                    - removed command DRAW_IMAGE_FROM_VAR_NAME, instead changed DRAW_IMAGE to
                      try to fetch the image from a Rexx variable named as the nickName, if an
                      image cannot be found in the registry by that nickname
                    - PRINT_IMAGE now also can take an image (must be a BufferedImage) from a Rexx
                      variable named after the nickname (only if not found in the image registry)
        2022-11-01: - add fontStyles[Int2Name], replace fontStyle numeric value with name, if
                      "bUseName4canonical" is set
                    - new command "imageCopy": returns a copy of an image (maybe for buffering purposes)
                    - new command "stringBounds": returns the Rectangle2D floats for "x y width height"
                      for the supplied string (allows for adjusting the string)
        2022-11-02: - when loading a font try also Font.decode(...) if a replacement font was
                      created; now one can append the style and size information directly after the fontname
                    - turned antialiasing and text antialising on (renderings will look better on
                      high resoulution targets)
                    - added new 'render' command to query or change the settings; note: if animating
                      text (using drawString) it may be necessary to turn off text antialiasing to
                      inhibit a trail/shmear, cf. 3-110_JDOR_animate_composite.rxj as an example
        2022-11-03: - the following commands now also take a Rexx variable named by the supplied
                      nick name, if not found in the registries: BACKGROUND, COLOR, FONT, GRADIENT_PAINT,
                      IMAGE_SIZE, IMAGE_TYPE, PAINT, SET_XOR_MODE, STROKE
        2022-11-05: - changing logic of "font" command:
                      - if sought font is not found, but a replacement font gets supplied then
                        use that but create an error condition to communicate that fact
                      - if sought font could be found via Font.decode() (may include trailing font style
                        and font size information), then do not change currFontType and currFontSize which
                        serve as default values if a font gets created with a name without type and size
                        information
        2022-11-06: - cmd "reset" also resets GC's AffineTransform (translate to 0,0, scale to 1,1,
                      shear to 0,0)
                    - cmd "getState" adds an entry "gc.transform" referring to current AffienTransform object
        2022-12-03: - adding EnumShape enumeration
                    - changing Command enumeration to EnumCommand
                    - adding commands "shape", "fillShape", "drawShape", "clipShape"
                    - adding support for Shapes: Arc2D, CubicCurve2D, Ellipse2D, Line2D, Polygon,
                      QuadCurve2D, Rectangle2D, RoundRectangle2D
        2022-12-03: - drawShape, fillShape, clipShape: if name not in cache, check whether a Rexx variable
                      of that name refers to a Shape object, if so use it, otherwise raise an error condition
                    - add alias names for shapes without the trailing "2D"
        2022-12-07: - adding Path2D shape support, new commands: shapeBounds, pathAppend, pathClose,
                      pathCurrentPoint, pathCurveTo, pathLineTo, pathMoveTo, pathQuatTo, pathReset,
                      pathWindingRule
                    - pathAppend: accepts a Shape or a PathIterator object via Rexx variable
                    - add ability to have named TRANSFORM objects: allows creating affineTransforms
                      via commands to be used in Shape.getPathIterator(affineTransform)
        2023-01-04: - small edits and corrections to error messages
                    - shape abbreviations, at least two letters, added "polygon" as abbreviation
        2023-01-07: - simple text on new commands (shapes and path related)
        2023-01-08: - add "assignTo RexxVarName" command: allows self contained macros to refer to earlier return values
        2023-01-16: - drawString: if not at least a single blank, report error (ok, if "drawString  ")
                    - bUseNames4canonical: if true then also use .true/.false if checkBooleanValue() used
        2023-01-18: - rename command "new" to "newImage" to better self document, add "new" as an alias
                      to allow existing programs to continue to work
        2023-05-30: - GET_STATE: add Rexx variable name to canonical command, if supplied
        2023-05-31: - fixed canonical name for STROKE (cap, join values)
        2023-06-03: - adding floatArrayToRexxArrayExpression(float [] floatArray)
                    - Rexx int and float arrays will be turned into RexxArrayExpression strings for
                      the canonical command form
        2023-06-05: - remove unused variables
                    - always use [int|float]ArrayToRexxExpression() in canonical command (polygon, strokes etc.)
                    - changed resizable to false by default, adding new command "winResizable [.true|.false]"
                    - add Area support (including Area's CAG methods)
        2023-06-06: - allow Rexx variables as second argument to the areaXYZ commands
                    - removed shape abbreviations that were too short to convey any meaning or
                      (LI, PA, PO), changed shape abbreviations to allow inferring better (CU->CUBIC,
                      EL->ELLI, QU->QUAD, RE->RECT, RO->ROUNDRECT)
        2023-06-08: - fixed bugs in Shape area (tested with new sample "1-190_JDOR_shapes.rxj")
                    - fixed (introduced) bug in processCommand() that inhibited processing the
                      redirected commands via redirected input
                    - now handleCommand() will have the triggering command processed when
                      executing redirected input

*/

package org.oorexx.handlers.jdor;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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

// for clipboard support
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
// import java.io.IOException;

// ============================================================================
/** Class to allow displaying the current image.
*/
class JavaDrawingFrame extends JFrame
{
    /* static definitions           */
    static final private int prefWidth  = 500;
    static final private int prefHeight = 500;
    static final private boolean prefResizable=false;

    /* instance definitions         */
    boolean bDebug= false; // true
    Surface sf = null;

    int currWidth  = prefWidth;
    int currHeight = prefHeight;
    boolean currFrameVisible = true;    // cf. command "winFrame [.true|.false]"
    boolean currFrameResizable = prefResizable;

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
        setResizable(currFrameResizable);
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
    static final public String version = "100.20230606";
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

    // FontStyle
    static final HashMap<String,Integer> fontStyles         = new HashMap<>();
    static final HashMap<Integer,String> fontStylesInt2Name = new HashMap<>();

    // RenderingHint.Key supported for "rendering" command
    static final HashMap<String,Object>  renderingHints         = new HashMap<>();
    static final HashMap<String,String>  renderingHintsAlias    = new HashMap<>();
    static final HashMap<Object,String>  renderingHintsObj2Name = new HashMap<>();

    // Arc2D Closures
    static final HashMap<String,Integer> arcClosures         = new HashMap<>();
    static final HashMap<Integer,String> arcClosuresInt2Name = new HashMap<>();

    // Path2D winding rules
    static final HashMap<String,Integer> windingRules         = new HashMap<>();
    static final HashMap<Integer,String> windingRulesInt2Name = new HashMap<>();

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

        // ----------------------------------------------------------------------
        // FontStyle
        fontStyles.put("PLAIN"     , 0);
        fontStyles.put("BOLD"      , 1);
        fontStyles.put("ITALIC"    , 2);
        fontStyles.put("BOLDITALIC", 3);
        // allow for looking up constant names (key) by value
        fontStyles.forEach((K,V)->fontStylesInt2Name.put(V,K));

        // ----------------------------------------------------------------------
        renderingHints.put("ALIASON"         , RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put("ALIASOFF"        , RenderingHints.VALUE_ANTIALIAS_OFF);
        renderingHints.put("ALIASDEFAULT"    , RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        renderingHints.put("TEXTALIASON"     , RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        renderingHints.put("TEXTALIASOFF"    , RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        renderingHints.put("TEXTALIASDEFAULT", RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
        renderingHintsAlias.put("ALIASON"         , "aliasOn"           );
        renderingHintsAlias.put("ALIASOFF"        , "aliasOff"          );
        renderingHintsAlias.put("ALIASDEFAULT"    , "aliasDefault"      );
        renderingHintsAlias.put("TEXTALIASON"     , "textAliasOn"       );
        renderingHintsAlias.put("TEXTALIASOFF"    , "textAliasOff"      );
        renderingHintsAlias.put("TEXTALIASDEFAULT", "textAliasDefault"  );
        // allow for looking up constant names (key) by value
        renderingHints.forEach((K,V)->renderingHintsObj2Name.put(V,K));

        // ----------------------------------------------------------------------
        // Arc2D closures
        arcClosures.put("OPEN"  , 0);
        arcClosures.put("CHORD" , 1);
        arcClosures.put("PIE"   , 2);
        // allow for looking up constant names (key) by value
        arcClosures.forEach((K,V)->arcClosuresInt2Name.put(V,K));

        // ----------------------------------------------------------------------
        // Path2D windingRules
        windingRules.put("WIND_EVEN_ODD" , 0);
        windingRules.put("WIND_NON_ZERO" , 1);
        // allow for looking up constant names (key) by value
        windingRules.forEach((K,V)->windingRulesInt2Name.put(V,K));
    }


    int nrCommand=0;    // in case many commands, indicates which command may have caused an error or failure condition

    public boolean bUseNames4canonical=true;    // if true: use constant names instead of int values in canonical output
    public boolean bUseInt4numbers    =true;    // if true: use int values instead of the supplied numbers in canonical output

    /* instance definitions         */
    JavaDrawingFrame jd = null;         // maintance a singleton or empty
    boolean bDebug=false; // true; // false; // true

    // we draw here and do a drawImage in the Surface paint
    BufferedImage bufImage = null;
    Graphics2D    bufGC    = null;

    /* printer related values */
    boolean printScaleToPage = false;
    int     printPosX=0, printPosY=0;
    double  printScaleX=1, printScaleY=1;

    /* current values      */
    int currX = 0;
    int currY = 0;

    int currFontStyle = 0;   // PLAIN by default
    int currFontSize  = 12;  // by default

    boolean currVisible = false;
    boolean currWinUpdate = true;       // update Frame whenever we draw: allows turning updating on/off
    boolean currResizable = false;      // cf. command "winResizable [.true|.false]"

    // 2022-11-02: makes a difference when rendered to screen as well, so by default antialising is turned on
    Object currAntiAliasing     = RenderingHints.VALUE_ANTIALIAS_ON;
    Object currTextAntiAliasing = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

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
    // 2022-12-03
    HashMap<String,Shape>         hmShapes = new HashMap<>();
    // 2022-12-07
    HashMap<String,AffineTransform>   hmTransforms = new HashMap<>();


    void reset ()   // reset all current variables and caches
    {
        currX            =  0;
        currY            =  0;

        currFontStyle    =  0;  // PLAIN by default
        currFontSize     = 12;
        if (bufGC!=null)
        {
            bufGC.setTransform(new AffineTransform());   // reset transform
        }

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

        hmShapes.clear();
        hmTransforms.clear();

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
            res=processCommand(slot, address, command, nrCommand, null);    // process kick-off command first

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
        else    // single command, indicated by null in fifth argument
        {
            res=processCommand(slot, address, command, nrCommand, null);
        }
        return res;
    }


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
        if (currCommand!=null)      // a command from redirected input, then it prevails
        {
            command=currCommand;    // direct command that triggered handler gets ignored in this use case
        }
        String [] arrCommand = null;

        boolean isOR = isOutputRedirected(slot);    // is output redirected ?

        // get the boundaries of the first 15 words
        ArrayList<int[]> alWordBoundaries = getWordBoundaries (command, 15);
        arrCommand = getNonBlankWords(command, alWordBoundaries);

        EnumCommand cmd = null;
        if (arrCommand.length>0)        // empty string
        {
            cmd=EnumCommand.getCommand(arrCommand[0]);    // can be null, if not available
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
        if (bufImage==null && !(cmd==EnumCommand.NEW_IMAGE))
        {
            bufImage = new BufferedImage(prefWidth, prefHeight, prefImageType);
            bufGC    = (Graphics2D) bufImage.getGraphics();
            bufGC.setColor(Color.BLACK);
            bufGC.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      currAntiAliasing);
            bufGC.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, currTextAntiAliasing);
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
                            newX = string2int(arrCommand[1]);
                            newY = string2int(arrCommand[2]);
                            jd.setLocation(newX, newY);
                            if (isOR)
                            {
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+newX+" "+newY;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2];
                                }
                            }
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
                        if (cmd!=EnumCommand.WIN_ALWAYS_ON_TOP && arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command expects no arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        if (jd==null)   // implies creation of JavaDrawingFrame
                        {
                            jd = new JavaDrawingFrame(bufImage);
                            if (cmd!=EnumCommand.WIN_ALWAYS_ON_TOP_SUPPORTED)
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
                                            throw new IllegalArgumentException("the supplied \"winAlwaysOnTo\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                                        }
                                        if (isOR)
                                        {
                                            if (bUseNames4canonical)
                                            {
                                                canonical=canonical+" "+ (retBoolValue ? ".true" : ".false");
                                            }
                                            else    // reuse argument verbatimely
                                            {
                                                canonical=canonical+" "+newValue;
                                            }
                                        }
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
                            throw new IllegalArgumentException("the supplied \"winFrame\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }

                        // no exception, so args o.k.
                        if (isOR)
                        {
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+(newFrameVisible ? ".true" : ".false");
                            }
                            else    // reuse argument verbatimely
                            {
                                canonical=canonical+" "+newValue;
                            }
                            writeOutput(slot, canonical);
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

                case WIN_RESIZABLE:   // winResizable [.true|.false]: query or set whether frame is resizable
                    {
                        resultValue = jd.currFrameResizable ? "1" : "0";  // query current value (to be returned if change occurs)

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
                            throw new IllegalArgumentException("the supplied \"winResizable\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }

                        if (currResizable==newBooleanValue)   // already at the same state, ignore command, but return "old" value
                        {
                            return resultValue;
                        }

                        // no exception, so args o.k.
                        if (isOR && arrCommand.length==2)
                        {
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+(newBooleanValue ? ".true" : ".false");
                            }
                            else    // reuse argument verbatimely
                            {
                                canonical=canonical+" "+newValue;
                            }

                            writeOutput(slot,canonical);
                        }

                        if (jd==null)
                        {
                            jd = new JavaDrawingFrame(bufImage);
                            if (newBooleanValue)
                            {
                                jd.setResizable(true);    // make frame resizable
                            }
                        }
                        else if (jd.currFrameResizable!=newBooleanValue)
                        {
                            jd.setResizable(newBooleanValue);
                        }
                        jd.currFrameResizable = newBooleanValue;
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
                            throw new IllegalArgumentException("the supplied \"winVisible\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }

                        if (currVisible==newBooleanValue)   // already at the same state, ignore command, but return "old" value
                        {
                            return resultValue;
                        }

                        // no exception, so args o.k.
                        if (isOR && arrCommand.length==2)
                        {
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+(newBooleanValue ? ".true" : ".false");
                            }
                            else    // reuse argument verbatimely
                            {
                                canonical=canonical+" "+newValue;
                            }

                            writeOutput(slot,canonical);
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
                            throw new IllegalArgumentException("the supplied \"winUpdate\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                  + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                        }
                        // no exception, so args o.k.
                        currWinUpdate = newBooleanValue;
                        if (isOR)
                        {
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+(newBooleanValue ? ".true" : ".false");
                            }
                            else    // reuse argument verbatimely
                            {
                                canonical=canonical+" "+newValue;
                            }
                            writeOutput(slot,canonical);
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

                        int newWidth  = string2int(arrCommand[1]);
                        int newHeight = string2int(arrCommand[2]);

                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+newWidth+" "+newHeight;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2];
                            }
                            writeOutput(slot, canonical);
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
                            if (isOR)
                            {
                                if (bUseNames4canonical)
                                {
                                    canonical=canonical+" "+(printScaleToPage ? ".true" : ".false");
                                }
                                else    // reuse argument verbatimely
                                {
                                    canonical=canonical+" "+newValue;
                                }
                                writeOutput(slot,canonical);
                            }
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
                            if (isOR)
                            {
                                canonical = canonical + " " + arrCommand[1];
                            }

                            if (arrCommand.length==2)   //
                            {
                                printScaleX=tmpScaleX;
                                printScaleY=tmpScaleX;  // use X value also for Y
                            }
                            else // scaleY given
                            {
                                printScaleY = Double.parseDouble(arrCommand[2]);
                                printScaleX = tmpScaleX;
                                if (isOR)
                                {
                                    canonical = canonical + " " + arrCommand[2];
                                }
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot,canonical);
                        }
                        return resultValue;
                    }

                case PRINT_POS: // "printPos [X [Y]]: get or set position of left upperhand corner
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command expects no, 1 or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        resultValue = printPosX+" "+printPosY;  // query current value (to be returned if change occurs)

                        int newX = string2int(arrCommand[1]);
                        int newY = newX;            // default to newX
                        if (arrCommand.length==3)
                        {
                            newY = string2int(arrCommand[2]);
                        }
                            // no exception, so args o.k.
                        printPosX = newX;
                        printPosY = newY;
                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+newX+" "+newY;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1] + " " +
                                    (arrCommand.length==3 ? arrCommand[2] : arrCommand[1]);
                            }
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
                                try
                                {
                                    tmpImage = (BufferedImage) getContextVariable(slot, name);   // try to get from a Rexx variable
                                }
                                catch (Throwable t) {}
                                if (tmpImage==null)
                                {
                                    throw new IllegalArgumentException("image (must be a BufferedImage) with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\" or \"pushImage "+name+"\" or assign a BufferedImage to a Rexx variable named \""+name+"\"");
                                }
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
                        if (argNum>3)
                        {
                            throw new IllegalArgumentException("this command needs either no, one or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                            // get current settings
                        AffineTransform at=bufGC.getTransform();
                        String strResult=null;
                        double newX=0, newY=0;

                        if (cmd==EnumCommand.SCALE)
                        {
                            strResult=at.getScaleX()+" "+at.getScaleY();
                        }
                        else // SHEAR
                        {
                            strResult=at.getShearX()+" "+at.getShearY();
                        }

                        if (argNum>1)   // set value
                        {
                            newX = Double.parseDouble(arrCommand[1]);
                            newY = newX;     // default to X value in case Y value is omitted
                            if (argNum==3)          // Y value supplied, use it
                            {
                                newY=Double.parseDouble(arrCommand[2]);
                            }

                            if (cmd==EnumCommand.SCALE)
                            {
                                bufGC.scale(newX,newY);
                            }
                            else
                            {
                                bufGC.shear(newX,newY);
                            }
                        }
                        if (isOR)
                        {
                           if (argNum>1)
                           {
                               canonical=canonical+" "+newX+" "+newY;
                           }
                           writeOutput(slot, canonical);  // write canonical form
                        }
                        return strResult;       // current/old settings
                    }

                case NEW_IMAGE:       // "new[Image] [width height [type]]" creates a new BufferedImage
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
                            width  = string2int(arrCommand[1]);
                            height = string2int(arrCommand[2]);
                            if (isOR)
                            {
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+width+" "+height;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[1] + " " + arrCommand[2];
                                }
                            }

                            if (arrCommand.length>3)
                            {
                                String argType = arrCommand[3].toUpperCase();
                                if (startRexxVariableChar.indexOf(argType.charAt(0))>=0)    // a symbolic name?
                                {
                                    if (!imageTypes.containsKey(argType))
                                    {
                                        throw new IllegalArgumentException("unknown value for \"type\" argument: \""+arrCommand[3]+"\"");
                                    }
                                    type=imageTypes.get(argType);
                                }
                                else // verbatim int type
                                {
                                    type=Integer.parseInt(arrCommand[3]);
                                    if (!imageTypes.containsValue(type))
                                    {
                                        throw new IllegalArgumentException("unknown value for \"type\" argument: \""+arrCommand[3]+"\"");
                                    }
                                }
                                if (isOR)
                                {
                                    canonical = canonical + " " +
                                        (bUseNames4canonical ? imageTypesInt2Name.get(type) : argType) ;
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
                                    if (adGCStack.size()>0)
                                    {
                                        tmpGC = adGCStack.pop();    // pop next GC
                                    }
                                    else    // no more GCs on stack
                                    {
                                        tmpGC = null;
                                    }
                                }
                                bufGC.dispose();
                                bufGC=null;
                            }
                        }

                        bufImage=new BufferedImage(width, height, type);    // create new image
                        bufGC=(Graphics2D) bufImage.getGraphics();          // get its GraphicsConfiguration
                        bufGC.setColor(Color.BLACK);            // make sure color is set to black

                        bufGC.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      currAntiAliasing);
                        bufGC.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, currTextAntiAliasing);
                        if (jd!=null)                           // update Frame if it exists
                        {
                            jd.bufImage=bufImage;
                        }
// TODO: resultValue -> currently not set, hence returns 0 (null): shall we return a different value, e.g. "width height type" ?
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
                                    throw new IllegalArgumentException("unknown value for \"type\" argument: \""+arrCommand[1]+"\"");
                                }
                                type=imageTypes.get(newType);
                            }
                            else // verbatim int type
                            {
                                type=Integer.parseInt(newType);
                                if (!imageTypes.containsValue(type))
                                {
                                    throw new IllegalArgumentException("unknown value for \"type\" argument: \""+arrCommand[1]+"\"");
                                }
                            }
                            if (isOR)
                            {
                                canonical = canonical + " " +
                                    (bUseNames4canonical ? imageTypesInt2Name.get(type) : arrCommand[1]) ;
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
                            try
                            {
                                img = (BufferedImage) getContextVariable(slot, name);  // try to get from a Rexx variable
                            }
                            catch (Throwable t) {}
                            if (img==null)
                            {
                                throw new IllegalArgumentException("image (must be a BufferedImage) with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\" or \"pushImage "+name+"\" or assign a BufferedImage to a Rexx variable named \""+name+"\"");
                            }
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+name);
                        }
                        return ""+img.getType();
                    }

                case IMAGE_COPY:    //   "imageCopy [nickName]": get a copy of the current image, if nickName supplied use from registry and if not found from Rexx variable
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs either no or exactly 1 argument (image's nickname for lookup), received "+(arrCommand.length-1)+" instead");
                        }
                        BufferedImage tmpImage = bufImage;  // current image
                        if (arrCommand.length==2)           // load image
                        {
                            String name = arrCommand[1];    // get  nickname
                            tmpImage = hmImages.get(name.toUpperCase());
                            if (tmpImage==null)
                            {
                                try
                                {
                                    tmpImage = (BufferedImage) getContextVariable(slot, name);  // try to get from a Rexx variable
                                }
                                catch (Throwable t) {}
                                if (tmpImage==null)
                                {
                                    throw new IllegalArgumentException("image (must be a BufferedImage) with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\" or \"pushImage "+name+"\" or assign a BufferedImage to a Rexx variable named \""+name+"\"");
                                }
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
                        return copyImage(tmpImage); // return copy of image
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
                                try // try to get from a Rexx variable
                                {
                                    tmpImg = (BufferedImage) getContextVariable(slot, name);
                                }
                                catch (Throwable t) {}
                                if (tmpImg==null)
                                {
                                    String errMsg="image with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\" or supply a Rexx variable referring to a BufferedImage";
                                    if (isOR)
                                    {
                                        writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                    }
                                    return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-2", errMsg );
                                }
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
                            if (arrCommand.length==2)   // supply context Rexx variable name to set
                            {
                                canonical = canonical + " " + arrCommand[1];
                            }
                            writeOutput(slot, canonical);
                        }

                        RexxProxy rop = (RexxProxy) newStringTable(slot);   // get a StringTable
                        rop.sendMessage2("SETENTRY", "currX"      , currX);
                        rop.sendMessage2("SETENTRY", "currY"      , currY);

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

                        // current rendering options in effect
                        rop.sendMessage2("SETENTRY", "currAntiAliasing"    , currAntiAliasing);
                        rop.sendMessage2("SETENTRY", "currTextAntiAliasing", currTextAntiAliasing);

                        // current GraphicConfiguration
                        rop.sendMessage2("SETENTRY", "gc.background" , bufGC.getBackground());
                        rop.sendMessage2("SETENTRY", "gc.color"      , bufGC.getColor()     );
                        rop.sendMessage2("SETENTRY", "gc.composite"  , bufGC.getComposite() );
                        rop.sendMessage2("SETENTRY", "gc.font"       , bufGC.getFont()      );
                        rop.sendMessage2("SETENTRY", "gc.paint"      , bufGC.getPaint()     );
                        rop.sendMessage2("SETENTRY", "gc.stroke"     , bufGC.getStroke()    );
                        rop.sendMessage2("SETENTRY", "gc.transform"  , bufGC.getTransform() );

                        // stack and HashMaps
                        rop.sendMessage2("SETENTRY", "gcStack"    , adGCStack            ); // pushGC
                        rop.sendMessage2("SETENTRY", "imageStack" , adImageStack         ); // pushImage
                        rop.sendMessage2("SETENTRY", "colors"     , hmColors             );
                        rop.sendMessage2("SETENTRY", "gradiantPaints", hmGradientPaints  );
                        rop.sendMessage2("SETENTRY", "fonts"      , hmFonts              );
                        rop.sendMessage2("SETENTRY", "images"     , hmImages             );
                        rop.sendMessage2("SETENTRY", "shapes"     , hmShapes             );
                        rop.sendMessage2("SETENTRY", "strokes"    , hmStrokes            );
                        rop.sendMessage2("SETENTRY", "transforms" , hmTransforms         );

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

                        resultValue = bufGC.getColor();  // query current value (to be returned if change occurs)

                        if (arrCommand.length==1)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }

                        Color color=null;

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
                                try // try to get from a Rexx variable
                                {
                                    color = (Color) getContextVariable(slot, colorNickName);
                                }
                                catch (Throwable t) {}
                                if (color==null)
                                {
                                    String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered nor is it a Rexx variable referring to a color";
                                    if (isOR)
                                    {
                                        writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                    }
                                    return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-3", errMsg );
                                }
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            bufGC.setColor(color);
                            // return color;
                            return resultValue;     // return previous color
                        }

                        int red=0, green=0, blue=0, alpha=0;
                        float fRed=0, fGreen=0, fBlue=0, fAlpha=0;
                        String strArguments=command.substring(alWordBoundaries.get(2)[0]);   // get all arguments
                        boolean bFloat=(strArguments.indexOf('.')>=0);  // if dot in any of the values then use float version

                        if (bFloat)
                        {
                            fRed   = Float.parseFloat(arrCommand[2]);
                            fGreen = Float.parseFloat(arrCommand[3]);
                            fBlue  = Float.parseFloat(arrCommand[4]);
                        }
                        else
                        {
                            red   = Integer.parseInt(arrCommand[2]);
                            green = Integer.parseInt(arrCommand[3]);
                            blue  = Integer.parseInt(arrCommand[4]);
                        }

                        if (arrCommand.length==6)   // alpha supplied, parse and use it
                        {
                            if (bFloat)
                            {
                                fAlpha = Float.parseFloat(arrCommand[5]);
                                color = new Color(fRed, fGreen, fBlue, fAlpha);
                            }
                            else
                            {
                                alpha = Integer.parseInt(arrCommand[5]);
                                color = new Color(red, green, blue, alpha);
                            }

                            if (isOR)
                            {
                                if (bFloat)
                                {
                                    canonical = canonical + " " + fRed + " " + fGreen + " " + fBlue + " " + fAlpha;
                                }
                                else
                                {
                                    canonical = canonical + " " + red + " " + green + " " + blue + " " + alpha;
                                }
                            }
                        }
                        else
                        {
                            if (bFloat)
                            {
                                color = new Color(fRed, fGreen, fBlue);
                            }
                            else
                            {
                                color = new Color(red, green, blue);
                            }

                            if (isOR)
                            {
                                if (bFloat)
                                {
                                    canonical = canonical + " " + fRed + " " + fGreen + " " + fBlue;
                                }
                                else
                                {
                                    canonical = canonical + " " + red + " " + green + " " + blue;
                                }
                            }
                        }
                        hmColors.put(colorNickName.toUpperCase(),color);
                        bufGC.setColor(color);
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

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
                                try // try to get from a Rexx variable
                                {
                                    gradientPaint = (GradientPaint) getContextVariable(slot, paintNickName);
                                }
                                catch (Throwable t) {}
                                if (gradientPaint==null)
                                {
                                    String errMsg="gradientPaint with the supplied nickname \""+paintNickName+"\" is not registered nor is it a Rexx variable referring to a GradientPaint object";
                                    if (isOR)
                                    {
                                        writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                    }
                                    return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-4", errMsg );
                                }
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            bufGC.setPaint(gradientPaint);
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
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-5", errMsg );
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
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-6", errMsg );
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
                                // canonical=canonical+" "+(cyclic ? "1" : "0");
                                if (bUseNames4canonical)
                                {
                                    canonical=canonical+" "+(cyclic ? ".true" : ".false");
                                }
                                else    // reuse argument verbatimely
                                {
                                    canonical=canonical+" "+newValue;
                                }
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
                            if (isOR)
                            {
                                canonical=canonical+" "+nickName;
                            }
                            Paint paint=null;
                            paint=hmGradientPaints.get(nickName.toUpperCase()); // first consult gradientPaints
                            if (paint==null)    // now try the registered colors
                            {
                                paint=hmColors.get(nickName.toUpperCase());
                                try // try to get from a Rexx variable
                                {
                                    paint = (Paint) getContextVariable(slot, nickName);
                                }
                                catch (Throwable t) {}
                            }
                            if (paint==null)    // neither found
                            {
                                String errMsg="neither paint nor gradientPaint with the supplied nickname \""+nickName+"\" is registered nor supplied with a Rexx variable of that name";
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
                            if (isOR)
                            {
                                canonical=canonical+" "+colorNickName;
                            }
                            Color  color=hmColors.get(colorNickName.toUpperCase());
                            if (color==null)
                            {
                                try // try to get from a Rexx variable
                                {
                                    color = (Color) getContextVariable(slot, colorNickName);
                                }
                                catch (Throwable t) {}
                                if (color==null)
                                {
                                    String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered nor is it a Rexx variable referring to a color";
                                    if (isOR)
                                    {
                                        writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                    }
                                    return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-8", errMsg );
                                }
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

                case SET_XOR_MODE:  // "xorMode colorNickName": set XORMode to color
                    {
                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }
                        String colorNickName = arrCommand[1];   // get color name
                        if (isOR)
                        {
                            canonical=canonical+" "+colorNickName;
                        }
                        Color  color=hmColors.get(colorNickName.toUpperCase());
                        if (color==null)
                        {
                            try // try to get from a Rexx variable
                            {
                                color = (Color) getContextVariable(slot, colorNickName);
                            }
                            catch (Throwable t) {}

                            if (color==null)
                            {
                                String errMsg="color with the supplied nickname \""+colorNickName+"\" is not registered nor supplied via a Rexx variable of that name";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-9", errMsg );
                            }
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

                        if (startRexxVariableChar.indexOf(ruleName.charAt(0))>=0)    // a symbolic name?
                        {
                            if (!alphaComposites.containsKey(ruleName))
                            {
                                throw new IllegalArgumentException("unknown value for \"rule\" argument: \""+arrCommand[1]+"\"");
                            }
                            intRule=alphaComposites.get(ruleName);
                        }
                        else // verbatim int type
                        {
                            int val=Integer.parseInt(ruleName);
                            if (!alphaComposites.containsValue(val))
                            {
                                throw new IllegalArgumentException("unknown value for \"rule\" argument: \""+arrCommand[1]+"\"");
                            }
                            intRule=val;
                        }

                        if (isOR)
                        {
                            canonical=canonical+" "+
                                (bUseNames4canonical ? alphaCompositesInt2Name.get(intRule) : intRule) ;
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


                case RENDER:        // "render [opt1 [opt2]]": returns or sets the current antialiasing settings for rendering
                    {
                        int argNum=arrCommand.length;
                        if (argNum>3)
                        {
                            throw new IllegalArgumentException("this command needs either no, one or two rendering arguments, received "+(argNum-1)+" instead");
                        }

                        // get current settings
                        Object o1=bufGC.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
                        Object o2=bufGC.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
                        String s1=renderingHintsObj2Name.get(o1);
                        String s2=renderingHintsObj2Name.get(o2);
                        resultValue = s1+" "+s2;    // query current value (to be returned if change occurs)

                        if (argNum==1)
                        {
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }

                        String strArg = arrCommand[1].toUpperCase();   // get argument
                        if (!renderingHints.containsKey(strArg))
                        {
                            throw new IllegalArgumentException("unknown value for argument # 1: \""+arrCommand[1]+"\"");
                        }

                        if (strArg.startsWith("TEXT"))
                        {
                            currTextAntiAliasing = renderingHints.get(strArg);
                            bufGC.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, currTextAntiAliasing);
                        }
                        else
                        {
                            currAntiAliasing = renderingHints.get(strArg);
                            bufGC.setRenderingHint(RenderingHints.KEY_ANTIALIASING, currAntiAliasing);
                        }
                        if (isOR)
                        {
                            canonical=canonical+" "+renderingHintsAlias.get(strArg);
                        }

                        if (argNum==3)
                        {
                            strArg = arrCommand[2].toUpperCase();
                            if (!renderingHints.containsKey(strArg))
                            {
                                throw new IllegalArgumentException("unknown value for argument # 1: \""+arrCommand[1]+"\"");
                            }
                            if (strArg.startsWith("TEXT"))
                            {
                                currTextAntiAliasing = renderingHints.get(strArg);
                                bufGC.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, currTextAntiAliasing);
                            }
                            else
                            {
                                currAntiAliasing = renderingHints.get(strArg);
                                bufGC.setRenderingHint(RenderingHints.KEY_ANTIALIASING, currAntiAliasing);
                            }
                            if (isOR)
                            {
                                canonical=canonical+" "+renderingHintsAlias.get(strArg);
                            }
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }


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
                            int newX      = string2int(arrCommand[1]);
                            int newY      = string2int(arrCommand[2]);
                            int newWidth  = string2int(arrCommand[3]);
                            int newHeight = string2int(arrCommand[4]);
                            resultValue = newX+" "+newY+" "+newWidth+" "+newHeight;

                            if (isOR)
                            {
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+resultValue;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[1] + " " + arrCommand[2] +
                                                          " "+arrCommand[3] + " " + arrCommand[4] ;
                                }
                            }
                            bufGC.clipRect(newX, newY, newWidth, newHeight);
                        }
                            // no exception, so args o.k.
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }

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

                        if (isOR)
                        {
                            // canonical=canonical+" "+strXPoints+" "+strYPoints+" "+strNPoints;
                            canonical=canonical+
                                            " "+intArrayToRexxArrayExpression(xPoints)+
                                            " "+intArrayToRexxArrayExpression(yPoints)+
                                            " "+strNPoints;
                            writeOutput(slot, canonical);
                        }

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
                                try // try to get from a Rexx variable
                                {
                                    stroke = (Stroke) getContextVariable(slot, strokeNickName);
                                }
                                catch (Throwable t) {}

                                if (stroke==null)
                                {
                                    String errMsg="stroke with the supplied nickname \""+strokeNickName+"\" is not registered nor is it a Rexx variable referring to a stroke";
                                    if (isOR)
                                    {
                                        writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                    }
                                    return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-10", errMsg );
                                }
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
                        width = string2int(arrCommand[2]);
                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+width;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[2];
                            }
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
                                if (!endCaps.containsKey(tmpCap))
                                {
                                    throw new IllegalArgumentException("unknown value for \"cap\" argument supplied: ["+tmpCap+"]");
                                }
                                cap=endCaps.get(tmpCap);
                            }
                            else // verbatim int type
                            {
                                cap=Integer.parseInt(tmpCap);
                                if (!endCaps.containsValue(cap))
                                {
                                    throw new IllegalArgumentException("unknown value for \"cap\" argument supplied: ["+tmpCap+"]");
                                }
                            }

                            // JOIN
                            String tmpJoin = arrCommand[4].toUpperCase();
                            if (startRexxVariableChar.indexOf(tmpJoin.charAt(0))>=0)    // a symbolic name?
                            {
                                if (!lineJoins.containsKey(tmpJoin))
                                {
                                    throw new IllegalArgumentException("unknown value for \"join\" argument supplied: ["+tmpJoin+"]");
                                }
                                join=lineJoins.get(tmpJoin);
                            }
                            else // verbatim int type
                            {
                                join=Integer.parseInt(tmpJoin);
                                if (! lineJoins.containsValue(join))
                                {
                                    throw new IllegalArgumentException("unknown value for \"join\" argument supplied: ["+tmpJoin+"]");
                                }
                            }
                            if (isOR)
                            {
                                if (bUseNames4canonical)
                                {
                                    canonical=canonical+" "+
                                              endCapsInt2Name.get(cap)+" "+
                                              lineJoinsInt2Name.get(join);
                                }
                                else
                                {
                                    canonical=canonical+" "+cap+" "+join;
                                }
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
// TODO: turn dash [] into quoted string instead of using strDash
// canonical=canonical+" "+strDash+" "+strDashPhase;
                                        canonical=canonical+" "+floatArrayToRexxArrayExpression(dash)+" "+strDashPhase;
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
                            int    fontStyle;
                            String newFontStyle = arrCommand[1].toUpperCase();
                            if (startRexxVariableChar.indexOf(newFontStyle.charAt(0))>=0)    // a symbolic name?
                            {
                                if (!fontStyles.containsKey(newFontStyle))
                                {
                                    throw new IllegalArgumentException("unknown value for \"fontStyle\" argument: \""+arrCommand[1]+"\"");
                                }
                                fontStyle=fontStyles.get(newFontStyle);
                            }
                            else // verbatim int type
                            {
                                fontStyle=Integer.parseInt(newFontStyle);
                                if (!fontStyles.containsValue(fontStyle))
                                {
                                    throw new IllegalArgumentException("unknown value for \"fontStyle\" argument: \""+arrCommand[1]+"\"");
                                }
                            }
                            if (isOR)
                            {
                                canonical = canonical + " " +
                                    (bUseNames4canonical ? fontStylesInt2Name.get(fontStyle) : arrCommand[1]) ;
                            }
                            currFontStyle=fontStyle;

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
                            int newFontSize = string2int(arrCommand[1]);
                                // no exception, so args o.k.
                            if (newFontSize<=0)
                            {
                                throw new IllegalArgumentException("illegal fontSize ["+newFontSize+"]: must not be 0 or less.");
                            }
                            currFontSize = newFontSize;
                            if (isOR)
                            {
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+newFontSize;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[1];
                                }
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

                        // Dialog", "DialogInput", "Serif", "SansSerif", "Monospaced"
                        if (arrCommand.length==2 )
                        {
                            font = hmFonts.get(fontNickName.toUpperCase());
                            if (font==null)
                            {
                                try // try to get from a Rexx variable
                                {
                                    font = (Font) getContextVariable(slot, fontNickName);
                                }
                                catch (Throwable t) {}
                                if (font==null)
                                {
                                    String errMsg="font with the supplied nickname \""+fontNickName+"\" is not registered nor is it a Rexx variable referring to a font";
                                    if (isOR)
                                    {
                                        writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                    }
                                    return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-11", errMsg );
                                }
                            }

                            bufGC.setFont(font);
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return resultValue;
                        }

                        // o.k. Font name may contain blanks so use start of third word and all what remains
                        String fontName = command.substring(alWordBoundaries.get(2)[0]);
                        font = new Font(fontName, currFontStyle, currFontSize);
                        // note: it may be the case that the font is not found and a replacement (Dialog.plain) gets returned
                        if (font==null || !font.getFontName().startsWith(fontName) ) // null or not the font sought
                        {
                            Font prevFont=font; // in case we got a font, but it was  not the one sought
                            // 2022-11-02: use Font.decode(): if a registered Font with style and size encoded
                            //             e.g. "Arial-BOLD-18", "Arial BOLD 18", "Arial-BOLD", "Arial 18", "Arial"
                            font = Font.decode(fontName);
                            if (font==null && prevFont==null)
                            {
                                String errMsg="font \""+fontName+"\" could not be loaded";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (fontname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-12", errMsg );
                            }

                            if (font==null)     // font could not be decoded, use previouls replacement font
                            {
                                bufGC.setFont(prevFont);    // set to replacement font, create error
                                String errMsg="font \""+fontName+"\" could not be found, using Java's replacement font";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (fontname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-13", errMsg );
                            }

/* no, do not adjust the current values, they are taken as current default
                            else  // adjust currFontStyle and currFontSize
                            {
                                currFontStyle=font.getStyle();
                                currFontSize =font.getSize();
                            }
*/
                        }
                        if (isOR)
                        {
                            canonical = canonical + " " + fontName;
                        }
                        hmFonts.put(fontNickName.toUpperCase(),font);
                        bufGC.setFont(font);
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
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

                case MOVE_TO:   // query current coordinates or goto x, y co-ordinate
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs no, 1 or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        resultValue = currX+" "+currY;   // query current value (to be returned if change occurs)
                        if (arrCommand.length>1)
                        {
                            int newX = string2int(arrCommand[1]);
                            int newY = newX;    // default to newX in case Y is not supplied
                            if (arrCommand.length==3)
                            {
                                newY = string2int(arrCommand[2]);
                            }
                                // no exception, so args o.k.
                            currX = newX;
                            currY = newY;
                            if (isOR)
                            {
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+currX+" "+currY;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[1]+" "+
                                        (arrCommand.length==3 ? arrCommand[2] : arrCommand[1]);
                                }
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
                        int toX = string2int(arrCommand[1]);
                        int toY = string2int(arrCommand[2]);

                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+toX+" "+toY;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2];
                            }

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
                        int [] pos = (int []) al.get(0);    // get boundaries of first non-blank word
                        if (al.size()<2)    // maybe blanks ?
                        {
                            String str = command.substring(pos[1]); // extract String: skip over first trailing blank
/*
System.err.println("*** -> DRAW_STRING: al.size()<2: nrCommand=["+nrCommand+"] command=["+command+"].length()=["+command.length()+"], pos[0]="+pos[0]+"/pos[1]="+pos[1]+" -> str=["+str+"]");
// bufGC.drawString("",currX,currY);
System.err.println("*** <- drawString([],"+currX+","+currY+") ...");
*/
                            if (command.length()<= (pos[1]+1))    // not even a blank supplied
                            {
                                throw new IllegalArgumentException("this command needs 1 argument (the string to be drawn)");
                            }

                        }
                            // no exception, so args o.k.
                            // to fetch leading blanks we skip over first blank (unlike for other commands where we ignore all blanks after the command)
                        String str = command.substring(pos[1]+1); // extract String: skip over first trailing blank
/*
if (al.size()<2) // blanks supplied
{
    System.err.println("*** <- drawString(str=["+str.replace(" ",".")+"],"+currX+","+currY+") ...");
    System.err.println("*** <- drawString(str=["+str+"],"+currX+","+currY+") ...");
}
*/

                        if (isOR)
                        {
                            writeOutput(slot,canonical+" "+str);
                        }
                        bufGC.drawString(str,currX,currY);
                    }
                    break;


                    // 2022-11-01: return bounds of string with current font and gc
                case STRING_BOUNDS:     // ( "stringBounds"             ) ,    //   "stringBounds string": returns width and height)
                    {
                        // ArrayList<int[]> al = getWordBoundaries(command);   // parse command return word boundaries
                        ArrayList<int[]> al = alWordBoundaries;
                        if (al.size()<2)
                        {
                            throw new IllegalArgumentException("this command needs 1 argument (the string to be measured with current font and graphic context)");
                        }
                            // no exception, so args o.k.
                            // to fetch leading blanks we skip over first blank (unlike for other commands where we ignore all blanks after the command)
                        int [] pos = (int []) al.get(0);
                        String str = command.substring(pos[1]+1);   // extract String: skip over first trailing blank

                        FontMetrics fm = bufGC.getFontMetrics();    // get current font metrics
                        String strResult = null;

                        // int width  = fm.stringWidth(str);
                        // TODO: use getMaxAscent() and getMaxDescent() instead? or use
                        // int height = fm.getAscent() + fm.getDescent();
                        // strResult = width + " " + height;

                        Rectangle2D r2d = fm.getStringBounds(str, bufGC);
                        strResult = r2d.getX()+" "+r2d.getY()+" "+r2d.getWidth()+" "+r2d.getHeight();

                        if (isOR)
                        {
                            writeOutput(slot,canonical+" "+str);
                        }
                        return strResult;
                    }



                case DRAW_OVAL:     // "drawOval width height"
                case FILL_OVAL:
                    {
                        if (arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int width  = string2int(arrCommand[1]);
                        int height = string2int(arrCommand[2]);
                            // no exception, so args o.k.
                        if (cmd==EnumCommand.DRAW_OVAL)
                        {
                            bufGC.drawOval(currX,currY,width,height);
                        }
                        else
                        {
                            bufGC.fillOval(currX,currY,width,height);
                        }
                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+width+" "+height;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2];
                            }

                            writeOutput(slot, canonical);
                        }
                    }
                    break;


                case COPY_AREA:          //   "copyArea width height distX distY"
                    {
                        if (arrCommand.length!=5)
                        {
                            throw new IllegalArgumentException("this command needs exactly 4 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        int width  = string2int(arrCommand[1]);
                        int height = string2int(arrCommand[2]);
                        int dx     = string2int(arrCommand[3]);
                        int dy     = string2int(arrCommand[4]);

                        if (isOR)
                        {
                            canonical = canonical+" "+width+" "+height+" "+dx+" "+dy;
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+width+" "+height+" "+dx+" "+dy;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2]+
                                                      " "+arrCommand[3]+" "+arrCommand[4];
                            }
                        }

                        bufGC.copyArea(currX, currY, width, height, dx, dy);

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        break;
                    }

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
                        boolean isRoundRect = (cmd==EnumCommand.DRAW_ROUND_RECT || cmd==EnumCommand.FILL_ROUND_RECT );
                        if (!isRoundRect && arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        int width  = string2int(arrCommand[1]);
                        int height = string2int(arrCommand[2]);
                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+width+" "+height;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2];
                            }
                        }

                        int arcWidth=-1, arcHeight=-1;
                        if (isRoundRect)
                        {
                            arcWidth  = string2int(arrCommand[3]);
                            arcHeight = string2int(arrCommand[4]);
                            if (isOR)
                            {
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+arcWidth+" "+arcHeight;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[3]+" "+arrCommand[4];
                                }
                            }
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
                        int width  = string2int(arrCommand[1]);
                        int height = string2int(arrCommand[2]);
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
                        if (cmd==EnumCommand.DRAW_RECT)
                        {
                            bufGC.draw3DRect(currX,currY,width,height,raised);
                        }
                        else
                        {
                            bufGC.fill3DRect(currX,currY,width,height,raised);
                        }
                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+width+" "+height;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2];
                            }
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+(raised ? ".true" : ".false");
                            }
                            else    // reuse argument verbatimely
                            {
                                canonical=canonical+" "+strRaised;
                            }
                            // writeOutput(slot, canonical+" "+raised);
                            writeOutput(slot, canonical);
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
                        int width      = string2int(arrCommand[1]);
                        int height     = string2int(arrCommand[2]);
                        int startAngle = string2int(arrCommand[3]);
                        int arcAngle   = string2int(arrCommand[4]);
                            // no exception, so args o.k.
                        if (cmd==EnumCommand.DRAW_ARC)
                        {
                            bufGC.drawArc(currX,currY,width,height,startAngle,arcAngle);
                        }
                        else
                        {
                            bufGC.fillArc(currX,currY,width,height,startAngle,arcAngle);
                        }
                        if (isOR)
                        {
                            if (bUseInt4numbers)
                            {
                                canonical = canonical+" "+width+" "+height+" "+startAngle+" "+arcAngle;
                            }
                            else
                            {
                                canonical = canonical+" "+arrCommand[1]+" "+arrCommand[2]+
                                                      " "+arrCommand[3]+" "+arrCommand[4];
                            }
                            writeOutput(slot, canonical);
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

                case ROTATE:       // "rotate theta [x y]" query or set rotation
                    {
                        ArrayList<int[]> al = alWordBoundaries;
                        int argNum = al.size();

                        if (argNum!=2 && argNum!=4)
                        {
                            throw new IllegalArgumentException("this command needs exactly one or three arguments, received "+(arrCommand.length-1)+" instead");
                        }
                        double theta = 0, radiansTheta = 0;
                        if (argNum>1) // at least theta given
                        {
                            theta = Double.parseDouble(arrCommand[1]);
                            if (isOR)
                            {
                                canonical=canonical+" "+theta;
                            }
                            radiansTheta = Math.toRadians(theta);  // turn degree to radians
                            if (radiansTheta!=0)
                            {
                                if (argNum==2)    // just rotate by theta
                                {
                                    bufGC.rotate(radiansTheta);
                                }
                                else    // if 3 arguments, fetch arg 2 (x) and 3 (y)
                                {
                                    int x = string2int(arrCommand[2]);
                                    int y = string2int(arrCommand[3]);
                                    if (isOR)
                                    {
                                        if (bUseInt4numbers)
                                        {
                                            canonical = canonical+" "+x+" "+y;
                                        }
                                        else
                                        {
                                            canonical = canonical+" "+arrCommand[2]+" "+arrCommand[3];
                                        }
                                    }
                                    bufGC.rotate(radiansTheta, x, y);
                                }
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }


                    // rgf, 2022-10-21: command to allow drawing an image referred to by a rexx Variable
//                case DRAW_IMAGE_FROM_REXX_VAR:   // "drawImageFromVar rexxVarName [bkgColor] | rexxVarName width height [bkgColor] | rexxVarName width height srcX1 srcY1 srcX2 srcY2 [bkgColor]"
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
                        if (cmd==EnumCommand.DRAW_IMAGE)   // fetch from the image registry
                        {
                            img = hmImages.get(name.toUpperCase());
                            if (img==null)
                            {
                                try
                                {
                                    img = (Image) getContextVariable(slot, name);   // try to get from a Rexx variable
                                }
                                catch (Throwable t) {}
                                if (img==null)
                                {
                                    throw new IllegalArgumentException("image with the name \""+name+"\" not found, you must first use \"loadImage "+name+" filename\" or \"pushImage "+name+"\" or assign an image to a Rexx variable named \""+name+"\"");
                                }
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
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-14", errMsg );
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
                                width = string2int(arrCommand[2]);
                                height= string2int(arrCommand[3]);
                                if (argNum>5)
                                {
                                    srcX1 = string2int(arrCommand[4]);
                                    srcY1 = string2int(arrCommand[5]);
                                    srcX2 = string2int(arrCommand[6]);
                                    srcY2 = string2int(arrCommand[7]);
                                }
                                if (isOR)
                                {
                                    // canonical=canonical+" "+width+" "+height;
                                    if (bUseInt4numbers)
                                    {
                                        canonical = canonical+" "+width+" "+height;
                                    }
                                    else
                                    {
                                        canonical = canonical+" "+arrCommand[2]+" "+arrCommand[3];
                                    }

                                    if (argNum==5)        // bkgColor
                                    {
                                        canonical=canonical+" "+colorNickName;
                                    }
                                    else if (argNum>5)
                                    {
                                        // canonical=canonical+" "+srcX1+" "+srcY1+" "+srcX2+" "+srcY2;
                                        if (bUseInt4numbers)
                                        {
                                            canonical = canonical+" "+srcX1+" "+srcY1+" "+srcX2+" "+srcY2;
                                        }
                                        else
                                        {
                                            canonical = canonical+" "+arrCommand[4]+" "+arrCommand[5]+
                                                                      arrCommand[6]+" "+arrCommand[7];
                                        }

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

                case TRANSLATE: // "translate x [y]" query or move origin; if y omitted, uses x
                    {
                        if (arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs no, 1 or 2 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                            // get current settings
                        AffineTransform at=bufGC.getTransform();
                        String strResult=null;
                        Integer newX=null, newY=null;

                        strResult=at.getTranslateX()+" "+at.getTranslateY();
                        if (arrCommand.length>1)
                        {
                            newX = string2int(arrCommand[1]);
                            newY = newX;    // default to newX
                            if (arrCommand.length==3)
                            {
                                newY = string2int(arrCommand[2]);
                            }
                                // no exception, so args o.k.
                            bufGC.translate(newX,newY);
                        }

                        if (isOR)
                        {
                            if (arrCommand.length>1)
                            {
                                // canonical = canonical+" "+newX+" "+newY;
                                if (bUseInt4numbers)
                                {
                                    canonical = canonical+" "+newX+" "+newY;
                                }
                                else
                                {
                                    canonical = canonical+" "+arrCommand[1]+" "+
                                        (arrCommand.length==3 ? arrCommand[2] : arrCommand[1]);
                                }
                            }
                            writeOutput(slot, canonical);
                        }
                        return strResult;   // return previous setting
                    }


                    // 2022-10-28: "transform { | RESET | translateX translateY scaleX scaleY shearX shearY}" query, change reset Graphics2D's AffineTransform
                    // 2022-12-07: additional option: "transform name translateX translateY scaleX scaleY shearX shearY}", returns AffineTransform
                case TRANSFORM:
                    {
                        int argNum = arrCommand.length;
                        if ((argNum>2 && argNum<7) || argNum>8)
                        {
                            throw new IllegalArgumentException("this command needs exactly no, 1, 6, or 7 arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        String strReset="RESET";
                        boolean isReset=false;
                        String nickName="";
                        String ucNickName="";
                        AffineTransform at = bufGC.getTransform();  // get current settings
                        double translateX=at.getTranslateX(),
                               translateY=at.getTranslateY(),
                               scaleX    =at.getScaleX(),
                               scaleY    =at.getScaleY(),
                               shearX    =at.getShearX(),
                               shearY    =at.getShearY();

                        resultValue=translateX+" "+translateY+" "+
                                         scaleX+" "+scaleY+" "+
                                         shearX+" "+shearY;

                        if (argNum>1)
                        {
                            AffineTransform newAT=null;
                            nickName=arrCommand[1];
                            if (argNum==2)  // reset or query stored AffineTransform ?
                            {
                                ucNickName=nickName.toUpperCase();
                                if (ucNickName.equals(strReset))
                                {
                                    newAT = new AffineTransform();  // uses the default values tX=tY=shX=shY=0 and scX=scY=1
                                    isReset=true;
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+strReset;
                                    }
                                }
                                else    // try to get it from cache
                                {
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+nickName;
                                    }
                                    at=hmTransforms.get(ucNickName);
                                    if (at==null)
                                    {
                                        if (startRexxVariableChar.indexOf(ucNickName.charAt(0))>=0) // a Rexx variable?
                                        {
                                            try
                                            {
                                                at=(AffineTransform) getContextVariable(slot, ucNickName);
                                            }
                                            catch (Throwable t) {}

                                            if (at==null)
                                            {
                                                throw new IllegalArgumentException("no transform with name \""+nickName+"\" stored, nor a Rexx variable that refers to an AffineTransform object");
                                            }
                                        }
                                    }
                                    return at;  // return AffineTransform object
                                }
                            }
                            else if (argNum>6)
                            {
// System.err.println("*** argNum>6: argNum=["+argNum+"] | command # "+nrCommand+": ["+command+"]");
                                if (argNum==7)
                                {
                                    // a dot keeps the current value
                                    translateX = arrCommand[1].equals(".") ? translateX : Double.parseDouble(arrCommand[1]);
                                    translateY = arrCommand[2].equals(".") ? translateY : Double.parseDouble(arrCommand[2]);
                                    scaleX     = arrCommand[3].equals(".") ? scaleX     : Double.parseDouble(arrCommand[3]);
                                    scaleY     = arrCommand[4].equals(".") ? scaleY     : Double.parseDouble(arrCommand[4]);
                                    shearX     = arrCommand[5].equals(".") ? shearX     : Double.parseDouble(arrCommand[5]);
                                    shearY     = arrCommand[6].equals(".") ? shearY     : Double.parseDouble(arrCommand[6]);
                                }
                                else // if (argNum==8)   // we need to store it!
                                {
                                    ucNickName=nickName.toUpperCase();
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+nickName;  // show name to use
                                    }
                                    translateX = arrCommand[2].equals(".") ? translateX : Double.parseDouble(arrCommand[2]);
                                    translateY = arrCommand[3].equals(".") ? translateY : Double.parseDouble(arrCommand[3]);
                                    scaleX     = arrCommand[4].equals(".") ? scaleX     : Double.parseDouble(arrCommand[4]);
                                    scaleY     = arrCommand[5].equals(".") ? scaleY     : Double.parseDouble(arrCommand[5]);
                                    shearX     = arrCommand[6].equals(".") ? shearX     : Double.parseDouble(arrCommand[6]);
                                    shearY     = arrCommand[7].equals(".") ? shearY     : Double.parseDouble(arrCommand[7]);
                                }


                                // matrix:  { m00/sX m10/shY m01/shX m11/sY m02/tX m12/tY }
                                newAT=new AffineTransform(
                                    scaleX, shearY, shearX, scaleY, translateX, translateY
                                    );
                            }

                            if (isReset || argNum==7)
                            {
                                bufGC.setTransform(newAT);  // assign new AffineTransform
                            }
                            else    // return AffineTransform
                            {
                                hmTransforms.put(ucNickName,newAT);
                                resultValue=newAT;
// System.err.println("*** argNum>6: ucNickName=["+ucNickName+"], newAT=["+newAT+"]");
                            }
                        }

                        if (isOR)
                        {
                            if (arrCommand.length==7)
                            {
                                canonical = canonical+" "+translateX+" "+translateY+" "+
                                                          scaleX+" "+scaleY+" "+
                                                          shearX+" "+shearY;
                            }
                            writeOutput(slot, canonical);
                        }

                        return resultValue;
                    }



                case PUSH_GC:   // pushes current GC, does a create() & assigns the new one as current
                case POP_GC:    // pops and & assigns as current
                    {
                        if (arrCommand.length!=1)
                        {
                            throw new IllegalArgumentException("this command does not expect arguments, received "+(arrCommand.length-1)+" argument(s)");
                        }

                        resultValue = bufGC;   // query current value (to be returned if change occurs)

                        if (cmd==EnumCommand.PUSH_GC)
                        {
                            Graphics2D tmpG2D = bufGC;
                            adGCStack.push(bufGC);
                            bufGC=(Graphics2D) bufGC.create();  // work on an independent copy from now on
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
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
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-15", errMsg );
                            }
                            bufGC.dispose();        // dispose current GC
                            bufGC=d2d;              // replace with previous GC
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            resultValue=bufGC;  // old one is disposed, so cannot use that anymore
                        }
                        return resultValue;
                    }

                    // PUSH_IMAGE: "pushImage [nickName]" push current image displayed, if nickName store it in image HashMap as well
                case PUSH_IMAGE:   // pushes current one, does a create() & assigns the new one as current
                case POP_IMAGE:    // pops and & assigns as current
                    {
                        if ( cmd==EnumCommand.POP_IMAGE && arrCommand.length!=1 )
                        {
                            throw new IllegalArgumentException("this command does not expect arguments, received "+(arrCommand.length-1)+" argument(s)");
                        }
                        else if ( cmd==EnumCommand.PUSH_IMAGE && arrCommand.length>2 )
                        {
                            throw new IllegalArgumentException("this command expects 1 argument at the most (nickName), received "+(arrCommand.length-1)+" argument(s)");
                        }


                        if (cmd==EnumCommand.PUSH_IMAGE)
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
                        }
                        else    // POP_IMAGE: the returned image should be used to draw via current GC
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
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-16", errMsg );
                            }

                            // bufGC.clearRect(0,0,bufImage.getWidth(),bufImage.getHeight());  // clear rectangle
                            bufGC.drawImage(img,0,0,null);
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            resultValue = img;     // return popped img, not the draw result
                            break;  // update UI if shown
                        }
                    }

                case ASSIGN_RC:     //   "assignRC RexxVariable": assigns current value of RC to "RexxVariable": allows self contained macros
                    {
                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 (Rexx variable name) argument (the name of the shape and optionally the new winding rule argument), received "+(arrCommand.length-1)+" instead");
                        }

                        String rxVarName=arrCommand[1];
                        if (startRexxVariableChar.indexOf(rxVarName.charAt(0))<0) // a Rexx variable?
                        {
                            throw new IllegalArgumentException("argument ["+rxVarName+"] is not a valid Rexx symbol for a Rexx variable name");
                        }

                        resultValue = getContextVariable(slot, "RC");   // get the current value of variable RC
                        setContextVariable(slot, rxVarName, resultValue);
                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+rxVarName);
                        }
                        break;
                    }

                    // "shape name [type args...]"
                case SHAPE:
                    {
                        int argNum=arrCommand.length;
                        if (arrCommand.length<2)
                        {
                            throw new IllegalArgumentException("this command needs more than "+arrCommand.length+" arguments");
                        }
                        String nickName=arrCommand[1];
                        String ucNickName=nickName.toUpperCase();
                        if (isOR)
                        {
                            canonical=canonical+" "+nickName;
                        }
                        Shape  sh=null;
                        if (argNum==2)      // query Shape from registry
                        {
                            sh=hmShapes.get(ucNickName);
                            if (sh==null)
                            {
                                String errMsg="Shape with the supplied nickname \""+nickName+"\" is not registered";
                                if (isOR)
                                {
                                    writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                }
                                return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-17", errMsg );
                            }
                            if (isOR)
                            {
                                writeOutput(slot, canonical);
                            }
                            return sh;     // return Shape
                        }

                        // create and store a Shape
                        String strShapeType=arrCommand[2];
                        EnumShape eShape=EnumShape.getShape(strShapeType);
                        if (eShape==null)
                        {
                            String errMsg="Shape type \""+strShapeType+"\" is not supported";
                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-18", errMsg );
                        }
                        String canonicalShapeType=eShape.getMixedCase();
                        canonical=canonical+" "+canonicalShapeType; // get canoncial shape type
                        switch (eShape)
                        {

                            case SHAPE_AREA:    //  "SHAPE areaNickName AREA shapeNickName" (can be an Area)
                                {
                                    if (argNum!=4)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs exactly two arguments (\"areaNickName shapeNickName\"), however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (wrong number of arguments for \""+canonicalShapeType+"\" shape): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-19", errMsg );
                                    }
                                    // check whether shapeNickName exists
                                    String strShapeArg2=arrCommand[3];
                                    Shape arg2shape=(Shape) hmShapes.get(strShapeArg2.toUpperCase());
                                    if (arg2shape==null)
                                    {
                                        String errMsg="area argument named \""+strShapeArg2+"\" is not registered";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (area shape argument): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-20", errMsg );
                                    }
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+strShapeArg2;
                                    }
                                    sh = new Area(arg2shape);
                                    break;
                                }
                                                //  Double(double x, double y, double w, double h, double start, double extent, int type)
                            case SHAPE_ARC2D:   //  Double(Rectangle2D ellipseBounds             , double start, double extent, int type)
                                {
                                    if (argNum!=7 && argNum!=10)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs exactly 4 (rectangle2d start extent type) or 7 arguments (x y w h start extent type), however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (wrong number of arguments for \""+canonicalShapeType+"\" shape): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-21", errMsg );
                                    }

                                    Rectangle2D r2d=null;
                                    double x=-1.0;
                                    double y=-1.0;
                                    double h=-1.0;
                                    double w=-1.0;
                                    double start =-1.0;
                                    double extent=-1.0;
                                    int    type=-1;
                                    String strType=null;

                                    if (argNum==7)
                                    {
                                        String strArg3=arrCommand[3];
                                        r2d=(Rectangle2D) hmShapes.get(strArg3.toUpperCase());
                                        if (r2d==null)  // not registered, try Rexx variable instead
                                        {
                                            try
                                            {
                                                r2d=(Rectangle2D) getContextVariable(slot,strArg3);
                                            }
                                            catch (Throwable t) {}
                                        }
                                        if (r2d==null)
                                        {
                                            String errMsg="rectangle2d argument named \""+strArg3+"\" is not registered nor is it a Rexx variable referring to a Rectangle2D shape";
                                            if (isOR)
                                            {
                                                writeOutput(slot, "-- ERROR (rectangle2d argument): ["+command+"]");
                                            }
                                            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-22", errMsg );
                                        }
                                        if (isOR)
                                        {
                                            canonical=canonical+" "+strArg3;
                                        }
                                        start =Double.parseDouble(arrCommand[4]);
                                        extent=Double.parseDouble(arrCommand[5]);
                                        strType=arrCommand[6];
                                    }
                                    else
                                    {
                                        x=Double.parseDouble(arrCommand[3]);
                                        y=Double.parseDouble(arrCommand[4]);
                                        h=Double.parseDouble(arrCommand[5]);
                                        w=Double.parseDouble(arrCommand[6]);
                                        if (isOR)
                                        {
                                            canonical=canonical+" "+x+" "+y+" "+h+" "+w;
                                        }
                                        start =Double.parseDouble(arrCommand[7]);
                                        extent=Double.parseDouble(arrCommand[8]);
                                        strType=arrCommand[9];
                                    }

                                    // check validy of type argument!
                                    String ucStringType = strType.toUpperCase();
                                    if (startRexxVariableChar.indexOf(ucStringType.charAt(0))>=0)    // a symbolic name?
                                    {
                                        if (!arcClosures.containsKey(ucStringType))
                                        {
                                            throw new IllegalArgumentException("unknown value for \"type\" argument supplied: ["+strType+"]");
                                        }
                                        type=arcClosures.get(ucStringType);
                                    }
                                    else // verbatim int type
                                    {
                                        type=string2int(strType);
                                        if (!arcClosures.containsValue(type))
                                        {
                                            throw new IllegalArgumentException("unknown value for \"type\" argument supplied: ["+strType+"]");
                                        }
                                    }

                                    if (isOR)
                                    {
                                        canonical=canonical+" "+start+" "+extent+" ";
                                        if (bUseNames4canonical)
                                        {
                                            canonical=canonical+arcClosuresInt2Name.get(type);
                                        }
                                        else
                                        {
                                            canonical=canonical+type;
                                        }
                                    }

                                    if (argNum==7)
                                    {
                                        sh=new Arc2D.Double(r2d, start, extent, type);
                                    }
                                    else
                                    {
                                        sh=new Arc2D.Double(x, y, w, h, start, extent, type);
                                    }
                                    break;
                                }

                            case SHAPE_CUBIC_CURVE2D:   // Double(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x2, double y2)
                                {
                                    if (argNum!=11)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs exactly 8 arguments, however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-23", errMsg );
                                    }
                                    double arg1=Double.parseDouble(arrCommand[3]);
                                    double arg2=Double.parseDouble(arrCommand[4]);
                                    double arg3=Double.parseDouble(arrCommand[5]);
                                    double arg4=Double.parseDouble(arrCommand[6]);
                                    double arg5=Double.parseDouble(arrCommand[7]);
                                    double arg6=Double.parseDouble(arrCommand[8]);
                                    double arg7=Double.parseDouble(arrCommand[9]);
                                    double arg8=Double.parseDouble(arrCommand[10]);
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+arg1+" "+arg2+" "+arg3+" "+arg4+" "+arg5+" "+arg6+" "+arg7+" "+arg8;
                                    }
                                    sh=new CubicCurve2D.Double(arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8);

                                    break;
                                }

                            case SHAPE_ELLIPSE2D:   // Double(double x, double y, double w, double h)
                            case SHAPE_LINE2D:      // Double(double x1, double y1, double x2, double y2)
                            case SHAPE_RECTANGLE2D: // Double(double x, double y, double w, double h)
                                {
                                    if (argNum!=7)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs exactly 4 arguments, however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-24", errMsg );
                                    }
                                    double arg1=Double.parseDouble(arrCommand[3]);
                                    double arg2=Double.parseDouble(arrCommand[4]);
                                    double arg3=Double.parseDouble(arrCommand[5]);
                                    double arg4=Double.parseDouble(arrCommand[6]);

                                    if (eShape==EnumShape.SHAPE_ELLIPSE2D)
                                    {
                                        sh=new Ellipse2D.Double(arg1,arg2,arg3,arg4);
                                    }
                                    else if (eShape==EnumShape.SHAPE_LINE2D)
                                    {
                                        sh=new Line2D.Double(arg1,arg2,arg3,arg4);
                                    }
                                    else
                                    {
                                        sh=new Rectangle2D.Double(arg1,arg2,arg3,arg4);
                                    }
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+arg1+" "+arg2+" "+arg3+" "+arg4;
                                    }
                                    break;
                                }

                            case SHAPE_POLYGON:     // Polygon(int[] xpoints, int[] ypoints, int npoints)
                                {
                                    if (arrCommand.length!=6)
                                    {
                                        throw new IllegalArgumentException("this command needs exactly 3 (xPointsArray yPointsArray nPoints) arguments, received "+(arrCommand.length-3)+" instead");
                                    }

                                    int [] xPoints=null, yPoints=null;
                                    int    nPoints=0;

                                    String strXPoints=arrCommand[3];
                                    if (startRexxVariableChar.indexOf(strXPoints.charAt(0))>=0) // a Rexx variable?
                                    {
                                        xPoints=(int []) getContextVariable(slot, strXPoints);
                                    }
                                    else    // a Rexx array expression: comma separated list of ints in parentheses (no blanks!)
                                    {
                                        xPoints=RexxArrayExpressionToIntArray(strXPoints,0);
                                    }

                                    String strYPoints=arrCommand[4];
                                    if (startRexxVariableChar.indexOf(strYPoints.charAt(0))>=0) // a Rexx variable?
                                    {
                                        yPoints=(int []) getContextVariable(slot, strYPoints);
                                    }
                                    else    // a Rexx array expression: comma separated list of floats in parentheses (no blanks!)
                                    {
                                        yPoints=RexxArrayExpressionToIntArray(strYPoints,0);
                                    }

                                    String strNPoints=arrCommand[5];
                                    nPoints = Integer.parseInt(strNPoints);
                                    if (isOR)
                                    {



                                        // canonical=canonical+" "+strXPoints+" "+strYPoints+" "+strNPoints;
                                        canonical=canonical+
                                                        " "+intArrayToRexxArrayExpression(xPoints)+
                                                        " "+intArrayToRexxArrayExpression(yPoints)+
                                                        " "+strNPoints;
                                    }
                                    sh=new Polygon(xPoints, yPoints, nPoints);
                                    break;
                                }

                            case SHAPE_QUAD_CURVE2D:        //  Double(double x1, double y1, double ctrlx, double ctrly, double x2, double y2)
                            case SHAPE_ROUND_RECTANGLE2D:   // Double(double x, double y, double w, double h, double arcWidth, double arcHeight)
                                {
                                    if (argNum!=9)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs exactly 6 arguments, however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-25", errMsg );
                                    }
                                    double arg1=Double.parseDouble(arrCommand[3]);
                                    double arg2=Double.parseDouble(arrCommand[4]);
                                    double arg3=Double.parseDouble(arrCommand[5]);
                                    double arg4=Double.parseDouble(arrCommand[6]);
                                    double arg5=Double.parseDouble(arrCommand[7]);
                                    double arg6=Double.parseDouble(arrCommand[8]);
                                    if (isOR)
                                    {
                                        canonical=canonical+" "+arg1+" "+arg2+" "+arg3+" "+arg4+" "+arg5+" "+arg6;
                                    }

                                    if (eShape==EnumShape.SHAPE_QUAD_CURVE2D)
                                    {
                                        sh=new QuadCurve2D.Double(arg1,arg2,arg3,arg4,arg5,arg6);
                                    }
                                    else
                                    {
                                        sh=new RoundRectangle2D.Double(arg1,arg2,arg3,arg4,arg5,arg6);
                                    }
                                    break;
                                }

                            case SHAPE_PATH2D:   // shape shapeName pa[th[2d]] [WIND_NON_ZERO=1]
                                {
                                    if (argNum<3 || argNum>4)
                                    {
                                        String errMsg="Shape \""+canonicalShapeType+"\" needs no or exactly 1 argument, however there are "+(argNum-3)+" supplied";
                                        if (isOR)
                                        {
                                            writeOutput(slot, "-- ERROR (nickname argument): ["+command+"]");
                                        }
                                        return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-26", errMsg );
                                    }

                                    int windingType=1;  // default to WIND_NON_ZERO (1)

                                    if (argNum==4)
                                    {
                                        String strType=arrCommand[3];
                                        // check validy of type argument!
                                        String ucStringType = strType.toUpperCase();
                                        if (startRexxVariableChar.indexOf(ucStringType.charAt(0))>=0)    // a symbolic name?
                                        {
                                            if (!windingRules.containsKey(ucStringType))
                                            {
                                                throw new IllegalArgumentException("unknown value for \"windingType\" argument supplied: ["+strType+"]");
                                            }
                                            windingType=windingRules.get(ucStringType);
                                        }
                                        else // verbatim int type
                                        {
                                            windingType=string2int(strType);
                                            if (!windingRules.containsValue(windingType))
                                            {
                                                throw new IllegalArgumentException("unknown value for \"windingType\" argument supplied: ["+strType+"]");
                                            }
                                        }
                                    }

                                    if (isOR)
                                    {
                                        if (bUseNames4canonical)
                                        {
                                            canonical=canonical+" "+windingRulesInt2Name.get(windingType);
                                        }
                                        else
                                        {
                                            canonical=canonical+" "+windingType;
                                        }
                                    }
                                    sh = new Path2D.Double(windingType);
                                    break;
                                }

                            default:
                                {
                                    String errMsg="shape type \""+canonicalShapeType+"\" not known/implemented";
                                    return createCondition (slot, nrCommand, command, ConditionType.FAILURE, "-27", errMsg );
                                }
                        }
                        hmShapes.put(ucNickName,sh);
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return sh;      // return the shape
                    }
                    // "drawShape name
                case DRAW_SHAPE:
                    // "fillShape name
                case FILL_SHAPE:
                    // "clipShape name
                case CLIP_SHAPE:
                    {
                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 1 argument, received "+(arrCommand.length-1)+" instead");
                        }
                        String nickName=arrCommand[1];

                        Shape sh=hmShapes.get(nickName.toUpperCase());

                        if (sh==null)
                        {
                            if (startRexxVariableChar.indexOf(nickName.charAt(0))>=0) // a Rexx variable?
                            {
                                sh=(Shape) getContextVariable(slot, nickName);
                                if (sh==null)
                                {
                                    throw new IllegalArgumentException("no shape with name \""+nickName+"\" stored, nor a Rexx variable that refers to a Shape object");
                                }
                            }
                        }

                        if (EnumCommand.DRAW_SHAPE==cmd)
                        {
                            bufGC.draw(sh);
                        }
                        else
                        if (EnumCommand.FILL_SHAPE==cmd)
                        {
                            bufGC.fill(sh);
                        }
                        else    // CLIP_SHAPE
                        {
                            bufGC.clip(sh);
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+nickName);
                        }
                        break;
                    }


                    // 2022-12-07: return bounds for any supported shape
                case SHAPE_BOUNDS:      // "shapeBounds shapeName" ... getBounds2D()
                    {
                        if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument (the name of the shape), received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName=arrCommand[1];
                        Shape sh=hmShapes.get(nickName.toUpperCase());
                        if (sh==null)
                        {
                            throw new IllegalArgumentException("no shape with name \""+nickName+"\" stored");
                        }

                        Rectangle2D r2d = sh.getBounds2D();
                        String strResult = r2d.getX()+" "+r2d.getY()+" "+r2d.getWidth()+" "+r2d.getHeight();

                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+nickName);
                        }
                        return strResult;
                    }

                        // AREA-command areaNickname shapeNickname (can be an area)
                case AREA_ADD:
                case AREA_EXCLUSIVE_OR:
                case AREA_INTERSECT:
                case AREA_SUBTRACT:
                case AREA_TRANSFORM:        // areaTransform areaNickname transformNickname
                    {
                        if (arrCommand.length!=3)
                        {
                            if (cmd!=EnumCommand.AREA_TRANSFORM)
                            {
                                throw new IllegalArgumentException("this command needs exactly 2 arguments (the name of the area and area/shape to be used as argument for the operation), received "+(arrCommand.length-1)+" instead");
                            }
                            throw new IllegalArgumentException("this command needs exactly 2 arguments (the name of the area and the nickname of a transform to be applied to it), received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName1=arrCommand[1];     // areaNickname
                        Area   tmpArea1=(Area) hmShapes.get(nickName1.toUpperCase());

                        if (tmpArea1==null)
                        {
                            throw new IllegalArgumentException("no Area shape with name \""+nickName1+"\" stored");
                        }

                        String nickName2=arrCommand[2];     // {transform|shape}NickName

                        if (isOR)
                        {
                            canonical = canonical+" "+nickName1+" "+nickName2;
                            writeOutput(slot, canonical);
                        }

                        if (cmd==EnumCommand.AREA_TRANSFORM)
                        {
                            AffineTransform at=hmTransforms.get(nickName2.toUpperCase());
                            if (at==null)
                            {
                                if (startRexxVariableChar.indexOf(nickName2.charAt(0))>=0) // a Rexx variable?
                                {
                                    at=(AffineTransform) getContextVariable(slot, nickName2);
                                }
                                if (at==null)
                                {
                                    throw new IllegalArgumentException("no transform with name \""+nickName2+"\" stored, nor a Rexx variable that refers to a transform object");
                                }
                            }
                            tmpArea1.transform(at); // carry out transform
                        }
                        else
                        {
                            Shape tmpShape=hmShapes.get(nickName2.toUpperCase());
                            if (tmpShape==null)
                            {
                                if (startRexxVariableChar.indexOf(nickName2.charAt(0))>=0) // a Rexx variable?
                                {
                                    tmpShape=(Shape) getContextVariable(slot, nickName2);
                                }
                                if (tmpShape==null)
                                {
                                    throw new IllegalArgumentException("no Area or Shape with name \""+nickName2+"\" stored, nor a Rexx variable that refers to a shape object");
                                }
                            }

                            Area tmpAreaRHS = (tmpShape instanceof Area) ? (Area) tmpShape : new Area(tmpShape);
                            if (cmd==EnumCommand.AREA_ADD)
                            {
                                tmpArea1.add(tmpAreaRHS);
                            }
                            else if (cmd==EnumCommand.AREA_EXCLUSIVE_OR)
                            {
                                tmpArea1.exclusiveOr(tmpAreaRHS);
                            }
                            else if (cmd==EnumCommand.AREA_INTERSECT)
                            {
                                tmpArea1.intersect(tmpAreaRHS);
                            }
                            else if (cmd==EnumCommand.AREA_SUBTRACT)
                            {
                                tmpArea1.subtract(tmpAreaRHS);
                            }
                        }
                        resultValue=tmpArea1;
                        break;
                    }


                case PATH_APPEND:       //   "pathAppend pathName shapeName {Shape|PathIterator-via-Rexx-variable} [connect=.true]"
                    {
                        if (arrCommand.length<3 || arrCommand.length>4)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 or 3 arguments (the name of the shape and optionally the boolean connect argument), received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName=arrCommand[1];
                        Path2D path=(Path2D) hmShapes.get(nickName.toUpperCase());

                        if (path==null)
                        {
                            throw new IllegalArgumentException("no Path2D shape with name \""+nickName+"\" stored");
                        }

                        String nickName2=arrCommand[2];
                        // Shape sh=(Shape) hmShapes.get(nickName2.toUpperCase());
                        Object sh=(Shape) hmShapes.get(nickName2.toUpperCase());
                        if (sh==null)
                        {
                            sh = getContextVariable(slot, nickName2); // try to get from a Rexx variable
                            if (sh==null || (! (sh instanceof Shape || sh instanceof PathIterator)) )
                            {
                                throw new IllegalArgumentException("no shape with name \""+nickName2+"\" stored, nor a Rexx variable pointing to a Shape or PathIterator");
                            }
                        }

                        boolean bConnect=true;      // default: connect new shape (lineTo instead of moveTo)
                        String  newValue="1";
                        if (arrCommand.length==4)   // connect argument supplied!
                        {
                            newValue=arrCommand[3];
                            if (checkBooleanValue(newValue))   // a valid BSF4ooRexx850 boolean value?
                            {
                                bConnect=getBooleanValue(newValue);  // get value
                            }
                            else
                            {
                                throw new IllegalArgumentException("the supplied \"connect\" argument \""+newValue+"\" is not a valid BSF4ooRexx850 boolean value, valid values (in any case) are: "
                                                      + "\"0\", \"1\", \"false\", \"true\", \".false\", \".true\"");
                            }
                            // bConnect = getBooleanValue(arrCommand[3]);
                        }

                        if (sh instanceof Shape)
                        {
                            path.append((Shape) sh,bConnect);
                        }
                        else
                        {
                            path.append((PathIterator) sh,bConnect);
                        }

                        if (isOR)
                        {
                            // writeOutput(slot, canonical+" "+nickName+" "+nickName2+" "+(bConnect ? "1" : "0"));
                            canonical = canonical+" "+nickName+" "+nickName2;
                            if (bUseNames4canonical)
                            {
                                canonical=canonical+" "+(bConnect ? ".true" : ".false");
                            }
                            else    // reuse argument verbatimely
                            {
                                canonical=canonical+" "+newValue;
                            }
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }

                case PATH_CLOSE:        //   "pathClose shapeName" ... closePath()
                case PATH_RESET:        //   "pathReset shapeName"
                case PATH_CURRENT_POINT://   "pathCurrentPoint shapeName" | ... getCurrentPoint()
                case PATH_CLONE:        //   "pathClone shapeName [newShapeName]"
                    {
                        if (cmd==EnumCommand.PATH_CLONE)
                        {
                            if (arrCommand.length<2 && arrCommand.length>3)
                            {
                                throw new IllegalArgumentException("this command needs exactly 1 or 2 arguments (the name of the Path2D shape), received "+(arrCommand.length-1)+" instead");
                            }
                        }
                        else if (arrCommand.length!=2)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 argument (the name of the Path2D shape), received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName=arrCommand[1];
                        Path2D path=(Path2D) hmShapes.get(nickName.toUpperCase());

                        if (path==null)
                        {
                            throw new IllegalArgumentException("no Path2D shape with name \""+nickName+"\" stored");
                        }

                        if (isOR)
                        {
                            canonical=canonical+" "+nickName;
                        }

                        if (cmd==EnumCommand.PATH_CLOSE)
                        {
                            path.closePath();
                        }
                        else if (cmd==EnumCommand.PATH_RESET)
                        {
                            path.reset();
                        }
                        else if (cmd==EnumCommand.PATH_CURRENT_POINT)
                        {
                            Point2D r2d=path.getCurrentPoint();
                            if (r2d == null)    // no current point?
                            {
                                resultValue=getNil(slot);   // get and assign .nil
                            }
                            else
                            {
                                resultValue=r2d.getX()+" "+r2d.getY();
                            }
                        }
                        else    // PATH_CLONE: may have a second argument
                        {
                            resultValue=path.clone();
                            if (arrCommand.length==3)   // save the clone?
                            {
                                String nickName2=arrCommand[2];
                                String ucNickName2=nickName2.toUpperCase();
                                hmShapes.put(ucNickName2,(Shape) resultValue);
                                if (isOR)
                                {
                                    canonical=canonical+" "+nickName2;
                                }
                            }
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }

                case PATH_LINE_TO:      //   "pathLineTo shapeName x y"
                case PATH_MOVE_TO:      //   "pathMoveTo shapeName x y"
                case PATH_QUAD_TO:      //   "pathQuadTo shapeName x1 y1 x2 y2"
                case PATH_CURVE_TO:     //   "pathCurveTo shapeName x1 y1 x2 y2 x3 y3"
                    {
                        int argNum=-1;
                        if (cmd==EnumCommand.PATH_LINE_TO || cmd==EnumCommand.PATH_MOVE_TO)
                        {
                            argNum=3;
                        }
                        else if (cmd==EnumCommand.PATH_QUAD_TO)
                        {
                            argNum=5;
                        }
                        else    // PATH_CURVE_TO
                        {
                            argNum=7;
                        }

                        if (arrCommand.length!=(argNum+1))
                        {
                            throw new IllegalArgumentException("this command needs exactly "+argNum+" arguments, received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName=arrCommand[1];
                        Path2D path=(Path2D) hmShapes.get(nickName.toUpperCase());

                        if (path==null)
                        {
                            throw new IllegalArgumentException("no Path2D shape with name \""+nickName+"\" stored");
                        }

                        double arg1 = Double.parseDouble(arrCommand[2]);
                        double arg2 = Double.parseDouble(arrCommand[3]);

                        if (isOR)
                        {
                            canonical=canonical+" "+nickName+" "+arg1+" "+arg2;
                        }

                        if (cmd==EnumCommand.PATH_LINE_TO)
                        {
                            path.lineTo(arg1,arg2);
                        }
                        else if (cmd==EnumCommand.PATH_MOVE_TO)
                        {
                            path.moveTo(arg1,arg2);
                        }
                        else
                        {
                            double arg3 = Double.parseDouble(arrCommand[4]);
                            double arg4 = Double.parseDouble(arrCommand[5]);
                            if (isOR)
                            {
                                canonical=canonical+" "+arg3+" "+arg4;
                            }
                            if (cmd==EnumCommand.PATH_QUAD_TO)
                            {
                                path.quadTo(arg1,arg2,arg3,arg4);
                            }
                            else
                            {
                                double arg5 = Double.parseDouble(arrCommand[6]);
                                double arg6 = Double.parseDouble(arrCommand[7]);
                                if (isOR)
                                {
                                    canonical=canonical+" "+arg5+" "+arg6;
                                }
                                path.curveTo(arg1,arg2,arg3,arg4,arg5,arg6);
                            }
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }


                case PATH_TRANSFORM:    //   "pathTransform shapeName atName"
                    {
                        if (arrCommand.length!=3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 2 arguments (the name of the shape and optionally the new winding rule argument), received "+(arrCommand.length-1)+" instead");
                        }

                        String pNickName=arrCommand[1];
                        Path2D path=(Path2D) hmShapes.get(pNickName.toUpperCase());

                        if (path==null)
                        {
                            throw new IllegalArgumentException("no Path2D shape with name \""+pNickName+"\" stored");
                        }

                        String atNickName=arrCommand[2];
                        AffineTransform at=hmTransforms.get(atNickName.toUpperCase());
                        if (at==null)
                        {
                            try
                            {
                                at = (AffineTransform) getContextVariable(slot, atNickName); // try to get from a Rexx variable
                            }
                            catch (Throwable t) {}
                            if (at==null)
                            {
                                throw new IllegalArgumentException("no transform with name \""+atNickName+"\" stored, nor a Rexx variable pointing to an AffineTransform");
                            }
                        }

                        path.transform(at);

                        if (isOR)
                        {
                            writeOutput(slot, canonical+" "+pNickName+" "+atNickName);
                        }
                        return resultValue;
                    }

                case PATH_WINDING_RULE: //   "pathWindingRule shapeName [WIND_EVEN_ODD=0 | WIND_NON_ZERO=1]"
                    {
                        if (arrCommand.length!=2 && arrCommand.length>3)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1 or 2 arguments (the name of the shape and optionally the new winding rule argument), received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName=arrCommand[1];
                        Path2D path=(Path2D) hmShapes.get(nickName.toUpperCase());

                        if (path==null)
                        {
                            throw new IllegalArgumentException("no Path2D shape with name \""+nickName+"\" stored");
                        }
                        if (isOR)
                        {
                            canonical=canonical+" "+nickName;
                        }

                        int wRule=path.getWindingRule();
                        if (arrCommand.length==2)
                        {
                            resultValue=""+wRule;
                        }
                        else    // get and set the new windingRule
                        {
                            // WindingRule
                            String ucWR = arrCommand[2].toUpperCase();
                            if (startRexxVariableChar.indexOf(ucWR.charAt(0))>=0)    // a symbolic name?
                            {
                                if (!windingRules.containsKey(ucWR))
                                {
                                    throw new IllegalArgumentException("unknown value for \"windingRule\" argument supplied: ["+ucWR+"]");
                                }
                                wRule=windingRules.get(ucWR);
                            }
                            else // verbatim int type
                            {
                                wRule=Integer.parseInt(ucWR);
                                if (! windingRules.containsValue(wRule))
                                {
                                    throw new IllegalArgumentException("unknown value for \"windingRule\" argument supplied: ["+ucWR+"]");
                                }
                            }
                            if (isOR)
                            {
                                if (bUseNames4canonical)
                                {
                                    canonical=canonical+" "+windingRulesInt2Name.get(wRule);
                                }
                                else
                                {
                                    canonical=canonical+" "+wRule;
                                }
                            }
                            path.setWindingRule(wRule);
                        }

                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }
                        return resultValue;
                    }


                case SHAPE_GET_PATH_ITERATOR:   //  "pathIterator shapeName [affinityTransformName=.nil [flatness]]" -> returns getPathIterator(AffinityTransform[,flatness])
                    {
                        int argNum=arrCommand.length;
                        if (argNum<2 && argNum>4)
                        {
                            throw new IllegalArgumentException("this command needs exactly 1, 2 or 3 arguments (the name of the shape and optionally the new winding rule argument), received "+(arrCommand.length-1)+" instead");
                        }

                        String nickName=arrCommand[1];
                        Shape sh=(Shape) hmShapes.get(nickName.toUpperCase());

                        if (sh==null)
                        {
                            sh = (Shape) getContextVariable(slot, nickName); // try to get from a Rexx variable
                            if (sh==null || (! (sh instanceof Shape ||  sh instanceof PathIterator)) )
                            {
                                throw new IllegalArgumentException("no shape with name \""+nickName+"\" stored, nor a Rexx variable pointing to a Shape");
                            }
                        }
                        if (isOR)
                        {
                            canonical=canonical+" "+nickName;
                        }


                        if (arrCommand.length==2)
                        {
                            resultValue=sh.getPathIterator(null);
                        }
                        else
                        {
                            String  nickNameAT=arrCommand[2];
                            AffineTransform at=hmTransforms.get(nickNameAT.toUpperCase());
                            if (at==null)
                            {
                                if (startRexxVariableChar.indexOf(nickNameAT.charAt(0))>=0) // a Rexx variable?
                                {
                                    try
                                    {
                                        at=(AffineTransform) getContextVariable(slot, nickNameAT);
                                    }
                                    catch (Throwable t) {}
                                    if (at==null)
                                    {
                                        throw new IllegalArgumentException("no transform with name \""+nickNameAT+"\" stored, nor a Rexx variable that refers to an AffineTransform object");
                                    }
                                }
                            }

                            if (isOR)
                            {
                                canonical=canonical+" "+nickNameAT;
                            }

                            if (argNum==4)  // flatness supplied
                            {
                                double flatness = Double.parseDouble(arrCommand[3]);
                                resultValue=sh.getPathIterator(at,flatness);
                                if (isOR)
                                {
                                    canonical=canonical+" "+flatness;
                                }
                            }
                            else
                            {
                                resultValue=sh.getPathIterator(at);
                            }
                        }
                        if (isOR)
                        {
                            writeOutput(slot, canonical);
                        }

                        return resultValue;
                    }



/* TODO: get and save in image Map under the name "clipboard"; set from current or map image
         check JavaFX Clipboard interfaces first, if possible create a matching implementation
         in plain Java (dealing with images, text and html text only)
                case CLIPBOARD_GET:     //   "clipboardGet [imgName]": if imgName given, saves it and returns image width and height, else draws img and returns it as an img object
                case CLIPBOARD_SET:     //   "clipboardSet [imgName]": copies current image to clipboard, if imgName given uses cached image instead; returns pre clipboard img object (if any) or .nil
                    {
                        if (arrCommand.length>2)
                        {
                            throw new IllegalArgumentException("this command needs no or exactly 1 (imgName) argument (the name of the shape and optionally the new winding rule argument), received "+(arrCommand.length-1)+" instead");
                        }

                        Clipboard sc=Toolkit.getDefaultToolkit().getSystemClipboard();

                        BufferedImage tmpImg=null;
                        if (cmd==EnumCommand.CLIPBOARD_GET)
                        {


                        }
                        else
                        {

                        }



                        break;
                    }
*/




                default:        // unknown/unhandled command, raise failure condition (should never arrive here)
                    if (isOR)
                    {
                        writeOutput(slot, "-- FAILURE (unimplemented command): ["+command+"]");
                    }
                    return createCondition (slot, nrCommand, command, ConditionType.FAILURE, "-101", "unimplemented command");
            }
        }
        catch (Throwable t)     // raise error condition if args etc. were causing exceptions
        {
            if (isOR)
            {
                writeOutput(slot, "-- ERROR (argument): ["+command+"]");
            }
            return createCondition (slot, nrCommand, command, ConditionType.ERROR, "-102", t.toString() );
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
        String [] parts=value.substring(start+1,end-1).split(",");
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
        String [] parts=value.substring(start+1,end-1).split(",");
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
        return value.substring(start,end);  // return digits only so Integer.parseInt() does not barf
    }

        // create an ooRexx like array expression
    /** Returns a Rexx array expression from received <code>float</code> array.
     * @param floatArray int array to work on
     * @return rendered Rexx array expression
     */
    static String floatArrayToRexxArrayExpression(float [] floatArray)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(arrayOpenDeli);
        if (floatArray != null)
        {
            int last=floatArray.length-1;
            for (int i=0;i<floatArray.length;i++)
            {
                sb.append(""+floatArray[i]);
                if (i<last) sb.append(',');
            }
        }
        sb.append(arrayCloseDeli);
        return sb.toString();
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

    /** Parse string value to an int value, even if the string value represents a float or double value.
     *  Implemenation is geared toward speed, only use Double.parseDouble() if a dot contained in the
     *  the string value.
     *
     * @param strValue
     * @return int value representint strValue
     * @throws NumberFormatException
     */
    static int string2int(String strValue) throws NumberFormatException
    {
        // if a dot in string assume Double value, else an int value
        boolean assumeInt=true;
        for (int i=0; i<strValue.length(); i++)
        {
            if (strValue.charAt(i)=='.')
            {
                assumeInt=false;
                break;
            }
        }
        if (assumeInt)
        {
            return Integer.parseInt(strValue);
        }
        return (int) Math.round(Double.parseDouble(strValue));
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
 */
enum EnumCommand {
    // define the enum values for the commands available to Rexx programs,
    // supplying the strings associated with them                   // ? synonyms too

    // 20220929: do not support Shapes, maybe in a future update; one can always
    //           get access using "gc" command and interact with it from ooRexx directly

    // java.awt.Graphics or java.awt.Graphics2D related

    BACKGROUND                  ( "background"               ) ,    //   "background  [colorNickName]" query current background color or set
    CLEAR_RECT                  ( "clearRect"                ) ,    //   "clear width height": uses current color to clear background
    CLIP                        ( "clip"                     ) ,    //   "clip [x y w h]" -> get (getClipBounds()) or clipRect(x y w h)
    CLIP_REMOVE                 ( "clipRemove"               ) ,    //   "clipRemove" -> setClip(null)
    COLOR                       ( "color"                    ) ,    //   "color  [colorNickName [r g b [a]]" query current color or set + define new color
    COMPOSITE                   ( "composite"                ) ,    //   "composite [rule [alpha]]": get current or get and set new;
    COPY_AREA                   ( "copyArea"                 ) ,    //   "copyArea width height distX distY"
    DRAW_3D_RECT                ( "draw3DRect"               ) ,    //   "draw3DRect width height raised" - 20220906
    DRAW_ARC                    ( "drawArc"                  ) ,    //   "drawArc  width height startAngle arcAngle"
    DRAW_IMAGE                  ( "drawImage"                ) ,    //   "drawImage nickName [bkgColor] | nickName width height [bkgColor] | nickName width height srcX1 srcY1 srcX2 srcY2 [bkgColor]"
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
    FONT_SIZE                   ( "fontSize"                 ) ,    //   "fontSize size
    FONT_STYLE                  ( "fontStyle"                ) ,    //   "fontStyle 0=PLAIN | 1=BOLD | 2=ITALIC | 3=BOLD+ITALIC
    GET_GC                      ( "GC"                       ) ,    //   "GC"    returns current GC (GraphicsContext)
    GET_IMAGE                   ( "image"                    ) ,    //   "image [nickName]" returns image or nickName-stored image
    GET_STATE                   ( "getState"                 ) ,    //   "getState [ctxtVariableName]" returns a StringTable with current variables, stacks and HashMaps;
    GRADIENT_PAINT              ( "gradientPaint"            ) ,    //   "gradientPaint paintNickName [x1 y1 colorName1 x2 y2 colorName2 [cyclic]]": set or define and set gradientPaint
    IMAGE_COPY                  ( "imageCopy"                ) ,    //   "imageCopy [nickName]": get a copy of the current image, if nickName supplied use from registry and if not found from Rexx variable
    IMAGE_SIZE                  ( "imageSize"                ) ,    //   "imageSize [imageNickName]": returns width and height
    IMAGE_TYPE                  ( "imageType"                ) ,    //   "imageType [nickName]": get current image's type, if nickName supplied return its image type
    LOAD_IMAGE                  ( "loadImage"                ) ,    //   "loadImage imageNickName filename", loads an image -> returns its dimension
    MOVE_TO                     ( "moveTo"                   ) ,    //   "pos [x y]" query or set position, synonyms: "goto"
    NEW_IMAGE                   ( "newImage"                 ) ,    //   "new[Image] [width height [type]]" creates a new BufferedImage
    PAINT                       ( "paint"                    ) ,    //   "paint [gradiantPaintNickName|colorNickName]" query or set paint (for background) // 2022-09-22, paint, as of 20220922 cf. <http://underpop.online.fr/j/java/help/colors-graphics-programming.html.gz>
    POP_GC                      ( "popGC"                    ) ,    //   "popGC"  pop and set previous gc
    POP_IMAGE                   ( "popImage"                 ) ,    //   "popImage"  pop and display previous image
    PREFERRED_IMAGE_TYPE        ( "preferredImageType"       ) ,    //   "preferredImageType [type]": get or set preferred image type, type can be integer or name of BufferedImage TYPE constant
    PRINT_IMAGE                 ( "printImage"               ) ,    //   "printImage [nickName]": print image, either current or the one by nickname
    PRINT_POS                   ( "printPos"                 ) ,    //   "printPos [X Y]: get or set position of left upperhand corner; honored if printScaleToPage is false
    PRINT_SCALE                 ( "printScale"               ) ,    //   "printScale [scaleX [scaleY]": get or set scaleX (if no scaleY use scaleX); honored if printScaleToPage is false
    PRINT_SCALE_TO_PAGE         ( "printScaleToPage"         ) ,    //   "printScaleToPage [.true|.false]": get or set
    PUSH_GC                     ( "pushGC"                   ) ,    //   "pushGC" push current gc
    PUSH_IMAGE                  ( "pushImage"                ) ,    //   "pushImage [nickName]" push current image displayed
    RENDER                      ( "render"                   ) ,    //   "render [opt1 [opt2]]": returns or sets the current antialiasing settings for rendering
    RESET                       ( "reset"                    ) ,    //   "reset" synonym: "clear", clears everything, resets
    ROTATE                      ( "rotate"                   ) ,    //   "rotate [theta [x y]]" query or set rotation
    SAVE_IMAGE                  ( "saveImage"                ) ,    //   "saveImage filename", saves current image to file
    SCALE                       ( "scale"                    ) ,    //   "scale [x [y]]" query or set scale for x, y; if y omitted, uses x
    SET_PAINT_MODE              ( "setPaintMode"             ) ,    //   "setPaintMode": cf. Grahpics#setPaintMode()
    SET_XOR_MODE                ( "setXorMode"               ) ,    //   "xorMode colorNickName": sets xorMode to supplied color
    SHEAR                       ( "shear"                    ) ,    //   "shear [x [y]]" query or set shear for x, y; if y omitted, uses x

    // 2022-12-03
    SHAPE                       ( "shape"                    ) ,    //   "shape name [type args...]" query or create a shape
    DRAW_SHAPE                  ( "drawShape"                ) ,    //   "drawShape name" draws the named shape
    FILL_SHAPE                  ( "fillShape"                ) ,    //   "fillShape name" fills the named shape
    CLIP_SHAPE                  ( "clipShape"                ) ,    //   "clipShape name" defines intersection with named shape as the new clip

    SLEEP                       ( "sleep"                    ) ,    //   "sleep msecs" (uses Thread.sleep())
    STRING_BOUNDS               ( "stringBounds"             ) ,    //   "stringBounds string": returns the Rectangle2D x y w h (in floats?)
    STROKE                      ( "stroke"                   ) ,    //   "stroke [strokeNickName [width [cap join [miterlimit [[]floatDash floatDashPhase]]]]" query or set stroke
    TRANSFORM                   ( "transform"                ) ,    //   "transform { | RESET | translateX translateY scaleX scaleY shearX shearY} " query, change reset Graphics2D's AfineTransform
    TRANSLATE                   ( "translate"                ) ,    //   "translate [x [y]]" query origin or move origin; if y omitted, uses x
    WIN_ALWAYS_ON_TOP           ( "winAlwaysOnTop"           ) ,    //   "winAlwaysOnTop [.true | .false]" get or set of this window state
    WIN_ALWAYS_ON_TOP_SUPPORTED ( "winAlwaysOnTopSupported"  ) ,    //   "winAlwaysOnTopSupported" get value
    WIN_CLOSE                   ( "winClose"                 ) ,    // S "winClose" synonym: "exit" closes JavaDrawingFrame instance
    WIN_FRAME                   ( "winFrame"                 ) ,    //   "winFrame [.true|.false]": query or set JFrame's frame according to argument
    WIN_HIDE                    ( "winHide"                  ) ,    //   "winHide" hide JavaDrawingFrame
    WIN_LOCATION                ( "winLocation"              ) ,    //   (synonym: WINMOVETO)"winLocation [x y]" get or set location on screen
    WIN_RESIZABLE               ( "winResizable"             ) ,    //   "winResizable [.true|.false]": query or set JFrame's frame according to argument
    WIN_SCREEN_SIZE             ( "winScreenSize"            ) ,    //   "winScreenSize" query screen's width and size
    WIN_SHOW                    ( "winShow"                  ) ,    //   "winShow" show JavaDrawingFrame
    WIN_SIZE                    ( "winSize"                  ) ,    //   "winSize [width height]" query or set position
    WIN_TITLE                   ( "winTitle"                 ) ,    //   "winTitle [newtitle]": get or set JFrame's title
    WIN_TO_BACK                 ( "winToBack"                ) ,    //   "winToBack" put window into the back
    WIN_TO_FRONT                ( "winToFront"               ) ,    //   "winToFront" put window into the front
    WIN_UPDATE                  ( "winUpdate"                ) ,    //   "winUpdate [.true|.false]" get or set whether Frame should be updated upon draws/changes
    WIN_VISIBLE                 ( "winVisible"               ) ,    //   "winVisible [.true|.false]" get or set visibility

    // 2022-12-07, Path2D specific support
    SHAPE_BOUNDS                ( "shapeBounds"              ) ,    //   "shapeBounds shapeName" ... getBounds2D()
    SHAPE_GET_PATH_ITERATOR     ( "pathIterator"             ) ,    //  "pathIterator shapeName [affinityTransform=.nil [flatness]]" -> returns getPathIterator(AffinityTransform[,flatness])

    // ad append: if shapeName not found check for Rexx variable name (must be either a Shape or a PathIterator!)
    PATH_APPEND                 ( "pathAppend"               ) ,    //   "pathAppend shapeName | "rexxVarName (Shape|PathIterator)" [connect=.true]"
    PATH_CLOSE                  ( "pathClose"                ) ,    //   "pathClose shapeName" ... closePath()
    PATH_CURRENT_POINT          ( "pathCurrentPoint"         ) ,    //   "pathCurrentPoint shapeName" ... getCurrentPoint
    PATH_CURVE_TO               ( "pathCurveTo"              ) ,    //   "pathCurveTo shapeName x1 y1 x2 y2 x3 y3"
    PATH_LINE_TO                ( "pathLineTo"               ) ,    //   "pathLineTo shapeName x y"
    PATH_MOVE_TO                ( "pathMoveTo"               ) ,    //   "pathMoveTo shapeName x y"
    PATH_QUAD_TO                ( "pathQuadTo"               ) ,    //   "pathQuadTo shapeName x1 y1 x2 y2"
    PATH_RESET                  ( "pathReset"                ) ,    //   "pathReset shapeName"
    PATH_WINDING_RULE           ( "pathWindingRule"          ) ,    //   "pathWindingRule shapeName [WIND_EVEN_ODD=0 | WIND_NON_ZERO=1]" ... getWindingRule(), setWindingRule(newValue)

    // 2022-12-08
    PATH_CLONE                  ( "pathClone"                ) ,    //   "pathClone shapeName [newShapeName]"
    PATH_TRANSFORM              ( "pathTransform"            ) ,    //   "pathTransform shapeName atName"
    CLIPBOARD_GET               ( "clipboardGet"             ) ,    //   "clipboardGet [imgName]" ... if imgName: save in hmImages
    CLIPBOARD_SET               ( "clipboardSet"             ) ,    //   "clipboardSet [imgName]" ... if imgName: get from hmImages

    // 2023-01-08
    ASSIGN_RC                   ( "assignRC"                 ) ,    //   "assignRC RexxVariable"

    // 2023-06-05
    AREA_ADD                    ( "areaAdd"                  ) ,    //   "areaAdd         areaName shapeName..."
    AREA_EXCLUSIVE_OR           ( "areaExclusiveOr"          ) ,    //   "areaExclusiveOr areaName shapeName..."
    AREA_INTERSECT              ( "areaIntersect"            ) ,    //   "areaIntersect   areaName shapeName..."
    AREA_SUBTRACT               ( "areaSubtract"             ) ,    //   "areaSubtract    areaName shapeName..."
    AREA_TRANSFORM              ( "areaTransform"            ) ,    //   "areaTransform   areaName tNickname"



    NO_OP                       ( "noOp"                     )      // last element, doing nothing
                               ;


    /**  HashMap that allows to retrieve the enum type given a string, irrespectible of the case the string was supplied.
     *   Cf. {@link #getCommand(String mixedCase)}.
     */
    final static HashMap<String,EnumCommand> upperCase2Command=new HashMap<>(64);

    static   // use static block to add synonym strings to upperCase2Command Map
    {
        // add all types to HashMap (in declaration order)
         for (EnumCommand it : EnumCommand.values())
         {
             upperCase2Command.put(it.upperCase, it);
         }

         // add synonyms         new name      enum type
         upperCase2Command.put( "NEW"        , NEW_IMAGE ) ;    // 2023-01-18
         upperCase2Command.put( "CLEAR"      , RESET     ) ;
         upperCase2Command.put( "GOTO"       , MOVE_TO   ) ;
         upperCase2Command.put( "LOCATION"   , MOVE_TO   ) ;    // 2022-10-23: new alias
         upperCase2Command.put( "POS"        , MOVE_TO   ) ;    // 2022-10-29: new alias
         upperCase2Command.put( "POSITION"   , MOVE_TO   ) ;    // 2022-10-26: new alias
         upperCase2Command.put( "WINMOVETO"  , WIN_LOCATION ) ;
         upperCase2Command.put( "SHAPECLIP"  , CLIP_SHAPE   ) ; // 2023-06-06
         upperCase2Command.put( "SHAPEDRAW"  , DRAW_SHAPE   ) ; // 2023-06-06
         upperCase2Command.put( "SHAPEFILL"  , FILL_SHAPE   ) ; // 2023-06-06
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
    EnumCommand(String mixedCase)
    {
        this.mixedCase=mixedCase;
        this.upperCase=mixedCase.toUpperCase();
    }

    /** Returns the EnumCommand corresponding to the supplied String.
     *
     * @param mixedCase a String value in any case corresponding to the command from Rexx
     *
     * @return the enum EnumCommand corresponding to the supplied String, <code>null</code> if not defined
     */
    static public EnumCommand getCommand(String mixedCase)
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
        System.err.println("... arrived in main, args.length="+args.length+", DRAW_LINE.getMixedCase()="+DRAW_LINE.getMixedCase());
        System.err.println("... arrived in main, args.length="+args.length+", getCommand(\"NOT_AVAIL\")="+getCommand("NOT_AVAIL"));
        System.err.println("... arrived in main, args.length="+args.length+", getCommand(\"cLeAr\")  ="+getCommand("cLeAr"));
        System.err.println("... arrived in main, args.length="+args.length+", getCommand(\"drawOval\")="+getCommand("drawOval"));

        for (int i=0; i<args.length; i++)
        {
            System.err.println("... ... ... .. main, args["+i+"/"+args.length+"]: getCommand("+args[i]+")=["+getCommand(args[i])+"]");
        }
    }
}

// ============================================================================
/** Define the Shapes we know of, add aliases for the supported ones (first significant letter(s)),
    names without trailing 2D),
    support the string renderings.
 */

enum EnumShape {
    // define the Shape enum values for the commands available to Rexx programs,
    // supplying the strings associated with them

    SHAPE_AREA                  ( "Area"                    ),
    SHAPE_ARC2D                 ( "Arc2D"                   ),
    SHAPE_CUBIC_CURVE2D         ( "CubicCurve2D"            ),
    SHAPE_ELLIPSE2D             ( "Ellipse2D"               ),
    SHAPE_LINE2D                ( "Line2D"                  ),
    SHAPE_PATH2D                ( "Path2D"                  ),
    SHAPE_POLYGON               ( "Polygon"                 ),  // no trailing "2D"
    SHAPE_QUAD_CURVE2D          ( "QuadCurve2D"             ),
    SHAPE_RECTANGLE2D           ( "Rectangle2D"             ),
    SHAPE_ROUND_RECTANGLE2D     ( "RoundRectangle2D"        ),

    NO_OP                       ( "noOp"                    )      // last element, doing nothing
    ;

    /**  HashMap that allows to retrieve the enum type given a string, irrespectible of the case the string was supplied.
     *   Cf. {@link #getShape(String mixedCase)}.
     */
    final static HashMap<String,EnumShape> upperCase2Shape=new HashMap<>(64);

    static   // use static block to add synonym strings to upperCase2Shape Map
    {
        // add all types to HashMap (in declaration order)
         for (EnumShape it : EnumShape.values())
         {
             upperCase2Shape.put(it.upperCase, it);
         }

         // we allow shortening the shape type to the significant letters (at least 2 letters)
         // add synonyms      new name      enum type
         upperCase2Shape.put( "ARC"           , SHAPE_ARC2D             ) ;
         upperCase2Shape.put( "CUBIC"         , SHAPE_CUBIC_CURVE2D     ) ;
         upperCase2Shape.put( "CUBICCURVE"    , SHAPE_CUBIC_CURVE2D     ) ;
         upperCase2Shape.put( "ELLI"          , SHAPE_ELLIPSE2D         ) ;
         upperCase2Shape.put( "ELLIPSE"       , SHAPE_ELLIPSE2D         ) ;
         upperCase2Shape.put( "LINE"          , SHAPE_LINE2D            ) ;
         upperCase2Shape.put( "PATH"          , SHAPE_PATH2D            ) ;
         upperCase2Shape.put( "QUAD"          , SHAPE_QUAD_CURVE2D      ) ;
         upperCase2Shape.put( "QUADCURVE"     , SHAPE_QUAD_CURVE2D      ) ;
         upperCase2Shape.put( "RECT"          , SHAPE_RECTANGLE2D       ) ;
         upperCase2Shape.put( "RECTANGLE"     , SHAPE_RECTANGLE2D       ) ;
         upperCase2Shape.put( "ROUNDRECT"     , SHAPE_ROUND_RECTANGLE2D ) ;
         upperCase2Shape.put( "ROUNDRECTANGLE", SHAPE_ROUND_RECTANGLE2D ) ;
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
    EnumShape(String mixedCase)
    {
        this.mixedCase=mixedCase;
        this.upperCase=mixedCase.toUpperCase();
    }

    /** Returns the EnumShape corresponding to the supplied String.
     *
     * @param mixedCase a String value in any case corresponding to the command from Rexx
     *
     * @return the enum EnumShape corresponding to the supplied String, <code>null</code> if not defined
     */
    static public EnumShape getShape(String mixedCase)
    {
        if (mixedCase==null)
        {
            return null;
        }
        return upperCase2Shape.get(mixedCase.toUpperCase());
    }


    public static void main(String[] args) {
        System.err.println("... arrived in main, args.length="+args.length+", SHAPE_ARC2D="+SHAPE_ARC2D);
        System.err.println("... arrived in main, args.length="+args.length+", SHAPE_ARC2D.toString()="+SHAPE_ARC2D.toString());
        System.err.println("... arrived in main, args.length="+args.length+", SHAPE_ARC2D.getMixedCase()="+SHAPE_ARC2D.getMixedCase());
        System.err.println("... arrived in main, args.length="+args.length+", getShape(\"NOT_AVAIL\")="+getShape("NOT_AVAIL"));
        System.err.println("... arrived in main, args.length="+args.length+", getShape(\"A\")        ="+getShape("A"));
        System.err.println("... arrived in main, args.length="+args.length+", getShape(\"q\")        ="+getShape("q"));

        for (int i=0; i<args.length; i++)
        {
            System.err.println("... ... ... .. main, args["+i+"/"+args.length+"]: getShape("+args[i]+")=["+getShape(args[i])+"]");
        }
    }
}

