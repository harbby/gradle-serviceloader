package com.github.harbby.gradle.plugins.serviceloader;

import com.github.harbby.gradle.plugins.serviceloader.extensions.ServiceLoaderExtension;
import com.github.harbby.gradle.plugins.serviceloader.tasks.ServiceLoaderTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class ServiceLoaderPlugin
        implements Plugin<Project>
{
    @Override
    public void apply(@Nonnull Project project)
    {
        ServiceLoaderExtension extension = project.getExtensions()
                .create(ServiceLoaderExtension.NAME, ServiceLoaderExtension.class);
        project.getPlugins().apply(JavaPlugin.class);
        ServiceLoaderTask task = project.getTasks().create("serviceLoaderBuild", ServiceLoaderTask.class);
        task.dependsOn(project.getTasks().findByName("classes"));
        requireNonNull(project.getTasks().findByName("jar"), "project task jar not find").dependsOn(task);
        project.afterEvaluate((tmp) -> {
            task.setServiceInterfaces(extension.getServiceInterfaces());
        });
    }
}
