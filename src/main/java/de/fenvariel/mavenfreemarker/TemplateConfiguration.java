/*
 * Copyright 2015 AlexS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fenvariel.mavenfreemarker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TemplateConfiguration {

    private String ftlTemplate = null;

    private String targetExtension = "";

    private File outputDir;

    private String[] editableSectionNames = new String[0];

    private SourceBundle[] sourceBundles = new SourceBundle[0];

    private Version version;

    public String getFtlTemplate() {
        return ftlTemplate;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public String getTargetExtension() {
        return targetExtension;
    }

    public void setTargetExtension(String targetExtension) {
        if (targetExtension == null || targetExtension.trim().isEmpty()) {
            this.targetExtension = "";
        } else {
            targetExtension = targetExtension.trim();
            if (targetExtension.charAt(0) != '.') {
                targetExtension = "." + targetExtension;
            }
            this.targetExtension = targetExtension;
        }

    }

    public Map<String, Pattern> getEditableSectionNames() {
        Map<String, Pattern> result = new HashMap<String, Pattern>();
        for (String keepSectionName : editableSectionNames) {
            result.put(keepSectionName, compilePattern(keepSectionName));
        }
        return result;
    }

    public SourceBundle[] getSourceBundles() {
        return sourceBundles;
    }

    public void setFtlTemplate(String ftlTemplate) {
        this.ftlTemplate = ftlTemplate;
    }

    public void setEditableSectionNames(String[] editableSectionNames) {
        this.editableSectionNames = editableSectionNames;
    }

    public void setSourceBundles(SourceBundle[] sourceBundles) {
        this.sourceBundles = sourceBundles;
    }

    private Pattern compilePattern(String sectionName) {
        int flags = Pattern.DOTALL | Pattern.MULTILINE;
        return Pattern.compile(".*^\\s*?//\\s*?EDITABLE SECTION " + sectionName + ".*?\n(.*?)^\\s*// EDITABLE SECTION " + sectionName
                + " END.*?\n", flags);
    }
}
