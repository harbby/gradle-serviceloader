[![Build Status](https://travis-ci.org/harbby/gradle-serviceloader.svg?branch=master)](https://travis-ci.org/harbby/gradle-serviceloader)

# gradle-serviceloader
java code  plugin example

+ add scala support
+ This plugin is used to generate the ```META-INF/services/``` files 
+ used by the [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
+ mechanism built into Java 6 (and higher). 

Using the service loader, you can specify a particular interface you want to load,and all implementations
which have an appropriate on the classpath will be found an available to your application.  This is particularly useful for SPI and Plugin
architectures.

## link
https://github.com/delphyne/gradle-serviceloader-manifest

## Installation

https://plugins.gradle.org/plugin/com.github.harbby.gradle.serviceloader

### build.gradle
Build script snippet for plugins DSL for Gradle 2.1 and later:
```groovy
plugins {
  id "com.github.harbby.gradle.serviceloader" version "1.1.8"
}
```
Build script snippet for use in older Gradle versions or where dynamic configuration is required:
```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.github.harbby:gradle-serviceloader:1.1.8"
  }
}

apply plugin: "com.github.harbby.gradle.serviceloader"
```

## Usage

Add a `serviceLoader` to your build.gradle.

```groovy
serviceLoader {
    serviceInterface 'ideal.sylph.spi.Runner'
}
```

Multiple interfaces may be provided to generate a manifest for each provided interface.

```groovy
serviceLoader {
    serviceInterface 'ideal.xx.xx.XXInterface'
    serviceInterface 'ideal.xx.xx.XXInterface'
}
```

## other
The following is for testing
```groovy
 apply plugin: 'com.github.harbby.gradle.serviceloader'
 
 buildscript {
 	repositories {
 		maven { url 'https://harbby.github.io/.m2/repository/' }
 	}
 	dependencies {
 		classpath 'com.github.harbby:gradle-serviceloader:1.1.8'
 	}
 }
 ```
