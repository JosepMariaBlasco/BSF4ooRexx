package org.oorexx.ide.lexer;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lexer.FlexAdapter;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.intellij.setting.OoRexxLanguageSettings;

import java.io.Reader;

import static org.oorexx.ide.intellij.setting.SettingPropertyNames.ALLOW_SPECIAL_CHARS;

public class OoRexxLexerAdapter extends FlexAdapter {

    public OoRexxLexerAdapter() {
        super(new OoRexxLexer());
    }
}
