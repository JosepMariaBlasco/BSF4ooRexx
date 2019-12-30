package org.oorexx.doc.analyse.model.reference;

import org.oorexx.doc.analyse.model.OoRexxClass;
import org.oorexx.doc.analyse.model.OoRexxPackage;

import java.util.Optional;

public class LinkedClassReference extends ClassReference {

    private OoRexxClass ooRexxClass;

    public LinkedClassReference(OoRexxPackage ooRexxPackage, String name) {
        super(name);
        Optional<OoRexxClass> any = ooRexxPackage.getClasses().stream()
                .filter(ooRexxClass -> ooRexxClass.getName().equals(name)).findAny();

        ooRexxClass = any.orElseThrow(() -> new RuntimeException("Could not find class " + name + " in package " + ooRexxPackage.getName()));
    }

    public LinkedClassReference(OoRexxClass ooRexxClass) {
        super(ooRexxClass.getName());
        this.ooRexxClass = ooRexxClass;
    }

    public OoRexxClass getOoRexxClass() {
        return ooRexxClass;
    }

    @Override
    public String getReference() {
        String href = "../packages/" + ooRexxClass.getOoRexxPackage().getName();
        return String.format("<a href=\"%s.html#%s\">%s</a>", href, ooRexxClass.getId(), ooRexxClass.getName());
    }
}
