package org.oorexx.doc.analyse.model;


import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.text.StringEscapeUtils;
import org.oorexx.doc.util.CommentUtil;

public abstract class OoRexxElement {

    private PsiElement psiElement;
    private String commentText;
    private OoRexxComment comment;

    public OoRexxElement(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }


    public void setCommentText(PsiComment psiComment) {
        if(psiComment != null) {
            this.commentText = CommentUtil.removeTags(psiComment.getText());
            this.comment = CommentUtil.extractComment(commentText);
        }
    }

    public String getCommentText(){
        if(commentText == null){
            return "";
        }
        return StringEscapeUtils.escapeHtml4(comment.getText());
    }

    public OoRexxComment getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
