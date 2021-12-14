package org.oorexx.ide.intellij.highlighting;

import java.util.ArrayList;
import java.util.List;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.intellij.util.KeywordHelper;
import org.oorexx.ide.lexer.OoRexxLexerAdapter;
import org.oorexx.ide.lexer.token.OoRexxTokenType;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class OoRexxSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey OOREXX_IDENTIFIER = createTextAttributesKey("OOREXX_IDENTIFIER", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey OOREXX_KEYWORD = createTextAttributesKey("OOREXX_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey OOREXX_BUILT_IN = createTextAttributesKey("OOREXX_BUILT_IN", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey OOREXX_NUMBER = createTextAttributesKey("OOREXX_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey OOREXX_OPERATOR = createTextAttributesKey("OOREXX_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey OOREXX_LINE_COMMENT = createTextAttributesKey("OOREXX_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey OOREXX_BLOCK_COMMENT = createTextAttributesKey("OOREXX_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey OOREXX_SELF = createTextAttributesKey("OOREXX_SELF", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey OOREXX_ENVIRONMENT = createTextAttributesKey("OOREXX_ENVIRONMENT", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey OOREXX_SPECIAL_ENVIRONMENT = createTextAttributesKey("OOREXX_SPECIAL_ENVIRONMENT", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey OOREXX_SHEBANG_LINE = createTextAttributesKey("OOREXX_SHEBANG_LINE", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("OOREXX_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    public static final TextAttributesKey OOREXX_STRINGS = createTextAttributesKey("OOREXX_STRINGS", DefaultLanguageHighlighterColors.STRING);

    private static final TextAttributesKey[] ENVIRONMENT_KEYS = new TextAttributesKey[]{OOREXX_ENVIRONMENT};
    private static final TextAttributesKey[] SPECIAL_ENVIRONMENT_KEYS = new TextAttributesKey[]{OOREXX_SPECIAL_ENVIRONMENT};
    private static final TextAttributesKey[] SELF_KEYS = new TextAttributesKey[]{OOREXX_SELF};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{OOREXX_NUMBER};
    private static final TextAttributesKey[] LINE_COMMENT_KEYS = new TextAttributesKey[]{OOREXX_LINE_COMMENT};
    private static final TextAttributesKey[] BLOCK_COMMENT_KEYS = new TextAttributesKey[]{OOREXX_BLOCK_COMMENT};
    private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{OOREXX_STRINGS};
    private static final TextAttributesKey[] SHEBANG_LINE_KEYS = new TextAttributesKey[]{OOREXX_SHEBANG_LINE};
    private static final TextAttributesKey[] BOOL_NIL_KEYS = new TextAttributesKey[]{OOREXX_KEYWORD};
    private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{OOREXX_KEYWORD};
    private static final TextAttributesKey[] OPERATOR_KEYS = new TextAttributesKey[]{OOREXX_OPERATOR};
    private static final TextAttributesKey[] BUILT_IN_KEYS = new TextAttributesKey[]{OOREXX_BUILT_IN};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    private List<String> keywordList = new ArrayList<>();

    public OoRexxSyntaxHighlighter() {
        keywordList = KeywordHelper.getList();
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new OoRexxLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {

        if (tokenType instanceof OoRexxTokenType) {

            OoRexxTokenType ooRexxTokenType = (OoRexxTokenType) tokenType;

            if (ooRexxTokenType.getTokenName().equals("+") ||
                    ooRexxTokenType.getTokenName().equals("-") ||
                    ooRexxTokenType.getTokenName().equals("*") ||
                    ooRexxTokenType.getTokenName().equals("/") ||
                    ooRexxTokenType.getTokenName().equals("%") ||
                    ooRexxTokenType.getTokenName().equals("<") ||
                    ooRexxTokenType.getTokenName().equals(">") ||
                    ooRexxTokenType.getTokenName().equals("=") ||
                    ooRexxTokenType.getTokenName().equals("\\") ||
                    ooRexxTokenType.getTokenName().equals("~")

            ) {
                return OPERATOR_KEYS;
            }

            if (ooRexxTokenType.getTokenName().equals(".LOCAL") || ooRexxTokenType.getTokenName().equals(".ENVIRONMENT")) {
                return SPECIAL_ENVIRONMENT_KEYS;
            }

            if (ooRexxTokenType.getTokenName().equals("ENV_IDENTIFIER")) {
                return ENVIRONMENT_KEYS;
            }


            if (ooRexxTokenType.getTokenName().equals("STRING_LITERAL")
                    || ooRexxTokenType.getTokenName().equals("EMPTY_STRING_LITERAL")
                    || ooRexxTokenType.getTokenName().equals("BYTE_STRING_LITERAL")
                    || ooRexxTokenType.getTokenName().equals("HEX_STRING_LITERAL")
            ) {
                return STRING_KEYS;
            }

            if (ooRexxTokenType.getTokenName().equals("INTEGER_LITERAL") | ooRexxTokenType.getTokenName().equals("FLOAT_LITERAL")) {
                return NUMBER_KEYS;
            }

            if (ooRexxTokenType.getTokenName().equals("EOL_COMMENT")) {
                return LINE_COMMENT_KEYS;
            }

            if (ooRexxTokenType.getTokenName().equals(".NIL") | ooRexxTokenType.getTokenName().equals(".TRUE") | ooRexxTokenType.getTokenName().equals(".FALSE")) {
                return BOOL_NIL_KEYS;
            }

            if (ooRexxTokenType.getTokenName().equals("SELF") || ooRexxTokenType.getTokenName().equals("SUPER")) {
                return SELF_KEYS;
            }

            if (ooRexxTokenType.getTokenName().equals("BLOCK_COMMENT") ||
                    ooRexxTokenType.getTokenName().equals("DOC_COMMENT")
            ) {
                return BLOCK_COMMENT_KEYS;
            }

            if (ooRexxTokenType.getTokenName().equals("SHEBANG_LINE")) {
                return SHEBANG_LINE_KEYS;
            }
        }

        return EMPTY_KEYS;
    }
}
