package com.github.harbby.gradle.plugins.serviceloader.extensions;

import java.util.ArrayList;
import java.util.List;

public class ServiceLoaderExtension
{
    public final static String NAME = "serviceLoader";

    private final List<String> serviceInterfaces = new ArrayList<>();

    public ServiceLoaderExtension serviceInterface(String... serviceInterfaces)
    {
        for (String it : serviceInterfaces) {
            this.serviceInterfaces.add(it);
        }
        return this;
    }

    public List<String> getServiceInterfaces()
    {
        return serviceInterfaces;
    }
}
