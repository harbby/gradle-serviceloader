[![Build Status](https://travis-ci.org/harbby/gradle-serviceloader.svg?branch=master)](https://travis-ci.org/harbby/gradle-serviceloader)

# gradle-serviceloader
_Generate java.util.ServiceLoader manifests for your projects_


This plugin is used to generate the ```META-INF/services/``` manifest files used by the [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
mechanism built into Java 6 (and higher).  Using the service loader, you can specify a particular interface you want to load, and all implementations
which have an appropriate manifest on the classpath will be found an available to your application.  This is particularly useful for SPI and Plugin
architectures.

## Installation

### Within a standalone build.gradle
```groovy
apply plugin: 'com.github.harbby.gradle.serviceloader'

buildscript {
	repositories {
		maven { url 'https://harbby.github.io/.m2/repository/' }
	}
	dependencies {
		classpath 'com.github.harbby:gradle-serviceloader:1.1.1'
	}
}
```

## Usage

Add a `serviceLoader` block to your build.gradle.

```groovy
serviceLoader {
    serviceInterface 'ideal.sylph.spi.Runner'
}
```

Multiple interfaces may be provided to generate a manifest for each provided interface.

```groovy
serviceLoader {
    serviceInterface 'ideal.sylph.spi.Runner'
    serviceInterface 'ideal.sylph.api.PipelinePlugin'
}
```

A GDSL file has been provided which will provide code completion within editors that support it.   