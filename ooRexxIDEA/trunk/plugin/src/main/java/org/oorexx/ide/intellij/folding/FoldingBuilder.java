package org.oorexx.ide.intellij.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.oorexx.ide.file.OoRexxFile;
import org.oorexx.ide.lang.OoRexxTypes;
import org.oorexx.ide.lang.psi.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.intellij.psi.util.PsiTreeUtil.collectElementsOfType;

public class FoldingBuilder extends FoldingBuilderEx {

    private List<FoldingDescriptor> descriptors = new ArrayList<>();

    private TextRange limit;

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean fast) {
        if(!(root instanceof OoRexxFile)) return new FoldingDescriptor[0];

        limit = root.getTextRange();

        descriptors = new ArrayList<>();

        createSelectFoldingDescriptor(root);
        createDoFoldingDescriptors(collectElementsOfType(root, OoRexxKeywordDo.class));

        Collection<PsiComment> psiComments = collectElementsOfType(root, PsiComment.class);
        createCommentFoldingDescriptors(psiComments);

        Collection<OoRexxDirective> directives = collectElementsOfType(root, OoRexxDirective.class);

        List<PsiElement> list = new ArrayList<>();
        list.addAll(psiComments);
        list.addAll(directives);

        Collections.sort(list, new Comparator<PsiElement>() {
            @Override
            public int compare(PsiElement o1, PsiElement o2) {
                return new Integer(o1.getTextOffset()).compareTo(o2.getTextOffset());
            }
        });

        List<PsiElement> filtered_ = new ArrayList<>();

        for(int i = 0 ; i < list.size() - 1; i++){
            PsiElement current = list.get(i);
            PsiElement next = list.get(i + 1);
            if(current instanceof OoRexxDirective){
                filtered_.add(current);
            } else if(current instanceof PsiComment && (next instanceof OoRexxDirective) && isDirectiveComment(root, current, next)){
                filtered_.add(current);
            }
        }

        if(list.size() > 1)
            filtered_.add(list.get(list.size() -1));

        list = filtered_;

        List<DirectiveWrapper> filtered = list.stream().filter(new Predicate<PsiElement>() {
            @Override
            public boolean test(PsiElement psiElement) {
                return psiElement instanceof OoRexxDirective;
            }
        }).map(new Function<PsiElement, DirectiveWrapper>() {
            @Override
            public DirectiveWrapper apply(PsiElement psiElement) {
                return new DirectiveWrapper(null, psiElement);
            }
        }).collect(Collectors.toList());

        for(DirectiveWrapper wrapper: filtered){
            int index = list.indexOf(wrapper.getDirective());

            if(index != 0){
                PsiElement psiElement = list.get(index - 1);

                if(psiElement instanceof PsiComment){
                    wrapper.setComment(psiElement);
                }
            }
        }

        createDirectiveFoldingDescriptor(filtered);


        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    private boolean isDirectiveComment(PsiElement root, PsiElement comment, PsiElement directive){

        if(PsiUtilCore.getElementType(comment).equals(OoRexxTypes.EOL_COMMENT)) return false;

        String text = root.getText().substring(comment.getTextOffset() + comment.getTextLength(), directive.getTextOffset());

        return text.trim().isEmpty();
    }


    private void createDirectiveFoldingDescriptor(List<DirectiveWrapper> wrappers){

        DirectiveWrapper nextWrapper = null;
        DirectiveWrapper activeClassWrapper = null;

        for (int i = 0; i < wrappers.size() - 1; i++) {
            DirectiveWrapper wrapper = wrappers.get(i);

            PsiElement directive = wrapper.getDirective();
            PsiElement comment = wrapper.getComment();

            nextWrapper = wrappers.get(i + 1);

            PsiElement specificDirective = directive.getChildren()[1];

            if(i == 0){
                if(!(specificDirective instanceof OoRexxDirectiveClass)){
                    try{
                        if(isFoldAble(specificDirective))
                            descriptors.add(new FoldingDescriptor(specificDirective.getNode(), getRange(wrapper, nextWrapper)));
                    }catch (AssertionError e){
                        //TODO: fix this
                    }
                }
            }

            if((specificDirective instanceof OoRexxDirectiveClass ||
                    specificDirective instanceof OoRexxDirectiveRoutine ||
                    specificDirective instanceof OoRexxDirectiveRequires ||
                    specificDirective instanceof OoRexxDirectiveAnnotate ||
                    specificDirective instanceof OoRexxDirectiveResource ||
                    specificDirective instanceof OoRexxDirectiveOptions ||
                    specificDirective instanceof OoRexxDirectiveConstant) && activeClassWrapper != null){
                try{
                    if(isFoldAble(specificDirective))
                        descriptors.add(new FoldingDescriptor(activeClassWrapper.getDirective().getNode(), getRange(activeClassWrapper, wrapper)));
                }catch (AssertionError e){
                    //TODO: fix this
                }
                activeClassWrapper = null;
            }

            if(specificDirective instanceof OoRexxDirectiveClass && activeClassWrapper == null){
                activeClassWrapper = wrapper;
            }

            if(!(specificDirective instanceof OoRexxDirectiveClass)){
                try {
                    if(isFoldAble(specificDirective))
                        descriptors.add(new FoldingDescriptor(directive.getNode(), getRange(wrapper, nextWrapper)));
                }catch (AssertionError e){
                    //TODO: fix this
                }
            }
        }

        if(wrappers.size() > 1){
            try{
                if(isFoldAble(nextWrapper.getDirective()))
                    descriptors.add(new FoldingDescriptor(nextWrapper.getDirective().getNode(), getRange(nextWrapper, null)));
            }catch (AssertionError e){
                //TODO: fix this
            }
        }

        if(activeClassWrapper != null){
            try{
                if(isFoldAble(nextWrapper.getDirective()))
                    descriptors.add(new FoldingDescriptor(activeClassWrapper.getDirective().getNode(), getRange(activeClassWrapper, null)));
            }catch (AssertionError e){
                //TODO: fix this
            }
        }
    }

    private boolean isFoldAble(PsiElement ooRexxDirective){
        if(ooRexxDirective.getChildren().length <= 1) return false;

        PsiElement directiveType = ooRexxDirective.getChildren()[1];

        return !(directiveType instanceof OoRexxDirectiveRequires
                || directiveType instanceof OoRexxDirectiveConstant
                || directiveType instanceof  OoRexxDirectiveAnnotate
                || directiveType instanceof OoRexxDirectiveOptions
        );
    }

    private TextRange getRange(DirectiveWrapper directiveWrapper, DirectiveWrapper nextWrapper){
        TextRange textRange = null;
        int start, end;

        if(nextWrapper == null) {
            start = directiveWrapper.getDirective().getTextOffset() + directiveWrapper.getDirective().getText().indexOf("\n");
            end = directiveWrapper.getDirective().getTextLength() + directiveWrapper.getDirective().getTextOffset();
        } else {
            start = directiveWrapper.getDirective().getTextOffset() + directiveWrapper.getDirective().getText().indexOf("\n");
            end = nextWrapper.getAnchorElement().getTextOffset() - 1;
        }

        if (start > end) {
            textRange = new TextRange(end, start);
        }
        else {
            textRange = new TextRange(start, end);
        }

        if(textRange.getLength() > limit.getLength())
            return new TextRange(start, limit.getLength());

        return textRange;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode astNode) {
        return false;
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode astNode) {
        return null;
    }

    private void createDoFoldingDescriptors(Collection<OoRexxKeywordDo> psiElements){
        for(OoRexxKeywordDo ooRexxKeywordDo: psiElements){
            try{
                TextRange textRange = new TextRange(ooRexxKeywordDo.getFirstChild().getTextOffset() + ooRexxKeywordDo.getFirstChild().getTextLength(), ooRexxKeywordDo.getFirstChild().getTextOffset() +  ooRexxKeywordDo.getTextLength() );
                descriptors.add(new FoldingDescriptor(ooRexxKeywordDo, textRange));
            }catch (AssertionError e){
                //TODO: fix this
            }
        }
    }

    private void createCommentFoldingDescriptors(Collection<PsiComment> psiComments){
        try{
            descriptors.addAll(psiComments.stream().map(psiComment -> new FoldingDescriptor(psiComment.getNode(), psiComment.getTextRange())).collect(Collectors.toList()));
        }catch (AssertionError e){
            //TODO: fix this
        }
    }

    private void createSelectFoldingDescriptor(PsiElement root){
        Collection<PsiElement> selects = PsiTreeUtil.findChildrenOfAnyType(root, OoRexxKeywordSelect.class);
        for(PsiElement select: selects){
            PsiElement first = select.getFirstChild();
            PsiElement last = select.getLastChild();

            try{
                descriptors.add(new FoldingDescriptor(select.getNode(),new TextRange(select.getTextOffset() + select.getText().indexOf("\n"), last.getTextOffset() + last.getTextLength() )));
            }catch (AssertionError e){
                //TODO: fix this
            }
        }

    }

    private class DirectiveWrapper{
        PsiElement comment;
        PsiElement directive;

        public DirectiveWrapper(PsiElement comment, PsiElement directive) {
            this.comment = comment;
            this.directive = directive;
        }

        public PsiElement getComment() {
            return comment;
        }

        public PsiElement getDirective() {
            return directive;
        }

        public PsiElement getAnchorElement(){
            return comment != null? comment : directive;
        }

        public void setComment(PsiElement comment) {
            this.comment = comment;
        }
    }

}
