package org.oorexx.ide.intellij.run.configuration;

import java.io.File;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.oorexx.ide.file.OoRexxFileTypeFactory;
import org.oorexx.ide.lang.psi.OoRexxShebang;

/**
 * Created by alex on 12.09.16.
 */
public class RunConfigurationProducer extends com.intellij.execution.actions.RunConfigurationProducer<RunConfiguration> {

    public RunConfigurationProducer() {
        super(RunConfigurationType.getInstance().getConfigurationFactories()[0]);
    }

    public static boolean isExecutableFile(VirtualFile virtualFile) {

        for (String ext : OoRexxFileTypeFactory.fileExtensions) {
            if (virtualFile.getPath().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean setupConfigurationFromContext(RunConfiguration runConfiguration, ConfigurationContext configurationContext, Ref<PsiElement> ref) {
        VirtualFile file = findFile(configurationContext);
        if (file != null && isExecutableFile(file)) {
            runConfiguration.setScriptFile(file.getCanonicalPath());
            runConfiguration.setGeneratedName();

            PsiElement shebang = PsiTreeUtil.findChildOfType(ref.get(), OoRexxShebang.class);
            if (shebang != null) {
                runConfiguration.setExecutablePath(shebang.getText().substring(2));
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(RunConfiguration runConfiguration, ConfigurationContext configurationContext) {
        VirtualFile file = findFile(configurationContext);
        VirtualFile runConfigurationScriptFile = VfsUtil.findFileByIoFile(new File(runConfiguration.getScriptFile()), true);
        return file != null && Comparing.equal(runConfigurationScriptFile, file);
    }

    public VirtualFile findFile(ConfigurationContext configurationContext) {
        Location location = configurationContext.getLocation();
        return location == null ? null : location.getVirtualFile();
    }


}
