/*
 * Copyright (C) 2009 Jayway AB
 * Copyright (C) 2007-2008 JVending Masa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jayway.maven.plugins.android.phase04processclasses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;

import com.jayway.maven.plugins.android.AbstractAndroidMojo;
import com.jayway.maven.plugins.android.CommandExecutor;
import com.jayway.maven.plugins.android.utils.ArtifactOutputFolderBuilder;

/**
 * unpack library.
 * 
 * @author hugo.josefson@jayway.com
 * @goal unpack
 * @phase process-classes
 * @requiresDependencyResolution compile
 */
public class UnpackMojo extends AbstractAndroidMojo {
	
	private ArtifactOutputFolderBuilder artifactOutputFolderBuilder;

	public void execute() throws MojoExecutionException, MojoFailureException {
		CommandExecutor executor = CommandExecutor.Factory
				.createDefaultCommmandExecutor();
		executor.setLogger(this.getLog());
		
//		if (getLog().isInfoEnabled()) 
//			getLog().info("project artifact: " + project.getArtifact().getFile().getAbsolutePath());

		artifactOutputFolderBuilder = new ArtifactOutputFolderBuilder(project, "dependencies");
		
		if (generateApk) {
			// Unpack all dependent and main classes
			unpackClasses();
		}
	}
	
	private void unpackClasses() throws MojoExecutionException {
		Set<Artifact> artifacts = getRelevantCompileArtifacts();
		
		for (Artifact artifact : artifacts) 
			unpackArtifact(artifact);
		
		unpackArtifact(project.getArtifact());
	}

	protected void unpackArtifact(Artifact artifact) throws MojoExecutionException {
		File outputDirectory = artifactOutputFolderBuilder.generateOutputFolder(artifact);
		
		if (!artifact.isSnapshot()  && outputDirectory.exists()) {
			if (getLog().isInfoEnabled()) 
				getLog().info(artifact.getArtifactId() + " already unpacked in " + outputDirectory.getAbsolutePath());
			return;
		}
		
		if (getLog().isInfoEnabled()) 
			getLog().info("unpacking " + artifact.getArtifactId() + " to " + outputDirectory.getAbsolutePath());
		
		// it will always unpack for SNAPSHOT artifacts, but it will try to reuse already unpacked folders for other dependencies. 

		if (artifact.getFile().isDirectory()) {
			try {
				FileUtils
						.copyDirectory(artifact.getFile(), outputDirectory);
			} catch (IOException e) {
				throw new MojoExecutionException(
						"IOException while copying "
								+ artifact.getFile().getAbsolutePath()
								+ " into "
								+ outputDirectory.getAbsolutePath(), e);
			}
		} else {
			try {
				unjar(new JarFile(artifact.getFile()), outputDirectory);
			} catch (IOException e) {
				throw new MojoExecutionException(
						"IOException while unjarring "
								+ artifact.getFile().getAbsolutePath()
								+ " into "
								+ outputDirectory.getAbsolutePath(), e);
			}
		}
	}

	private void unjar(JarFile jarFile, File outputDirectory)
			throws IOException {
		for (Enumeration en = jarFile.entries(); en.hasMoreElements();) {
			JarEntry entry = (JarEntry) en.nextElement();
			File entryFile = new File(outputDirectory, entry.getName());
			if (!entryFile.getParentFile().exists()
					&& !entry.getName().startsWith("META-INF")) {
				entryFile.getParentFile().mkdirs();
			}
			if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
				final InputStream in = jarFile.getInputStream(entry);
				try {
					final OutputStream out = new FileOutputStream(entryFile);
					try {
						IOUtil.copy(in, out);
					} finally {
						IOUtils.closeQuietly(out);
					}
				} finally {
					IOUtils.closeQuietly(in);
				}
			}
		}
	}
}
