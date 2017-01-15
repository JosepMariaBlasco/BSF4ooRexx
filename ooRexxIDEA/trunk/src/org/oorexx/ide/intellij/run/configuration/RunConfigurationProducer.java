package org.oorexx.ide.intellij.run.configuration;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.oorexx.ide.intellij.file.OoRexxFileType;
import org.oorexx.ide.intellij.file.OoRexxFileTypeFactory;
import org.oorexx.ide.lang.psi.OoRexxShebang;

/**
 * Created by alex on 12.09.16.
 */
public class RunConfigurationProducer extends com.intellij.execution.actions.RunConfigurationProducer<RunConfiguration>{

    public RunConfigurationProducer() {
        super(RunConfigurationType.getInstance().getConfigurationFactories()[0]);
    }

    @Override
    protected boolean setupConfigurationFromContext(RunConfiguration runConfiguration, ConfigurationContext configurationContext, Ref<PsiElement> ref) {
        VirtualFile file = findFile(configurationContext);
        if (file != null && isExecutableFile(file)) {
            runConfiguration.setScriptFile(file);
            runConfiguration.setGeneratedName();

            PsiElement shebang = PsiTreeUtil.findChildOfType(ref.get(), OoRexxShebang.class);
            if(shebang != null) {
                runConfiguration.setExecutablePath(shebang.getText().substring(2, shebang.getText().length()));
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(RunConfiguration runConfiguration, ConfigurationContext configurationContext) {
        VirtualFile file = findFile(configurationContext);
        return file != null && Comparing.equal(runConfiguration.getScriptFile(), file);
    }

    public VirtualFile findFile(ConfigurationContext configurationContext){
        Location location = configurationContext.getLocation();
        return location == null ? null : location.getVirtualFile();
    }

    public static boolean isExecutableFile(VirtualFile virtualFile){

        for(String ext: OoRexxFileTypeFactory.fileExtensions)
            if(virtualFile.getPath().endsWith(ext))
                return true;
        return false;
    }


}
