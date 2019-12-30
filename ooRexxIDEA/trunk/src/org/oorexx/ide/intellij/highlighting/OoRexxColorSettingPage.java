package org.oorexx.ide.intellij.highlighting;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.*;
import org.jetbrains.annotations.*;
import org.oorexx.ide.file.OoRexxIcons;

import javax.swing.*;
import java.util.Map;

public class OoRexxColorSettingPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Keyword", OoRexxSyntaxHighlighter.OOREXX_KEYWORD),
            new AttributesDescriptor("Number", OoRexxSyntaxHighlighter.OOREXX_NUMBER),
            new AttributesDescriptor("Strings", OoRexxSyntaxHighlighter.BAD_CHARACTER),
            new AttributesDescriptor("Line Comments", OoRexxSyntaxHighlighter.OOREXX_LINE_COMMENT),
            new AttributesDescriptor("Block Comments", OoRexxSyntaxHighlighter.OOREXX_BLOCK_COMMENT),
            new AttributesDescriptor("Self", OoRexxSyntaxHighlighter.OOREXX_SELF),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return OoRexxIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new OoRexxSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "::ROUTINE clr.import PUBLIC\n" +
                "  USE strict ARG typeName, rexxName4local=.nil\n" +
                "\n" +
                "  if .CLRLogger~doLogging then    -- if not OFF, produce information\n" +
                "     .CLRLogger~trace(pp.line(.line) \"CLR.CLS/clr.import(), typeName=\"pp(typeName)\", rexxName4local=\"pp(rexxName4local))\n" +
                "\n" +
                "  return .clr~clr.import(typeName, rexxName4local)\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "ooRexx";
    }
}
