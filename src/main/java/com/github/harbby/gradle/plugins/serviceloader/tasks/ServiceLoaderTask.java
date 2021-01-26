package com.github.harbby.gradle.plugins.serviceloader.tasks;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ServiceLoaderTask
        extends AbstractTask
{
    private final Logger logger = super.getLogger();
    private final Project project = super.getProject();

    @Input
    @SkipWhenEmpty
    private List<String> serviceInterfaces;

    @InputDirectory
    @SkipWhenEmpty
    private final FileCollection classesOutput;

    @OutputDirectory
    private final File outputDirectory;

    private final JavaPluginConvention javaConvention;
    private final SourceSet main;

    public void setServiceInterfaces(List<String> serviceInterfaces)
    {
        this.serviceInterfaces = serviceInterfaces;
    }

    public ServiceLoaderTask()
            throws IOException
    {
        super.setDescription("Generate META-INF/services manifests for use with ServiceLoaders");
        super.setGroup("Source Generation");

        javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        this.main = javaConvention.getSourceSets().findByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSetOutput mainOutput = main.getOutput();
        classesOutput = mainOutput.getClassesDirs();
        outputDirectory = new File(mainOutput.getResourcesDir(), "META-INF/services");
    }

    private List<String> getClassNames(FileCollection classesOutput)
    {
        Set<String> classesDirs = classesOutput.getFiles().stream().map(File::getPath).collect(Collectors.toSet());
        List<String> classNames = classesOutput.getAsFileTree().getFiles().stream()
                .filter(it -> it.isFile() && it.getName().endsWith(".class"))
                .map(it -> {
                    for (String dirPath : classesDirs) {
                        if (it.getPath().startsWith(dirPath)) {
                            String classString = it.getPath().substring(dirPath.length() + 1)
                                    .replace(".class", "")
                                    .replace(File.separator, ".");  //
                            return classString;
                        }
                    }
                    //TODO: this is error file ...
                    logger.error("class[{}] not startsWith in {}", it, classesDirs);
                    return it.getPath();
                })
                .collect(Collectors.toList());
        return classNames;
    }

    @TaskAction
    public void run()
            throws Exception
    {
        super.getProject().mkdir(outputDirectory);
        List<URL> classpath = new ArrayList<>();
        for (File file : main.getRuntimeClasspath().getFiles()) {
            classpath.add(file.toURI().toURL());
        }
        for (File file : main.getCompileClasspath().getFiles()) {
            classpath.add(file.toURI().toURL());
        }
        logger.debug("{} deps: {}", project.getName(), classpath);

        try (URLClassLoader classloader = URLClassLoader.newInstance(classpath.toArray(new URL[classpath.size()]))) {
            for (String serviceInterface : serviceInterfaces) {
                logger.debug("Search for {} in {}", serviceInterface, classpath);
                Class<?> serviceClass = classloader.loadClass(serviceInterface);
                logger.debug("Found {}", serviceClass);

                List<String> classNames = getClassNames(classesOutput);
                logger.debug("Will consider {}", classNames);

                List<Class<?>> classes = new ArrayList<>(classNames.size());
                for (String it : classNames) {
                    classes.add(classloader.loadClass(it));
                }

                logger.debug("Successfully loaded {}", classes);

                List<Class<?>> implementations = classes.stream()
                        .filter(it -> !(it.isInterface() || Modifier.isAbstract(it.getModifiers())) && Modifier.isPublic(it.getModifiers()) && serviceClass.isAssignableFrom(it))
                        .filter(it -> it.getCanonicalName() != null)  //Scala exists in an empty condition
                        .collect(Collectors.toList());

                logger.warn("Found {} implementations of {}: {}", implementations.size(), serviceInterface, implementations);

                if (implementations.isEmpty()) {
                    logger.warn("No implementations found for {}", serviceInterface);
                    return;
                }

                File manifest = new File(outputDirectory, serviceInterface);
                try (BufferedWriter out = Files.newBufferedWriter(manifest.toPath(), UTF_8)) {
                    for (Class<?> it : implementations) {
                        out.write(it.getCanonicalName());
                        out.newLine();
                    }
                }
                logger.info("Generated manifest at {}", manifest.getPath());
            }
        }
    }
}
