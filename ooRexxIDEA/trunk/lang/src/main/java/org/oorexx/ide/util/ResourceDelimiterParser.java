package org.oorexx.ide.util;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;

import java.util.concurrent.ConcurrentHashMap;

import static org.oorexx.ide.lang.OoRexxTypes.*;
import static org.oorexx.ide.parser.OoRexxParser.*;

public class ResourceDelimiterParser extends GeneratedParserUtilBase{

    private static ConcurrentHashMap<Thread, String> currentDelimiter = new ConcurrentHashMap<>();

    public static boolean directive_resource_with_delemiter_ext(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "directive_resource_with_delemiter")) return false;
        if (!nextTokenIs(b, RESOURCE)) return false;
        boolean r, p;
        PsiBuilder.Marker m = enter_section_(b, l, _NONE_);
        r = consumeToken(b, RESOURCE);
        r = r && symbol_or_string(b, l + 1);
        r = r && consumeToken(b, END);
        p = r; // pin = 3
        setCurrentDelimiter(b.getTokenText());
        r = r && report_error_(b, consumeToken(b, getCurrentDelimiter()));
        r = p && report_error_(b, instruction_terminator(b, l + 1)) && r;
        r = p && report_error_(b, directive_resource_with_delemiter_5(b, l + 1)) && r;
        r = p && report_error_(b, directive_resource_delimiter_end(b, l + 1)) && r;
        r = p && instruction_terminator(b, l + 1) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    private static boolean directive_resource_with_delemiter_5(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "directive_resource_with_delemiter_5")) return false;
        int c = current_position_(b);
        while (true) {
            if (!directive_resource_with_delemiter_5_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "directive_resource_with_delemiter_5", c)) break;
            c = current_position_(b);
        }
        return true;
    }

    private static boolean directive_resource_with_delemiter_5_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "directive_resource_with_delemiter_5_0")) return false;
        boolean r;
        PsiBuilder.Marker m = enter_section_(b);
        r = directive_resource_with_delemiter_5_0_0(b, l + 1);
        r = r && directive_resource_with_delemiter_5_0_1(b, l + 1);
        r = r && directive_resource_data(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    private static boolean directive_resource_with_delemiter_5_0_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "directive_resource_with_delemiter_5_0_0")) return false;
        boolean r;
        PsiBuilder.Marker m = enter_section_(b, l, _NOT_);
        r = !eof(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    private static boolean directive_resource_with_delemiter_5_0_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "directive_resource_with_delemiter_5_0_1")) return false;
        boolean r;
        PsiBuilder.Marker m = enter_section_(b, l, _NOT_);
        r = !consumeToken(b, getCurrentDelimiter());
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    public static boolean directive_resource_delimiter_end(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "directive_resource_delimiter_end")) return false;
        boolean r;
        PsiBuilder.Marker m = enter_section_(b);
        r = consumeToken(b, getCurrentDelimiter());
        exit_section_(b, m, DIRECTIVE_RESOURCE_END, r);
        return r;
    }

    public static boolean directive_resource_data_ext(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "directive_resource_data")) return false;
        boolean r;
        PsiBuilder.Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_RESOURCE_DATA, "<directive resource data>");
        IElementType token = b.getTokenType();
        r = consumeToken(b,token);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    private static String getCurrentDelimiter() {
        return currentDelimiter.get(Thread.currentThread());
    }

    private static void setCurrentDelimiter(String s) {
        currentDelimiter.put(Thread.currentThread(), s);
    }
}
