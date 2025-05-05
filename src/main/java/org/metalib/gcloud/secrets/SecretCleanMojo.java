package org.metalib.gcloud.secrets;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.metalib.gcloud.secrets.helpers.GitIgnoreUpdater;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.metalib.gcloud.secrets.helpers.Finals.SECRETS_DIR;
import static org.metalib.gcloud.secrets.helpers.Finals.SECRET_CLEAN;

/**
 * Sets the secrets in the gcloud project.
 */
@Mojo(name = SECRET_CLEAN, defaultPhase = LifecyclePhase.NONE)
public class SecretCleanMojo extends AbstractMojo {

    @Parameter(property = "gcloud.secrets.dir", required = true, defaultValue = "${project.basedir}/" + SECRETS_DIR)
    File secretDir;

    @Parameter(property = "gcloud.secrets.names", required = true)
    List<String> secrets;

    @Parameter( defaultValue = "${project}", readonly = true)
    MavenProject mavenProject;

    public void execute() throws MojoExecutionException {
        try {
            if (null == secrets || secrets.isEmpty()) {
                getLog().info("No secrets to set. Skipping ...");
            }
            if (!secretDir.exists() && !secretDir.mkdirs()) {
                throw new MojoExecutionException("Failed to create secret directory: " + secretDir.getAbsolutePath());
            }
            for (final var secretId : secrets) {
                final var secretFile = new File(secretDir, secretId);
                if (!secretFile.exists()) {
                    throw new MojoExecutionException("secret file not found: " + secretFile.getAbsolutePath());
                }
            }
            getLog().info("Clear secrets in: " + secretDir.getAbsolutePath());
            for (final var secret : secrets) {
                final var secretFile = new File(secretDir, secret);
                if (Files.deleteIfExists(secretFile.toPath())) {
                    getLog().info("Secret file " + secretFile.getAbsolutePath() + " deleted.");
                }
            }
            if (GitIgnoreUpdater.update(mavenProject.getBasedir(), secretDir)) {
                getLog().info("Updated .gitignore file to include secret directory");
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }
    }
}
