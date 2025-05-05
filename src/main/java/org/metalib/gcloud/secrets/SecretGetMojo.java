package org.metalib.gcloud.secrets;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.metalib.gcloud.secrets.helpers.GitIgnoreUpdater;
import org.metalib.gcloud.secrets.helpers.SecretManager;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.metalib.gcloud.secrets.helpers.Finals.SECRETS_DIR;
import static org.metalib.gcloud.secrets.helpers.Finals.SECRET_GET;

/**
 * Sets the secrets in the gcloud project.
 */
@Mojo(name = SECRET_GET, defaultPhase = LifecyclePhase.NONE)
public class SecretGetMojo extends AbstractMojo {

    @Parameter(property = "gcloud.secrets.project", required = true)
    String secretProject;

    @Parameter(property = "gcloud.secrets.dir", required = true, defaultValue = "${project.basedir}/" + SECRETS_DIR)
    File secretDir;

    @Parameter(property = "gcloud.secrets.names", required = true)
    List<String> secrets;

    @Parameter( defaultValue = "${project}", readonly = true)
    MavenProject mavenProject;

    public void execute() throws MojoExecutionException {
        try {
            if (null == secrets || secrets.isEmpty()) {
                getLog().info("No secrets to retrieve. Skipping ...");
            }
            if (!secretDir.exists() && !secretDir.mkdirs()) {
                throw new MojoExecutionException("Failed to create secret directory: " + secretDir.getAbsolutePath());
            }
            getLog().info("Retrieving secrets from project: " + secretProject);
            for (final var secret : SecretManager.retrieveSecret(secretProject, secrets.toArray(new String[0])).entrySet()) {
                final var secretFile = new File(secretDir, secret.getKey());
                Files.write(secretFile.toPath(), secret.getValue().getBytes());
                getLog().info("Secret: " + secret.getKey() + " retrieved: " + secretFile.getAbsolutePath());
            }
            if (GitIgnoreUpdater.update(mavenProject.getBasedir(), secretDir)) {
                getLog().info("Updated .gitignore file to include secret directory");
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }
    }
}
