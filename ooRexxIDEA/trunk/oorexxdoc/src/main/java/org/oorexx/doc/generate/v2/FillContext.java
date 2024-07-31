package org.oorexx.doc.generate.v2;

import freemarker.template.Template;
import org.oorexx.doc.analyse.model.OoRexxPackage;
import org.oorexx.doc.input.model.OoRexxDocGenerationContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FillContext {
    private OoRexxDocGenerationContext ooRexxDocGenerationContext;
    private TemplateBundle templateBundle;
    private List<OoRexxPackage> ooRexxPackages;
    private Object current;
    private Map<String, Template> templateMapping;

    public FillContext() {
    }

    public FillContext(OoRexxDocGenerationContext ooRexxDocGenerationContext, TemplateBundle templateBundle, List<OoRexxPackage> ooRexxPackages, Object current, Map<String, Template> templateMapping) {
        this.ooRexxDocGenerationContext = ooRexxDocGenerationContext;
        this.templateBundle = templateBundle;
        this.ooRexxPackages = ooRexxPackages;
        this.current = current;
        this.templateMapping = templateMapping;
    }

    public OoRexxDocGenerationContext getOoRexxDocGenerationContext() {
        return ooRexxDocGenerationContext;
    }

    public void setOoRexxDocGenerationContext(OoRexxDocGenerationContext ooRexxDocGenerationContext) {
        this.ooRexxDocGenerationContext = ooRexxDocGenerationContext;
    }

    public TemplateBundle getTemplateBundle() {
        return templateBundle;
    }

    public void setTemplateBundle(TemplateBundle templateBundle) {
        this.templateBundle = templateBundle;
    }

    public List<OoRexxPackage> getOoRexxPackages() {
        return ooRexxPackages;
    }

    public void setOoRexxPackages(List<OoRexxPackage> ooRexxPackages) {
        this.ooRexxPackages = ooRexxPackages;
    }

    public Object getCurrent() {
        return current;
    }

    public void setCurrent(Object current) {
        this.current = current;
    }

    public Map<String, Template> getTemplateMapping() {
        return templateMapping;
    }

    public void setTemplateMapping(Map<String, Template> templateMapping) {
        this.templateMapping = templateMapping;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FillContext that = (FillContext) o;
        return Objects.equals(current, that.current) && Objects.equals(templateBundle, that.templateBundle) && Objects.equals(ooRexxPackages, that.ooRexxPackages) && Objects.equals(templateMapping, that.templateMapping) && Objects.equals(ooRexxDocGenerationContext, that.ooRexxDocGenerationContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ooRexxDocGenerationContext, templateBundle, ooRexxPackages, current, templateMapping);
    }
}
