package org.metalib.gcloud.secrets;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Ignore;
import org.junit.Rule;
import static org.junit.Assert.*;
import static org.metalib.gcloud.secrets.helpers.Finals.SECRET_SET;

import org.junit.Test;
import java.io.File;

public class SecretSetTest {
    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {}
        @Override
        protected void after() {}
    };

    /**
     * @throws Exception if any
     */
    @Test
    @Ignore("CI/CD fail")
    public void testSomething() throws Exception {
        File pom = new File( "target/test-classes/project-to-test/" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        SecretSetMojo secretSetMojo = (SecretSetMojo) rule.lookupConfiguredMojo( pom, SECRET_SET);
        assertNotNull(secretSetMojo);

        secretSetMojo.execute();

        File outputDirectory = ( File ) rule.getVariableValueFromObject(secretSetMojo, "secretDir" );
        assertNotNull( outputDirectory );
    }

    /** Do not need the MojoRule. */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn() {
        assertTrue( true );
    }

}

