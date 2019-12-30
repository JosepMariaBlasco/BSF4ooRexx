package org.oorexx.doc.util;

import org.apache.commons.lang3.StringUtils;
import org.oorexx.doc.analyse.model.OoRexxComment;
import org.oorexx.doc.analyse.model.OoRexxCommentAnnotation;

public class CommentUtil {

    public static String removeTags(String comment){
        return comment.replaceAll("/\\*\\*", "").replaceAll("\\*/", "").replaceAll("\\n *\\*", "").replaceAll("\\n\\r *\\*", "");
    }

    private static String[] annotations = {"@author","@version","@param","@return","@see","@since","@deprecated","@condition"};
    
    public static OoRexxComment extractComment(String comment){

        OoRexxComment ooRexxComment = new OoRexxComment();

        int annotationStart = nextAnnotationIndex(comment, 0);
        int offset = 0;
        if(annotationStart != -1 && annotationStart != comment.length()){
            do {
                offset = annotationStart;
                annotationStart = nextAnnotationIndex(comment, annotationStart + 1);
                String annotationText = StringUtils.substring(comment, offset, annotationStart);
                extractAnnotation(annotationText, ooRexxComment);
                
            } while (annotationStart != -1 && annotationStart != comment.length());
        }

        ooRexxComment.setText(StringUtils.trim(StringUtils.substring(comment, 0, nextAnnotationIndex(comment, 0))));

        return ooRexxComment;
    }

    private static void extractAnnotation(String annotationText, OoRexxComment ooRexxComment){
        int spaceIndex = annotationText.indexOf(' ');

        String annotation = annotationText.substring(1, spaceIndex);
        String text = annotationText.substring(spaceIndex + 1, annotationText.length());

        ooRexxComment.getCommentAnnotations().add(new OoRexxCommentAnnotation(annotation, text));
    }

    private static int nextAnnotationIndex(String comment, int start){
        for(String annotation: annotations){
            int i = StringUtils.indexOfIgnoreCase(comment, annotation, start);
            if(i != -1){
                return i;
            }
        }
        return comment.length();
    }


}
