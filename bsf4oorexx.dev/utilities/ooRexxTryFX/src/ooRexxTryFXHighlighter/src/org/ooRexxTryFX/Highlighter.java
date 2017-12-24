package org.ooRexxTryFX;


/**
 * Created by Adrian Baginski on 20.07.2017.
 */

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.impl.source.tree.FileElement;
import javafx.concurrent.Task;
import javafx.util.Pair;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.richtext.CodeArea;
import org.oorexx.ide.standalone.OoRexxStandaloneParser;


public class Highlighter {
    private CodeArea codeArea;
    private ExecutorService executor;
    public Highlighter(CodeArea codeArea) {
        this.codeArea = codeArea;
        executor = Executors.newSingleThreadExecutor();
        codeArea.plainTextChanges()
            .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
            .successionEnds(Duration.ofMillis(500))
            .supplyTask(this::computeHighlightingAsync)
            .awaitLatest(codeArea.plainTextChanges())
            .filterMap(t -> {
                if(t.isSuccess()) {
                    return Optional.of(t.get());
                } else {
                    t.getFailure().printStackTrace();
                    return Optional.empty();
                }
            })
            .subscribe(this::applyHighlighting);
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

    public StyleSpans<Collection<String>> computeHighlighting(String code) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        instructionLengthMap.clear();
        FileElement node = (FileElement) OoRexxStandaloneParser.parse(code);
        LighterAST tree = node.getLighterAST();
        walkTheAST(tree, tree.getRoot());
        for(Object map: instructionLengthMap) {
            Pair pair = (Pair) map;
            String type = parseType((String) pair.getKey());
            type = type.toLowerCase();
            int length = (int) pair.getValue();
            spansBuilder.add(Collections.singleton(type), length);
        }
        return spansBuilder.create();
    }
    /* ooRexx Syntax Highlighting */
    private static List instructionLengthMap = new ArrayList<Pair>();
    private static void walkTheAST(LighterAST tree, LighterASTNode root) {
        walkTheAST(tree, root, 0);
    }
    private static void walkTheAST(LighterAST tree, LighterASTNode root, Integer level) {
        List<LighterASTNode> children = tree.getChildren(root);
        for(LighterASTNode child: children) {
            List<LighterASTNode> grandchildren = tree.getChildren(child);
            String type = child.getTokenType().toString();
            int start = child.getStartOffset();
            int end = child.getEndOffset();
            int length = end - start;
            if(grandchildren.size() > 0)
                walkTheAST(tree, child, level+1);
            else if(length > 0)
                instructionLengthMap.add(new Pair(type, length));
        }
    }
    private static String parseType(String type) {
        switch(type) {
            case ":":
                return "directive";
            case ".false":
                return "false";
            case ".true":
                return "true";
            case ".nil":
                return "nil";
            case "<":
            case ">":
            case "=":
            case "\\":
            case "¬":
            case "&":
            case "|":
                return "comparison";
            case "-":
            case "+":
            case "/":
            case "*":
            case "%":
                return "arithmetic";
            case "~":
                return "message";
            default:
                return type;
        }
    }
}
