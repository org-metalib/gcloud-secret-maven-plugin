package org.metalib.gcloud.secrets.helpers;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.metalib.gcloud.secrets.helpers.Finals.GIT_DIR;

public class GitIgnoreUpdaterTest {

    static final String TEST_OUTPUT_DIR = "target/gitignore-test";
    static final String REPO_DIR = TEST_OUTPUT_DIR + "/repo";
    static final String SECRETS_DIR = REPO_DIR + "/.secrets";

    @BeforeClass
    public static void init() {
        new File(SECRETS_DIR).mkdirs();
    }

    @Test
    public void secretDirOutsideOfRepoDirShouldPass() throws IOException {
        final var gitDir = new File(REPO_DIR, GIT_DIR);
        gitDir.mkdirs();
        try {
            assertFalse(GitIgnoreUpdater.update(new File(REPO_DIR), new File("/tmp/.secrets")));
        } finally {
            gitDir.delete();
        }
    }

    @Test
    public void notGitRepoShouldPass() throws IOException {
        assertFalse(GitIgnoreUpdater.update(new File(REPO_DIR), new File(SECRETS_DIR)));
    }

    @Test
    public void gitRepoShouldPass() throws IOException {
        final var gitDir = new File(REPO_DIR, ".git");
        gitDir.mkdirs();
        try {
            assertTrue(GitIgnoreUpdater.update(new File(REPO_DIR), new File(SECRETS_DIR)));
        } finally {
            gitDir.delete();
            new File(REPO_DIR, Finals.GITIGNORE_FILE).delete();
        }
    }

}