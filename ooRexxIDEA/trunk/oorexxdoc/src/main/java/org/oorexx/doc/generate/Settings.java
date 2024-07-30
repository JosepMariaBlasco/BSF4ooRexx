package org.oorexx.doc.generate;

public class Settings {

    private boolean includePublic = true;
    private boolean includePrivate;
    private boolean includePackage;
    private boolean includeVariableReferences;

    public Settings(boolean includePublic, boolean includePrivate, boolean includePackage, boolean includeVariableReferences) {
        this.includePublic = includePublic;
        this.includePrivate = includePrivate;
        this.includePackage = includePackage;
        this.includeVariableReferences = includeVariableReferences;
    }

    public Settings() {
    }

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

    public void setIncludeVariableReferences(boolean includeVariableReferences) {
        this.includeVariableReferences = includeVariableReferences;
    }

    public boolean isIncludeVariableReferences() {
        return includeVariableReferences;
    }
}
