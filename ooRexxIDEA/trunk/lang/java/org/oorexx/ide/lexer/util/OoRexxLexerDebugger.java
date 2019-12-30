package org.oorexx.ide.lexer.util;

import com.intellij.psi.tree.IElementType;
import org.oorexx.ide.lexer.OoRexxLexerAdapter;

import java.io.IOException;
import java.io.Reader;

public class OoRexxLexerDebugger {

    public static void main(String argv[]) {
        if (argv.length == 0) {
            System.out.println("Usage : java OoRexxLexer [ --encoding <name> ] <inputfile(s)>");
        }
        else {
            int firstFilePos = 0;
            String encodingName = "UTF-8";
            if (argv[0].equals("--encoding")) {
                firstFilePos = 2;
                encodingName = argv[1];
                try {
                    java.nio.charset.Charset.forName(encodingName); // Side-effect: is encodingName valid?
                } catch (Exception e) {
                    System.out.println("Invalid encoding '" + encodingName + "'");
                    return;
                }
            }
            for (int i = firstFilePos; i < argv.length; i++) {
                OoRexxLexerAdapter scanner = null;
                try {
                    java.io.FileInputStream stream = new java.io.FileInputStream(argv[i]);
                    Reader reader = new java.io.InputStreamReader(stream, encodingName);
                    scanner = new OoRexxLexerAdapter();
                    scanner.start(read(reader));
                    IElementType token;
                    do{
                        token = scanner.getTokenType();
                        System.out.println(token==null? "<<EOF>>" : token);
                        scanner.advance();
                    }while (token != null);

                }
                catch (java.io.FileNotFoundException e) {
                    System.out.println("File not found : \""+argv[i]+"\"");
                }
                catch (IOException e) {
                    System.out.println("IO error scanning file \""+argv[i]+"\"");
                    System.out.println(e);
                }
                catch (Exception e) {
                    System.out.println("Unexpected exception:");
                    e.printStackTrace();
                }
            }
        }
    }

    public static String read(Reader reader) throws IOException{
        int intValueOfChar;
        String targetString = "";
        while ((intValueOfChar = reader.read()) != -1) {
            targetString += (char) intValueOfChar;
        }
        reader.close();
        return targetString;
    }

}
