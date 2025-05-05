package org.metalib.gcloud.secrets.helpers;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for interacting with Google Cloud Secret Manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecretManager {

    /**
     * Deletes secrets from Google Cloud Secret Manager if they exist.
     * @param projectId The ID of the Google Cloud project.
     * @param secretIds The IDs of the secrets to delete.
     * @return A map of secret IDs to booleans indicating whether the secret was successfully deleted.
     */
    public static Map<String,Boolean> deleteSecretsIfExists(String projectId, String... secretIds) {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            final var result = new LinkedHashMap<String,Boolean>();
            for (String secretId : secretIds) {
                result.put(secretId, deleteSecretIfExists(client, projectId, secretId));
            }
            return result;
        } catch (Exception e) {
            // Handle exceptions related to SecretManagerServiceClient creation
            throw new SecretException(e);
        }
    }

    /**
     * Deletes a secret from Google Cloud Secret Manager if it exists.
     * @param client The secret manager client
     * @param projectId The ID of the Google Cloud project.
     * @param secretId The ID of the secret to delete.
     * @return A boolean indicating whether the secret was successfully deleted.
     */
    public static boolean deleteSecretIfExists(SecretManagerServiceClient client, String projectId, String secretId) {
        try {
            final var secretName = SecretName.of(projectId, secretId);
            // Check if the secret exists by trying to access it
            client.getSecret(secretName);
            // If no exception was thrown, the secret exists, and we can delete it
            client.deleteSecret(secretName);
            return true; // Secret was successfully deleted
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * Upsert secrets in Google Cloud Secret Manager.
     * @param projectId The ID of the Google Cloud project.
     * @param secrets The ID,Value map of the secrets to upsert.
     * @return The secret name, version map that was upserted.
     */
    public static Map<SecretName,SecretVersion> upsertSecrets(String projectId, Map<String, String> secrets) {
        try (final var client = SecretManagerServiceClient.create()) {
            final var result = new LinkedHashMap<SecretName,SecretVersion>();
            for (var entry : secrets.entrySet()) {
                final var secretName = checkOrCreateSecret(client, projectId, entry.getKey());
                result.put(secretName, client.addSecretVersion(secretName, SecretPayload.newBuilder()
                        .setData(ByteString.copyFromUtf8(entry.getValue())).build()));
            }
            return result;
        } catch (Exception e) {
            throw new SecretException(e);
        }
    }

    /**
     * Check if the secret exists in the project, if not create it.
     * @param client The secret manager client
     * @param projectId The project id
     * @param secretId The secret id
     * @return The secret name
     */
    static SecretName checkOrCreateSecret(SecretManagerServiceClient client, String projectId, String secretId) {
        final var secretName = SecretName.of(projectId, secretId);
        try {
            // Try to access the secret to check if it exists
            client.getSecret(secretName);
        } catch (Exception e) {
            // If the secret does not exist, create it
            client.createSecret(ProjectName.of(projectId), secretId, Secret.newBuilder()
                    .setReplication(Replication.newBuilder().setAutomatic(Replication.Automatic.newBuilder().build()).build())
                    .build());
        }
        return secretName;
    }

    /**
     * Retrieves a secret value from Google Cloud Secret Manager.
     *
     * @param projectId The ID of the Google Cloud project.
     * @param secretIds The IDs of the secrets to retrieve.
     * @return The secret value as a string.
     */
    public static Map<String,String> retrieveSecret(String projectId, String... secretIds) {
        // Initialize the client and connect to the service
        try (final var client = SecretManagerServiceClient.create()) {
            final var result = new LinkedHashMap<String,String>();
            for (String secretId : secretIds) {
                set(result, client, projectId, secretId);
            }
            return result;
        } catch (Exception e) {
            throw new SecretException(e);
        }
    }

    static void set(Map<String,String> result, SecretManagerServiceClient client, String projectId, String secretId) {
        try {
            result.put(secretId, client.accessSecretVersion(SecretVersionName.of(projectId, secretId, "latest"))
                    .getPayload().getData().toStringUtf8());
        } catch (NotFoundException e) {
            result.put(secretId, null);
        }
    }

    /**
     * Exception thrown when an error occurs while interacting with the secret manager.
     */
    public static class SecretException extends RuntimeException {
        public SecretException(Throwable throwable) {
            super(throwable);
        }
    }
}
