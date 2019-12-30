package org.oorexx.doc.analyse.model;

public enum AccessModifier {
    PUBLIC, PRIVATE;

    public static AccessModifier fromText(String text){
        if("private".equalsIgnoreCase(text)){
            return PRIVATE;
        } else if("public".equalsIgnoreCase(text)){
            return PUBLIC;
        }

        return null;
    }

}
