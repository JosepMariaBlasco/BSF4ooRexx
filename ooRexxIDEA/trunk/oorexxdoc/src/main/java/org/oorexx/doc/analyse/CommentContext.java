package org.oorexx.doc.analyse;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.oorexx.ide.file.OoRexxFile;
import org.oorexx.ide.lang.psi.OoRexxDirective;
import org.oorexx.ide.lang.psi.OoRexxInstructionTerminator;
import org.oorexx.ide.lang.psi.impl.OoRexxInstructionTerminatorImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommentContext {

    private ArrayList<PsiComment> docComments = new ArrayList<>();
    private ArrayList<OoRexxDirective> ooRexxDirectives = new ArrayList<>();
    private ArrayList<PsiElement> combinedList = new ArrayList<>();

    public CommentContext(OoRexxFile ooRexxFile) {
        Collection<PsiComment> psiComments = PsiTreeUtil.findChildrenOfType(ooRexxFile, PsiComment.class);
        this.docComments.addAll(psiComments);
        Collection<OoRexxInstructionTerminator> instT = PsiTreeUtil.findChildrenOfType(ooRexxFile, OoRexxInstructionTerminatorImpl.class);
        List<OoRexxInstructionTerminator> collect = instT.stream().collect(Collectors.toList());

        collect.forEach(a -> {
            for (PsiElement current = a.getFirstChild(); current != null; current = current.getNextSibling()) {
                if (current instanceof PsiComment){
                    docComments.add((PsiComment) current);
                }
            }
        });
        this.docComments = docComments.stream()
                .filter(psiComment -> "DOC_COMMENT".equalsIgnoreCase(psiComment.getTokenType().toString()))
                .collect(Collectors.toCollection(ArrayList::new));

        this.ooRexxDirectives = new ArrayList<>(PsiTreeUtil.findChildrenOfType(ooRexxFile, OoRexxDirective.class));

        this.combinedList.addAll(docComments);
        this.combinedList.addAll(ooRexxDirectives);

        this.combinedList.sort((o1, o2) -> new Integer(o1.getTextOffset()).compareTo(o2.getTextOffset()));
    }

    public PsiComment getComment(OoRexxDirective ooRexxDirective){
        if(ooRexxDirective == null && !combinedList.isEmpty() && combinedList.get(0) instanceof PsiComment){
            return (PsiComment) combinedList.get(0);
        }
        int i = this.combinedList.indexOf(ooRexxDirective);

        if(i <= 0){
            return null;
        }

        PsiElement psiElement = this.combinedList.get(i - 1);

        if(psiElement instanceof PsiComment){
            return (PsiComment) psiElement;
        }
        return null;
    }

    public PsiComment getComment(OoRexxFile ooRexxFile){
        if(this.combinedList.size() == 1){
            if(this.combinedList.get(0) instanceof PsiComment){
                return (PsiComment) this.combinedList.get(0);
            }
        }

        if(this.combinedList.size() > 1){
            PsiElement first = this.combinedList.get(0);
            PsiElement second = this.combinedList.get(1);

            if(first instanceof PsiComment && second instanceof PsiComment){
                return (PsiComment) first;
            }
        }

        return null;
    }
}