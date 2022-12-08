package org.oorexx.ide.intellij.completion;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.oorexx.ide.intellij.completion.OoRexxCompletionContributor.*;

public class ExternalCompletionElementsUtil {


    public static void loadExternal(Map<String, List<OoRexxCompletionContributor.ElementData>> elements) {
        File externalCompletionFiles = new File(System.getProperty("user.home"), "Intellij/ooRexxPlugin");
        if(!externalCompletionFiles.exists()){
            return;
        }
        File[] files = externalCompletionFiles.listFiles();
        for (File file : files) {
            if(!(file.isFile() && file.getAbsolutePath().endsWith(".txt"))){
                continue;
            }
            addCompletionsFromFile(file, elements);
        }
    }

    private static void addCompletionsFromFile(File file, Map<String, List<OoRexxCompletionContributor.ElementData>> elements) {
        List<String> content = null;
        try {
            content = FileUtils.readLines(file, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String currentCategory = null;

        for (String s : content) {
            if(s.startsWith("[") && s.endsWith("]")) {
                currentCategory = determineCategory(s);
                continue;
            }

            if(currentCategory != null &&  StringUtils.isNoneBlank(s)) {
                elements.get(currentCategory).add(new OoRexxCompletionContributor.ElementData(s,
                        getTailText(currentCategory) + " - " +
                                file.getName().replace(".txt", "")));
            }
        }
    }

    private static String determineCategory(String s) {
        String cat = s.replace("[", "").replace("]", "").trim().toLowerCase();
        return switch (cat) {
            case METHODS -> METHODS;
            case ENVIRONMENT -> ENVIRONMENT;
            case KEYWORDS-> KEYWORDS;
            case DIRECTIVES -> DIRECTIVES;
            case FUNCTIONS -> FUNCTIONS;
            default -> null;
        };
    }

    private static String getTailText(String category) {
        return switch (category) {
            case METHODS -> TT_METHOD;
            case ENVIRONMENT -> TT_ENVIRONMENT;
            case KEYWORDS-> TT_KEYWORD;
            case DIRECTIVES -> TT_DIRECTIVE;
            case FUNCTIONS -> TT_FUNCTION;
            default -> null;
        };
    }
}
