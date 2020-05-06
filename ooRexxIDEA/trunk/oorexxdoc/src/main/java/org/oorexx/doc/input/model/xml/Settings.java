package org.oorexx.doc.input.model.xml;

public class Settings {

    private boolean includePublic = true;
    private boolean includePrivate;
    private boolean includePackage;
    private boolean includeVariableReferences;

    public boolean isIncludePublic() {
        return includePublic;
    }

    public void setIncludePublic(boolean includePublic) {
        this.includePublic = includePublic;
    }

    public boolean isIncludePrivate() {
        return includePrivate;
    }

    public void setIncludePrivate(boolean includePrivate) {
        this.includePrivate = includePrivate;
    }

    public boolean isIncludePackage() {
        return includePackage;
    }

    public void setIncludePackage(boolean includePackage) {
        this.includePackage = includePackage;
    }

    public boolean isIncludeVariableReferences() {
        return includeVariableReferences;
    }

    public void setIncludeVariableReferences(boolean includeVariableReferences) {
        this.includeVariableReferences = includeVariableReferences;
    }
}
