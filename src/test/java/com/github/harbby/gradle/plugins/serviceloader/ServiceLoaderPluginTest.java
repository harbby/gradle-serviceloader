package com.github.harbby.gradle.plugins.serviceloader;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ServiceLoaderPluginTest {
    private File projectDir;
    private GradleRunner runner;

    @Before
    public void setup() throws IOException {
        projectDir = Files.createTempDirectory("gradle-").toFile();
        runner = GradleRunner.create().withProjectDir(projectDir).withPluginClasspath();
    }

    @Test
    public void serviceloaderTest() throws IOException {
        var name = UUID.randomUUID().toString().replaceAll("-", "");
        var group = "com.github.harbby";
        var version = "1.1.8";
        var pluginInterface = "test.TestInterface";
        Files.writeString(new File(projectDir, "build.gradle").toPath(), """
                plugins {
                	id 'com.github.harbby.gradle.serviceloader'
                }
                serviceLoader {
                	serviceInterface '%s'
                }
                group = '%s'
                version = '%s'
                """.stripIndent().formatted(pluginInterface, group, version));

        Files.writeString(new File(projectDir, "settings.gradle").toPath(), "rootProject.name='%s'".formatted(name));

        copyResource("src/main/java", "test/TestInterface.java");
        copyResource("src/main/java", "test/TestImpl.java");

        BuildResult result = runner.withDebug(true)
                .forwardStdOutput(new OutputStreamWriter(System.out))
                .forwardStdError(new OutputStreamWriter(System.err))
                .withArguments("--info", "--stacktrace", "build")
                .build();
        Assert.assertSame(result.task(":serviceLoaderBuild").getOutcome(), TaskOutcome.SUCCESS);
        Assert.assertTrue(new File(projectDir, "build/resources/main/META-INF/services/%s".formatted(pluginInterface)).exists());

        File jar = new File(projectDir, "build/libs/%s-%s.jar".formatted(name, version));
        Assert.assertTrue(jar.exists());
        JarFile jarFile = new JarFile(jar);
        ZipEntry entry = jarFile.getEntry("META-INF/services/%s".formatted(pluginInterface));
        Assert.assertEquals("test.TestImpl\n", new String(jarFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8));
    }


    private URL findResource(String resource) throws FileNotFoundException {
        URL url = getClass().getClassLoader().getResource(resource);
        if (url == null) {
            throw new FileNotFoundException(resource);
        }
        return url;
    }

    private File copyResource(String sourceRoot, String resourcePath) throws IOException {
        File dir = new File(projectDir, sourceRoot);
        dir.mkdirs();
        var sourceFile = new File(findResource(resourcePath).getFile());
        File writeFile = new File(dir, sourceFile.getName());

        Files.copy(sourceFile.toPath(), writeFile.toPath());
        return writeFile;
    }
}