package com.github.delphyne.gradle.plugins.serviceloader_manifest

import org.gradle.api.internal.project.AbstractProject
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test
import test.TestInterface

class ServiceLoaderManifestPluginTest {

	@Test
	void testApply() {
		AbstractProject project = (AbstractProject) ProjectBuilder.builder().build()
		project.plugins.apply(ServiceLoaderManifestPlugin)
		project.serviceLoader {
			serviceInterface TestInterface.canonicalName
		}
		project.evaluate()
		assert project.serviceLoader.serviceInterfaces == [TestInterface.canonicalName]
		assert project.tasks.findByName('generateServiceLoaderManifest').with {
			assert it.dependsOn(project.tasks.findByName('classes'))
			assert project.tasks.findByName('jar').dependsOn(it)
			it
		}
	}
}
