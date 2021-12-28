package org.oorexx.ide.util;

import com.intellij.lang.PsiBuilder;
import org.oorexx.ide.parser.OoRexxParser;

public class ExecutorParserUtil {

    public static boolean active = false;

    public static boolean parseParameterHook(PsiBuilder b, int l) {
        if (active) {
            return OoRexxParser.method_named_argument(b, l);
        }

        return false;
    }

    public static boolean parseUseKeywordHook(PsiBuilder b, int l) {
        if (active) {
            return OoRexxParser.keyword_use_named(b, l);
        }

        return false;
    }

    public static boolean parseBlockHook(PsiBuilder b, int l) {
        if (active) {
            return OoRexxParser.block(b, l);
        }

        return false;
    }


}
