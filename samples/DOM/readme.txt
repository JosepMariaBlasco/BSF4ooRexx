2009-06-22, 2010-08-22, 2010-12-27, 2017-04-14, 2018-08-10, ---rgf

Created a set of examples that demonstrate the new callback functionality of BSF4Rexx 4.0
by using the Java DOM (and SAX) classes.

Run and look up the Rexx code and study the comments.

Usage:

        xyz.rxj www.rexxla.org.xhtml

        where "xyz.rxj" is one of:
                00_validateDocument.rxj
                01_getText.rxj
                02_getText.rxj
                03_listElementNames.rxj
                04_listElementNamesIndented.rxj
                05_listElementNamesIndentedWithAttributes.rxj
                06_listElementNamesIndentedWithAttributes_DOES_NOT_USE_DTD.rxj
                07_listElementNamesAsTree.rxj


File list:
        00_validateDocument.rxj
        01_getText.rxj
        02_getText.rxj
        03_listElementNames.rxj
        04_listElementNamesIndented.rxj
        05_listElementNamesIndentedWithAttributes.rxj
        06_listElementNamesIndentedWithAttributes_DOES_NOT_USE_DTD.rxj
        07_listElemen
        DTD/
        www.rexxla.org.xhtml


Note 1) the directory DTD is necessary as SAX will look it up for the DTD definition and
        additional support files

Note 2) "06_listElementNamesIndentedWithAttributes_DOES_NOT_USE_DTD.rxj" can be used with
        any XML-file or URL referring to a XML file; this is possible, because it ignores
        external DTD-files, hence the following would be possible with this script:

                06_listElementNamesIndentedWithAttributes_DOES_NOT_USE_DTD.rxj http://www.wi.ac.at
                06_listElementNamesIndentedWithAttributes_DOES_NOT_USE_DTD.rxj http://www.rexxla.org
                ...




