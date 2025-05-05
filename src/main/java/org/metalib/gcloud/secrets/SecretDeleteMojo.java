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
import java.util.List;

import static org.metalib.gcloud.secrets.helpers.Finals.SECRETS_DIR;
import static org.metalib.gcloud.secrets.helpers.Finals.SECRET_DELETE;

/**
 * Sets the secrets in the gcloud project.
 */
@Mojo(name = SECRET_DELETE, defaultPhase = LifecyclePhase.NONE)
public class SecretDeleteMojo extends AbstractMojo {

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
                getLog().info("No secrets to set. Skipping ...");
            }
            getLog().info("Deleting secrets from project: " + secretProject);
            for (final var secret : SecretManager.deleteSecretsIfExists(secretProject, secrets.toArray(new String[0])).entrySet()) {
                getLog().info("Secret: <" + secret.getKey() +
                        (Boolean.TRUE.equals(secret.getValue()) ? "> deleted." : "> not found."));
            }
            if (GitIgnoreUpdater.update(mavenProject.getBasedir(), secretDir)) {
                getLog().info("Updated .gitignore file to include secret directory");
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }
    }
}
