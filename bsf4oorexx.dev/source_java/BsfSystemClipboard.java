/*
   purpose: allow for using the system clipboard to set images and strings and
            to fetch (get) images and strings
   author:  Rony G. Flatscher
   date:    2023-06-10
   version: 100.20230610
   license: Apache license 2.0

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2023 Rony G. Flatscher

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
// we just support Image and String

// cf. <https://stackoverflow.com/questions/4552045/copy-bufferedimage-to-clipboard>, 2023-06-10
//     <https://stackoverflow.com/questions/18254808/how-to-clear-the-system-clipboard-in-java>, 2023-10-10

// (further reading, e.g.:
// awt vs. FX clipboard, cf. <https://gist.github.com/TurekBot/f639f327747e7f76639a806333756d30> (2023-06-10) )

package org.rexxla.bsf.engines.rexx;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

/** This class employs the <em>java.awt.datatransfer.Clipboard</em> class and allows for
 *  setting (and getting from) the system clipboard Java images (<em>java.awt.Image</em>)
 *  and strings.
 *
 * @since  2023-06-10
 * @author Rony G. Flatscher
 */
public class BsfSystemClipboard
{
    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    // ------------
    /** Tests whether the system clipboard is empty.
     *  @return <em>true</em> if empty, <em>false</em> else
     */
    public static boolean isEmpty()
    {
        Transferable t=null;
        DataFlavor tdf[]=null;
        try
        {
            t  =clipboard.getContents(null);
            tdf=t.getTransferDataFlavors();     // array of DataFlavors, if empty, then length==0
        }
        catch (IllegalStateException ise) {}    // if clipboard currently unavailable

        return (tdf==null ? true : tdf.length==0);
    }

    // ------------
    /** Clear (empty) the system clipboard.
     */
    public static void clear()
    {
       clipboard.setContents(new ClearClipboardTransferable(), null);
    }

    // ------------
    // we need to support from Java 8 on, images to clipboard will create a JPG which must
    // not have transparency, so making sure to create a JPG compliant bitmap and have the
    // background painted white and then set the image on to it.

    /** This allows a <em>java.awt.Image</em> image to be copied as a JPG/JPEG (!) image to the
     *  system clipboard. The supplied image will therefore be transformed into a JPG/JPEG (with
     *  a white background).
     *
     *  @param img the image to be copied as JPG/JPEG (with white background) to the clipboard
     */
    public static void setImage(Image img)
    {
        // make sure the image data can be written as JPG: write image to a RGB image with white background
        BufferedImage jpgImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = jpgImg.createGraphics();
        g2d.setColor(Color.WHITE);      // otherwise the background would be black!
        g2d.fillRect(0, 0, jpgImg.getWidth(), jpgImg.getHeight());
        g2d.drawImage(img, 0, 0, null); // without an ImageObserver
        g2d.dispose();
        clipboard.setContents(new TransferableImage(jpgImg), null);    // we do not supply an owner
    }

    /** Gets an image from the system clipboard and returns it.
     *
     *  @return image from the system clipboard or <em>null</em> if none available
     */
    public static Image getImage()
    {
        if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor))
        {
            try
            {
                return (Image) clipboard.getData(DataFlavor.imageFlavor);
            }
            catch (Exception exc) {}    // just return null;
        }
        return null;
    }

    // ------------
    public static void setString(String str)
    {
        clipboard.setContents(new StringSelection(str), null);      // we do not supply an owner
    }

    public static String getString()
    {
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
        {
System.err.println("getString(): stringFlavor is available!");
            try
            {
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }
            catch (Exception exc) {}    // just return null;
        }
        return null;
    }


    /** Allows to transfer an <em>imageFlavor</em>
     */
    static class TransferableImage implements Transferable
    {
        Image img;
        public TransferableImage( Image img )
        {
            this.img = img;
        }

        public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException
        {
            if ( img != null && flavor.equals( DataFlavor.imageFlavor )  )
            {
                return img;
            }
            throw new UnsupportedFlavorException( flavor );
        }

        /** Supports <em>imageFlavor</em> only.
         *
         * @return a <em>DataFlavor</em> array with the single entry <em>DataFlavor.imageFlavor</em>
        */
        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        public boolean isDataFlavorSupported( DataFlavor flavor )
        {
            return flavor.equals(DataFlavor.imageFlavor);
        }
    }


    // 2023-10-10: https://stackoverflow.com/questions/18254808/how-to-clear-the-system-clipboard-in-java
    /** Allows to transfer no content to the clipboard, causing the clipboard to be emptable.
     */
    static class ClearClipboardTransferable implements Transferable
    {
        public DataFlavor[] getTransferDataFlavors()
        {
          return new DataFlavor[0];
        }

        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
          return false;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
        {
          throw new UnsupportedFlavorException(flavor);
        }
    }

}


// further reading, e.g.:
// awt vs. FX clipboard, cf. <https://gist.github.com/TurekBot/f639f327747e7f76639a806333756d30> (2023-06-10)
