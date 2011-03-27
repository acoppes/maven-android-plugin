package com.jayway.maven.plugins.android.utils;

import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class ArtifactOutputFolderBuilderTest {

	Mockery mockery = new Mockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};
	
	String tmpFolder = System.getProperty("java.io.tmpdir");

	@Test
	public void shouldCreateOutputFolderForArtifactOnTargetFolder() {

		final MavenProject mavenProject = mockery.mock(MavenProject.class);
		final Artifact artifact = mockery.mock(Artifact.class);
		final Build build = mockery.mock(Build.class);
		
		mockery.checking(new Expectations() {
			{
				oneOf(artifact).getArtifactId();
				will(returnValue("artifact"));

				oneOf(artifact).getVersion();
				will(returnValue("0.0.1"));

				oneOf(mavenProject).getBuild();
				will(returnValue(build));
				
				oneOf(build).getDirectory();
				will(returnValue(tmpFolder + "/target"));
			}
		});
		
		ArtifactOutputFolderBuilder builder = new ArtifactOutputFolderBuilder(mavenProject, "dependencies");
		
		File artifactOutputFolder = builder.generateOutputFolder(artifact);
		
		assertThat(artifactOutputFolder, IsNull.notNullValue());
		assertThat(artifactOutputFolder.getAbsolutePath(), IsEqual.equalTo(tmpFolder + "/target/dependencies/artifact/0.0.1/classes"));
		
	}
	
}
