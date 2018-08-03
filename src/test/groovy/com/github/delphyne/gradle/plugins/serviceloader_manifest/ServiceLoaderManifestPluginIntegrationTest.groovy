package com.github.delphyne.gradle.plugins.serviceloader_manifest

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import test.TestImpl
import test.TestInterface

import java.util.jar.JarFile
import java.util.zip.ZipEntry

class ServiceLoaderManifestPluginIntegrationTest {

	File projectDir
	GradleRunner runner

	@BeforeMethod
	void setup() {
		List<File> pluginClasspath = findResource('plugin-classpath.txt').readLines().collect { new File(it) }
		projectDir = File.createTempDir()
		runner = GradleRunner.create().withProjectDir(projectDir).withPluginClasspath(pluginClasspath)
	}

	@Test
	void testFoo() {
		def name = UUID.randomUUID().toString().replaceAll('-', '')
		def group = 'com.github.delphyne'
		def version = '0.0.1'

		new File(projectDir, 'build.gradle').text = """
			plugins {
				id 'com.github.delphyne.service-loader-manifest'
			}
			serviceLoader {
				serviceInterface '${TestInterface.canonicalName}'
			}
			group = '$group'
			version = '$version'
			""".stripIndent()
		new File(projectDir, 'settings.gradle').text = "rootProject.name='$name'"

		copyResource('src/main/java', 'test/TestInterface.java')
		copyResource('src/main/java', 'test/TestImpl.java')

		BuildResult result = null
		System.out.withWriter { out ->
			System.err.withWriter { err ->
				result = runner
						.withDebug(true)
						.forwardStdOutput(out)
						.forwardStdError(err)
						.withArguments('--info', '--stacktrace', 'build')
						.build()
			}
		}

		assert result.task(':generateServiceLoaderManifest')?.outcome == TaskOutcome.SUCCESS
		assert new File(projectDir, "build/resources/main/META-INF/services/${TestInterface.canonicalName}").exists()
		File jar = new File(projectDir, "build/libs/${name}-${version}.jar")
		assert jar.exists()
		JarFile jarFile = new JarFile(jar)
		ZipEntry entry = jarFile.getEntry("META-INF/services/${TestInterface.canonicalName}")
		assert entry != null
		assert jarFile.getInputStream(entry).text == "${TestImpl.canonicalName}\n"
	}

	URL findResource(String resource) {
		def url = getClass().classLoader.findResource(resource)
		if (!url) {
			throw new FileNotFoundException(resource)
		}
		url
	}

	File copyResource(String sourceRoot, String resourcePath) {
		new File(new File(projectDir, sourceRoot), resourcePath).with {
			parentFile.mkdirs()
			text = findResource(resourcePath).text
			it
		}
	}
}
