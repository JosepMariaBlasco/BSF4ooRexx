package org.oorexx.doc.util;

import org.apache.commons.lang3.StringUtils;
import org.oorexx.doc.analyse.model.OoRexxComment;
import org.oorexx.doc.analyse.model.OoRexxCommentAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommentUtil {

    public static String removeTags(String comment){
        return comment.replaceAll("/\\*\\*", "").replaceAll("\\*/", "").replaceAll("\\n *\\*", "").replaceAll("\\n\\r *\\*", "");
    }

    private static String[] annotations = {"@author","@version","@param","@return","@see","@since","@deprecated","@condition"};
    
    public static OoRexxComment extractComment(String comment){
        List<String> rawAnnotations = new ArrayList<>();
        int offset = firstIndexOfAnnotation(comment.toLowerCase(), 0);
        int loopCounter = 0;
        if(StringUtils.containsAny(comment.toLowerCase(), annotations)) {
            while(loopCounter < 200) { // prevents endless loop
                int newOffset = firstIndexOfAnnotation(comment.toLowerCase(), offset + 1);
                String text = null;
                if(newOffset == -1){
                    text = comment.substring(offset);
                    rawAnnotations.add(text);
                    break;
                }
                text = comment.substring(offset, newOffset);
                rawAnnotations.add(text);
                offset = newOffset;
                loopCounter++;
            }
        }

        OoRexxComment ooRexxComment = new OoRexxComment();
        int firstOffset = firstIndexOfAnnotation(comment.toLowerCase(), 0);
        if(firstOffset == -1){
            firstOffset = comment.length();
        }
        String commentText = comment.substring(0, firstOffset);
        ooRexxComment.setText(commentText.trim());
        ooRexxComment.setCommentAnnotations(extractAnnotation(rawAnnotations));
        return ooRexxComment;
    }

    private static int firstIndexOfAnnotation(String text, int offset){
        String searchText = text.toLowerCase();
        List<Integer> indices = new ArrayList<>();
        for (String annotation : annotations) {
            int i = searchText.indexOf(annotation, offset);
            if(i >= offset) {
                indices.add(i);
            }
        }

        if(indices.isEmpty()){
            return -1;
        }

        indices.sort(Integer::compareTo);

        return indices.get(0);
    }

    private static List<OoRexxCommentAnnotation> extractAnnotation(List<String> rawAnnotations) {
        return rawAnnotations.stream()
                .map(s -> {
                    int spaceIndex = s.indexOf(' ');
                    return new OoRexxCommentAnnotation(s.substring(1, spaceIndex).trim(), s.substring(spaceIndex + 1).trim());
                }).collect(Collectors.toList());
    }

}
