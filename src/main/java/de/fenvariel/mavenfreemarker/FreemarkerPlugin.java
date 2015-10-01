package de.fenvariel.mavenfreemarker;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "process-ftl")
public class FreemarkerPlugin
        extends AbstractMojo {

    @Parameter
    private File templateDir;

    @Parameter(defaultValue = "${basedir}", readonly = true)
    private File baseDir;

    @Parameter
    private TemplateConfiguration[] templateConfigurations = new TemplateConfiguration[0];

    private Configuration getFreemarker(Version version) throws MojoExecutionException {
        Configuration configuration = new Configuration(version.freemarkerVersion);
        try {
            configuration.setTemplateLoader(new FileTemplateLoader(templateDir));
        } catch (IOException ex) {
            throw new MojoExecutionException("failed to initialize freemarker", ex);
        }
        return configuration;
    }

    public void execute()
            throws MojoExecutionException {

        for (TemplateConfiguration config : templateConfigurations) {
            generate(config);
        }
    }

    private String getNameWithoutExtension(File file) {
        if (file.isDirectory()) {
            return file.getName();
        }
        String filename = file.getName();
        int idx = filename.lastIndexOf('.');
        if (idx > 0) {
            return filename.substring(0, idx);
        } else {
            return filename;
        }
    }

    public Map<String, Object> getConfig(TemplateConfiguration config) {
        Map<String, Object> configMap = new HashMap<String, Object>();
        configMap.put("editableSectionNames", config.getEditableSectionNames().keySet());
        configMap.put("ftlTemplate", config.getFtlTemplate());
        configMap.put("outputDir", config.getOutputDir().toString());
        configMap.put("prefix", config.getPrefix());
        configMap.put("suffix", config.getSuffix());
        configMap.put("targetExtension", config.getTargetExtension());
        configMap.put("version", config.getVersion());
        return configMap;
    }

    private void generate(TemplateConfiguration config) throws MojoExecutionException {
        Template template;
        try {
            template = getFreemarker(config.getVersion()).getTemplate(config.getFtlTemplate(), "UTF-8");
        } catch (Exception ex) {
            throw new MojoExecutionException("error reading template-file " + config.getFtlTemplate(), ex);
        }
        File outputDir = config.getOutputDir();
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        for (SourceBundle source : config.getSourceBundles()) {
            Collection<File> sourceFiles;
            try {
                sourceFiles = source.getSourceFiles(baseDir);
            } catch (IOException ex) {
                throw new MojoExecutionException("error reading source files", ex);
            }
            if (sourceFiles == null || sourceFiles.isEmpty()) {
                throw new MojoExecutionException("no source files found for bundle");
            }
            for (File sourceFile : sourceFiles) {
                String destinationFilename = config.getPrefix() + getNameWithoutExtension(sourceFile) + config.getSuffix();
                Path destinationFilePath = Paths.get(outputDir.getAbsolutePath(), destinationFilename + config.getTargetExtension());
                File destinationFile = destinationFilePath.toFile();
                Map<String, Object> root = new HashMap<String, Object>();
                root.put("data", readJson(sourceFile));
                root.put("additionalData", source.getAdditionalData());
                Map<String, Object> configMap = getConfig(config);
                configMap.put("destinationFilePath", destinationFilePath);
                configMap.put("destinationFilename", destinationFilename);
                root.put("config", configMap);
                generate(template, destinationFile, root, config.getEditableSectionNames());
            }
        }
    }

    private Map<String, Object> readJson(File source) throws MojoExecutionException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(source, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException ex) {
            throw new MojoExecutionException("error reading source-file " + source.getAbsolutePath(), ex);
        }
    }

    private void generate(Template template, File file, Map<String, Object> data, Map<String, Pattern> keepPatterns) throws MojoExecutionException {
        try {

            file.getParentFile().mkdirs();

            includeKeepSections(file, keepPatterns, data);

            Writer writer = new FileWriter(file);
            try {
                template.process(data, writer);
                writer.flush();
                System.out.println("Written " + file.getCanonicalPath());
            } finally {
                writer.close();
            }
        } catch (Exception ex) {
            throw new MojoExecutionException("error generating file: " + file.getAbsolutePath() + " from template " + template.getName() + " and source " + data, ex);
        }
    }

    private void includeKeepSections(File file, Map<String, Pattern> keepPatterns, Map<String, Object> source) throws MojoExecutionException {
        Map<String, Object> editables = new HashMap<String, Object>();
        source.put("editable", editables);
        if (keepPatterns != null && file.exists() && !keepPatterns.isEmpty()) {
            try {
                String contents = readFile(file);

                Matcher matcher;

                for (Entry<String, Pattern> section : keepPatterns.entrySet()) {
                    matcher = section.getValue().matcher(contents);
                    if (matcher.matches()) {
                        editables.put(section.getKey(), matcher.group(1));
                    }
                }
            } catch (IOException ex) {
                throw new MojoExecutionException("error parsing file for keep-sections: " + file.getAbsolutePath(), ex);
            }
        }
    }

    private String readFile(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(file.toURI()));
        return new String(bytes);
    }
}
