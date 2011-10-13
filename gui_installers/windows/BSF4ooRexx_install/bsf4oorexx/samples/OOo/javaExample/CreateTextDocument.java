// ---rgf, 2007-01-12, Schiseminar 06/07, Pruggern
//
//  CLASSPATH needs to point to: $OOoBase/program/classes/jurt.jar
//                               $OOoBase/program/classes/unoil.jar
//                               $OOoBase/program/classes/ridl.jar
//                               $OOoBase/program/classes/juh.jar
//                               [$OOoBase/program/classes/sandbox.jar (for OOo v 1.x!)]
//
//
//  where $OOoBase is the OpenOffice/StarOffice path to its installation directory
//  e.g. OOoBase="/opt/openoffice.org.2.1"
//
// # . ./setCP.sh
// # adjust the following path to your installation; --rgf, 2007-02-17
// OOoBase=/opt/openoffice.org.2.1
//
// export CLASSPATH=$CLASSPATH:$OOoBase/program/classes/jurt.jar:$OOoBase/program/classes/unoil.jar:$OOoBase/program/classes/ridl.jar:$OOoBase/program/classes/juh.jar:.


class CreateTextDocument {
    public static void main (String args[]) {
        // excerpted from "HardFormatting.java" from the OOo development package
        com.sun.star.frame.XDesktop xDesktop = null;
        com.sun.star.lang.XMultiComponentFactory xMCF = null;

        try {
            com.sun.star.uno.XComponentContext xContext = null;

            // (1) bootstrap the UNO runtime environment
            xContext = com.sun.star.comp.helper.Bootstrap.bootstrap();

            // (2) get the service manager
            xMCF = xContext.getServiceManager();
            if( xMCF != null ) {
                System.out.println("Connected to a running office ...");

                // (3) start up an instance of office
                Object oDesktop = xMCF.createInstanceWithContext(
                    "com.sun.star.frame.Desktop", xContext);

                // (4a) get the XDesktop interface object
                xDesktop = (com.sun.star.frame.XDesktop)
                    com.sun.star.uno.UnoRuntime.queryInterface(
                    com.sun.star.frame.XDesktop.class, oDesktop);

                // (4b) get the desktop's component loader interface object
                com.sun.star.frame.XComponentLoader xComponentLoader =
                    (com.sun.star.frame.XComponentLoader)
                    com.sun.star.uno.UnoRuntime.queryInterface(
                    com.sun.star.frame.XComponentLoader.class, xDesktop);

                // create an empty text ("swriter") document
                com.sun.star.beans.PropertyValue xEmptyArgs[] = // empty property array
                    new com.sun.star.beans.PropertyValue[0];

                // (5) create an empty word processor ("swriter") component (document)
                com.sun.star.lang.XComponent xComponent = // text document
                    xComponentLoader.loadComponentFromURL( "private:factory/swriter","_blank",
                               0, xEmptyArgs);

                // (6) get Text interface object and set a text
                com.sun.star.text.XTextDocument xTextDocument =
                    (com.sun.star.text.XTextDocument)
                    com.sun.star.uno.UnoRuntime.queryInterface(
                    com.sun.star.text.XTextDocument.class, xComponent);

                com.sun.star.text.XText xText = xTextDocument.getText();

                // ((com.sun.star.text.XTextRange) xText).setString("Java was here!\r---rgf");
                xText.setString("Java was here!\r---rgf");
            }
            else
            System.out.println("Not able to create desktop object.");
        }
        catch( Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }

        System.err.println("Successful run.");
        System.exit(0);
    }
}
