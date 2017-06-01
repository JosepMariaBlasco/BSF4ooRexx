package org.oorexx.ide.parser;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.grammar.LightPsi;
import org.junit.Test;
import org.oorexx.ide.intellij.file.OoRexxFileTypeFactory;
import org.oorexx.ide.parser.standalone.OoRexxStandaloneParserDefinition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OoRexxParserTest {

    private Logger logger = Logger.getLogger(OoRexxParserTest.class.getCanonicalName());

    @Test
    public void genericTest() throws IOException {

        List<File> noErrors = getNoErrorFiles();

        for(File file: noErrors){
            assertNoError(getPsiFile(file));
        }

        List<File> errors = getErrorFiles();

        for(File file: errors){
            assertError(getPsiFile(file));
        }
    }

    private void assertNoError(PsiFile psiFile){
        logger.log(Level.INFO, String.format("Asserting no error %s", psiFile.getVirtualFile().getCanonicalPath()));

        assertFalse(hasErrorElement(psiFile));
    }

    private void assertError(PsiFile psiFile){
        logger.log(Level.INFO, String.format("Asserting error %s", psiFile.getVirtualFile().getCanonicalPath()));

        assertTrue(hasErrorElement(psiFile));
    }

    private boolean hasErrorElement(PsiFile psiFile){
        PsiElement root = psiFile.getNode().getPsi();

        Collection<PsiErrorElement> errors = PsiTreeUtil.findChildrenOfType(root, PsiErrorElement.class);

        return !errors.isEmpty();
    }

    private List<File> getErrorFiles(){
        ArrayList<File> files = new ArrayList<>();

        getFilesOfDir(getTestResource("errors"), files);

        return files;
    }

    private List<File> getNoErrorFiles(){
        ArrayList<File> files = new ArrayList<>();

        getFilesOfDir(getTestResource("noErrors"), files);

        //getFilesOfDir(new File("/opt/BSF4ooRexx"), files);

        return files;
    }

    private PsiFile getPsiFile(File file) throws IOException {
        return LightPsi.parseFile(file, new OoRexxStandaloneParserDefinition());
    }

    private void getFilesOfDir(File dir, List<File> files){
        for(File file: dir.listFiles()){
            if(file.isDirectory()){
                getFilesOfDir(file, files);
            } else if(isAllowedFile(file)){
                files.add(file);
            }
        }
    }

    private boolean isAllowedFile(File file){
        for(String extension: OoRexxFileTypeFactory.fileExtensions){
            if(file.getName().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private File getTestResource(String dir){
        return new File(OoRexxParserTest.class.getClassLoader().getResource(dir).getFile());
    }
}