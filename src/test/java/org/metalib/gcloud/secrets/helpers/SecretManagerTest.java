package org.metalib.gcloud.secrets.helpers;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * To run this test the following preconditions must be met:<br>
 * First, you need to authenticate with gcloud:<br>
 * <pre>    gcloud auth application-default login</pre>
 * Second, you need provide a project ID in the following environment variable:<br>
 * <pre>    export GOOGLE_CLOUD_PROJECT=your-project-id</pre>
 */
public class SecretManagerTest {

    static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    static final String SECRET_ID = "gcloud-secret-manager-maven-plugin-test";
    static final String SECRET_VALUE = "secret value text.";

    @Test
    @Ignore("Manual run only")
    public void test() {
        final var created = SecretManager.upsertSecrets(PROJECT_ID, Map.of(SECRET_ID, SECRET_VALUE));
        assertNotNull(created);
        final var retrieved = SecretManager.retrieveSecret(PROJECT_ID, SECRET_ID);
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());
        assertEquals(SECRET_VALUE, retrieved.get(SECRET_ID));
        final var deleted = SecretManager.deleteSecretsIfExists(PROJECT_ID, SECRET_ID);
        assertNotNull(deleted);
        assertEquals(1, deleted.size());
        assertTrue(deleted.get(SECRET_ID));
        final var retrieveDeleted = SecretManager.retrieveSecret(PROJECT_ID, SECRET_ID);
        assertNotNull(retrieveDeleted);
        assertEquals(1, retrieveDeleted.size());
        assertNull(retrieveDeleted.get(SECRET_ID));
    }
}