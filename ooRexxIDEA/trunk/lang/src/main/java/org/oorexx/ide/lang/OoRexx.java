package org.oorexx.ide.lang;

import com.intellij.lang.Language;

public class OoRexx extends Language {

    public static final OoRexx INSTANCE = new OoRexx();

    public OoRexx() {
        super("ooRexx");
    }
}
