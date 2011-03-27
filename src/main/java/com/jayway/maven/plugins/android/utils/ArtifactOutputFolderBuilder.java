package com.jayway.maven.plugins.android.utils;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

public class ArtifactOutputFolderBuilder {

	private final MavenProject mavenProject;

	private final String dependenciesDirectory;

	public ArtifactOutputFolderBuilder(MavenProject mavenProject, String dependenciesDirectory) {
		this.mavenProject = mavenProject;
		this.dependenciesDirectory = dependenciesDirectory;
	}

	public File generateOutputFolder(Artifact artifact) {
		return new File(mavenProject.getBuild().getDirectory(), //
				dependenciesDirectory + File.separator + artifact.getArtifactId() + File.separator + //
						artifact.getVersion() + File.separator + "classes");
	}

}