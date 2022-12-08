package org.oorexx.ide.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.Nullable;
import org.oorexx.ide.util.ExecutorParserUtil;

import java.util.stream.Stream;

@State(
        name = "OoRexxLanguageSettings",
        storages = {
                @Storage("ooRexx.xml"),
        }
)
public class OoRexxLanguageSettings implements PersistentStateComponent<OoRexxLanguageSettings> {

    private boolean allowSpecialChars = false;
    private boolean enableExecutorFeature = false;
    private AutoCompletionCasing autoCompletionCasing;


    public enum AutoCompletionCasing {
        UPPER_CASE("Upper Case"),
        LOWER_CASE("Lower Case"),
        CAMEL_CASE("Camel Case"),
        CAPITALIZE("Capitalize");

        private String readableName;

        AutoCompletionCasing(String s) {
            this.readableName = s;
        }

        public String getReadableName() {
            return readableName;
        }

        public static String[] getReadableNames(){
            return Stream.of(AutoCompletionCasing.values())
                    .map(AutoCompletionCasing::getReadableName)
                    .toArray(String[]::new);
        }

        public static AutoCompletionCasing fromReadableName(String readableName) {
            for (AutoCompletionCasing value : AutoCompletionCasing.values()) {
                if(value.getReadableName().equals(readableName)) {
                    return value;
                }
            }

            throw new IllegalArgumentException(readableName);
        }
    }

    public static OoRexxLanguageSettings getInstance() {
        return ServiceManager.getService(OoRexxLanguageSettings.class);
    }

    @Nullable
    @Override
    public OoRexxLanguageSettings getState() {
        return this;
    }

    @Override
    public void loadState(OoRexxLanguageSettings ooRexxLanguageSettings) {
        this.allowSpecialChars = ooRexxLanguageSettings.allowSpecialChars;
        this.enableExecutorFeature = ooRexxLanguageSettings.enableExecutorFeature;
        this.autoCompletionCasing = ooRexxLanguageSettings.autoCompletionCasing;
    }

    public boolean isAllowSpecialChars() {
        return allowSpecialChars;
    }

    public void setAllowSpecialChars(boolean allowSpecialChars) {
        this.allowSpecialChars = allowSpecialChars;
    }

    public boolean isEnableExecutorFeature() {
        return enableExecutorFeature;
    }

    public void setEnableExecutorFeature(boolean enableExecutorFeature) {
        this.enableExecutorFeature = enableExecutorFeature;
        ExecutorParserUtil.active = enableExecutorFeature;
    }

    public AutoCompletionCasing getAutoCompletionCasing() {
        return autoCompletionCasing != null ? autoCompletionCasing : AutoCompletionCasing.CAMEL_CASE;
    }

    public void setAutoCompletionCasing(AutoCompletionCasing autoCompletionCasing) {
        this.autoCompletionCasing = autoCompletionCasing;
    }
}
