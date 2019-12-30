package org.oorexx.doc.analyse.model;

import java.util.ArrayList;
import java.util.List;

public class OoRexxComment {

    private String text = "";
    private List<OoRexxCommentAnnotation> commentAnnotations = new ArrayList<>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<OoRexxCommentAnnotation> getCommentAnnotations() {
        return commentAnnotations;
    }

    public void setCommentAnnotations(List<OoRexxCommentAnnotation> commentAnnotations) {
        this.commentAnnotations = commentAnnotations;
    }
}
