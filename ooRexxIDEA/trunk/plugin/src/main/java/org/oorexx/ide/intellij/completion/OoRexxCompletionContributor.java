package org.oorexx.ide.intellij.completion;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementRenderer;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.lang.psi.OoRexxCommandExpr;
import org.oorexx.ide.lang.psi.OoRexxNumericExpr;
import org.oorexx.ide.setting.OoRexxLanguageSettings;

import java.io.InputStream;
import java.util.*;

public class OoRexxCompletionContributor extends CompletionContributor {

    public static final String METHODS = "methods";
    public static final String FUNCTIONS = "functions";
    public static final String ENVIRONMENT = "environment symbols";
    public static final String KEYWORDS = "keywords";
    public static final String DIRECTIVES = "directives";
    public static final String COMMANDS = "commands";
    public static final String TT_METHOD = "Method";
    public static final String TT_FUNCTION = "Function";
    public static final String TT_ENVIRONMENT = "Environment";
    public static final String TT_KEYWORD = "Keyword";
    public static final String TT_DIRECTIVE = "Directive";
    public static final String TT_COMMAND = "JDOR Commands";

    public OoRexxCompletionContributor(){
        Map<String, List<ElementData>> elements = loadCompletionElements();

        extend(CompletionType.BASIC, generalPattern(),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext processingContext,
                                               @NotNull CompletionResultSet resultSet) {
                        if(shouldStop(parameters, processingContext, resultSet)){
                            return;
                        }
                        elements.get(KEYWORDS).forEach(
                                s -> resultSet.addElement(LookupElementBuilder.create(s.value + " ")
                                        .withInsertHandler((context, item) -> {
                                            removeLeadingDot(context);
                                            preventParentheseAutoComplete(context);
                                            handleCtrlDot(context, false);
                                        })
                                        .withRenderer(new LookupElementRenderer<>() {
                                            @Override
                                            public void renderElement(LookupElement element, LookupElementPresentation presentation) {
                                                presentation.setItemText(s.value);
                                                presentation.setTailText(" - " + s.tailText, true);
                                            }
                                        })

                                ));
                    }


                }
        );

        extend(CompletionType.BASIC, generalPattern(),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext processingContext,
                                               @NotNull CompletionResultSet resultSet) {
                        if(shouldStop(parameters, processingContext, resultSet)){
                            return;
                        }
                        elements.get(METHODS).forEach(
                                s -> resultSet.addElement(LookupElementBuilder.create(s.value)
                                        .appendTailText(" - " + s.tailText, true)
                                        .withInsertHandler((context, item) -> {
                                            removeLeadingDot(context);
                                            handleCtrlDot(context, true);
                                        })
                                ));
                    }
                }
        );

        extend(CompletionType.BASIC, generalPattern(),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        if(shouldStop(parameters, context, resultSet)){
                            return;
                        }
                        elements.get(FUNCTIONS).forEach(
                                s -> resultSet.addElement(LookupElementBuilder.create(s.value)
                                        .appendTailText(" - " + s.tailText, true)
                                        .withInsertHandler((context1, item) -> {
                                                    removeLeadingDot(context1);
                                                    handleCtrlDot(context1, true);
                                                }
                                        )
                                ));

                    }
                }
        );


        extend(CompletionType.BASIC, generalPattern(),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext processingContext,
                                               @NotNull CompletionResultSet resultSet) {

                        if(shouldStop(parameters, processingContext, resultSet)){
                            return;
                        }
                        elements.get(ENVIRONMENT).forEach(
                                s -> resultSet.addElement(LookupElementBuilder.create(s.value)
                                        .appendTailText(" - " + s.tailText, true)
                                        .withInsertHandler((context, item) -> {
                                            removeLeadingDot(context);
                                            preventParentheseAutoComplete(context);
                                            handleCtrlDot(context, true);
                                        })

                                ));
                    }
                }
        );

        extend(CompletionType.BASIC, generalPattern(),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext processingContext,
                                               @NotNull CompletionResultSet resultSet) {

                        if(shouldStop(parameters, processingContext, resultSet)){
                            return;
                        }
                        elements.get(DIRECTIVES).forEach(
                                s -> resultSet.addElement(LookupElementBuilder.create(s.value + " ")
                                        .withInsertHandler((context, item) -> {
                                            removeLeadingDot(context);
                                            preventParentheseAutoComplete(context);
                                            handleCtrlDot(context, false);
                                        })
                                        .withRenderer(new LookupElementRenderer<>() {
                                            @Override
                                            public void renderElement(LookupElement element, LookupElementPresentation presentation) {
                                                presentation.setItemText(s.value);
                                                presentation.setTailText(" - " + s.tailText, true);
                                            }
                                        })

                                ));
                    }
                }
        );

        extend(CompletionType.BASIC, generalPattern(),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext processingContext,
                                               @NotNull CompletionResultSet resultSet) {

                        if(PsiTreeUtil.getParentOfType(parameters.getPosition(), OoRexxCommandExpr.class) == null
                                || shouldStop(parameters, processingContext, resultSet)){
                            return;
                        }
                        elements.get(COMMANDS).forEach(
                                s -> resultSet.addElement(LookupElementBuilder.create(s.value + " ")
                                        .withInsertHandler((context, item) -> {
                                            removeLeadingDot(context);
                                            preventParentheseAutoComplete(context);
                                            handleCtrlDot(context, false);
                                        })
                                        .withRenderer(new LookupElementRenderer<>() {
                                            @Override
                                            public void renderElement(LookupElement element, LookupElementPresentation presentation) {
                                                presentation.setItemText(s.value);
                                                presentation.setTailText(" - " + s.hint + " - " + s.tailText, true);
                                            }
                                        })

                                ));
                    }
                }
        );


    }

    private Map<String, List<ElementData>> loadCompletionElements() {
        List<ElementData> methodNames = transform(getCompletions("/completion/methodNames.txt"), TT_METHOD);
        List<ElementData> functionNames = transform(getCompletions("/completion/functionNames.txt"), TT_FUNCTION);
        List<ElementData> environmentNames = transform(getCompletions("/completion/environmentNames.txt"), TT_ENVIRONMENT);
        List<ElementData> keywordNames = transform(getCompletions("/completion/keywordNames.txt"), TT_KEYWORD);
        List<ElementData> directivesNames = transform(getCompletions("/completion/directivesNames.txt"), TT_DIRECTIVE);
        List<ElementData> commandNames = transform(getCompletions("/completion/commandNames.txt"), TT_COMMAND);

        Map<String, List<ElementData>> elements = new HashMap<>();

        elements.put(METHODS, methodNames);
        elements.put(FUNCTIONS, functionNames);
        elements.put(ENVIRONMENT, environmentNames);
        elements.put(KEYWORDS, keywordNames);
        elements.put(DIRECTIVES, directivesNames);
        elements.put(COMMANDS, commandNames);

        ExternalCompletionElementsUtil.loadExternal(elements);

        return elements;
    }

    private List<String> getCompletions(String path) {
        InputStream resourceAsStream = OoRexxCompletionContributor.class.getResourceAsStream(path);

        return IOUtils.readLines(resourceAsStream, "UTF-8");
    }

    private boolean shouldStop(CompletionParameters parameters, ProcessingContext context, CompletionResultSet resultSet) {
        PsiElement psiElement = PsiTreeUtil.prevVisibleLeaf(parameters.getPosition());
        if(psiElement == null){
            return false;
        }
        OoRexxNumericExpr parentOfType = PsiTreeUtil.getParentOfType(psiElement, OoRexxNumericExpr.class);
        if(parentOfType == null){
            return false;
        }
        String text = parameters
                .getEditor()
                .getDocument()
                .getText(new TextRange(Math.max(0, psiElement.getTextOffset()-20), parameters.getPosition().getTextOffset()))
                .replace(" ", "")
                ;

        return !text.endsWith(";") && !text.endsWith("\n");
    }

    @NotNull
    private static PsiElementPattern.Capture<PsiElement> generalPattern() {
        return PlatformPatterns.psiElement().andNot(PlatformPatterns.psiElement().
                beforeLeaf(PlatformPatterns.psiElement().withParent(OoRexxNumericExpr.class)));
    }

    private static void removeLeadingDot(InsertionContext context) {
        String text = context.getDocument().getText(new TextRange(Math.max(0,context.getStartOffset()-1), context.getStartOffset()));
        if(text.equals(".")) {
            context.getDocument().replaceString(Math.max(0, context.getStartOffset()-1), context.getStartOffset(), "");
        }
    }

    private static void preventParentheseAutoComplete(InsertionContext context) {
        if(context.getCompletionChar() == '(') {
            context.setAddCompletionChar(false);
        }
    }

    private static void handleCtrlDot(InsertionContext context, boolean enableCompletion) {
        if(context.getCompletionChar() == '.') {
            context.setAddCompletionChar(false);
            if(enableCompletion) {
                context.getDocument().insertString(context.getTailOffset(), "~");
                context.getEditor().getCaretModel().moveToOffset(context.getTailOffset());
            }
        }
    }

    private List<ElementData> transform(List<String> completions, String directive) {
        OoRexxLanguageSettings.AutoCompletionCasing autoCompletionCasing = OoRexxLanguageSettings.getInstance().getAutoCompletionCasing();

        return Lists.newArrayList(completions.stream().map(s -> new ElementData(getSanitizedText(map(autoCompletionCasing, s)), directive, getHint(s))).toList());
    }

    public static String getHint(String value) {
        if(!value.contains("--")) {
            return "";
        }
        String[] split = value.split("\s*--\s*");
        return split[1];
    }

    public static String getSanitizedText(String value) {
        if(!value.contains("--")) {
            return value;
        }
        String[] split = value.split("\s*--\s*");
        return split[0];
    }

    private String map(OoRexxLanguageSettings.AutoCompletionCasing autoCompletionCasing, String s) {
        return switch (autoCompletionCasing) {
            case CAMEL_CASE -> s;
            case UPPER_CASE -> s.toUpperCase();
            case LOWER_CASE -> s.toLowerCase();
            case CAPITALIZE -> StringUtils.capitalize(s);
        };
    }

    record ElementData(String value, String tailText, String hint){

    }


}
