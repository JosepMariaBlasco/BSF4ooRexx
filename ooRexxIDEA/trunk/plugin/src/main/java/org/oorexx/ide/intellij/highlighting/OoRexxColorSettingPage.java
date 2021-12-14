package org.oorexx.ide.intellij.highlighting;

import java.util.Map;
import javax.swing.*;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.oorexx.ide.file.OoRexxIcons;

public class OoRexxColorSettingPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Keyword", OoRexxSyntaxHighlighter.OOREXX_KEYWORD),
            new AttributesDescriptor("Number", OoRexxSyntaxHighlighter.OOREXX_NUMBER),
            new AttributesDescriptor("Strings", OoRexxSyntaxHighlighter.BAD_CHARACTER),
            new AttributesDescriptor("Line Comments", OoRexxSyntaxHighlighter.OOREXX_LINE_COMMENT),
            new AttributesDescriptor("Block Comments", OoRexxSyntaxHighlighter.OOREXX_BLOCK_COMMENT),
            new AttributesDescriptor("Self", OoRexxSyntaxHighlighter.OOREXX_SELF),
            new AttributesDescriptor("Built-In Function", OoRexxSyntaxHighlighter.OOREXX_BUILT_IN),
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
                " say space(\"    text with   extra  spaces \")\n" +
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
