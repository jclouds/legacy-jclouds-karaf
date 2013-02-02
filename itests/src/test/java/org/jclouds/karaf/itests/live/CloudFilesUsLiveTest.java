/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jclouds.karaf.itests.live;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.CoreOptions.scanFeatures;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import org.apache.karaf.features.FeaturesService;
import org.jclouds.blobstore.BlobStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.labs.paxexam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class CloudFilesUsLiveTest extends JcloudsLiveTestSupport {

    String group = "karaf";

    @Before
    public void setUp() throws Exception {
        identity = System.getProperty("jclouds.rackspace.identity");
        credential = System.getProperty("jclouds.rackspace.credential");

        if (isBlobStoreLiveConfigured()) {
            installAndCheckFeature("jclouds-commands");
            Thread.sleep(DEFAULT_TIMEOUT);
        }  else {
            System.err.println("Aborting test.");
            System.err.flush();
        }
    }

    @After
    public void tearDown() {
        if (isBlobStoreLiveConfigured()) {
            System.err.println(executeCommand("jclouds:blobstore-delete-container itest-container"));
        }
    }

    @Test
    public void testBlobStoreReadWrite() throws InterruptedException {
        if (isBlobStoreLiveConfigured()) {
            createManagedBlobStoreService("cloudfiles-us");
            BlobStore blobStoreService = getOsgiService(BlobStore.class);
            Thread.sleep(DEFAULT_TIMEOUT);

            String featureURL = System.getProperty("jclouds.featureURL");
            System.err.println(executeCommand("jclouds:blobstore-list"));
            System.err.println(executeCommand("jclouds:blobstore-create itest-container"));

            System.err.println(executeCommand("jclouds:blobstore-write itest-container  myfolder/myfile "+ featureURL ));
            System.err.println(executeCommand("jclouds:blobstore-write --url-payload itest-container "+ System.getProperty("jclouds.featureURL")));
            System.err.println(executeCommand("jclouds:blobstore-read --display itest-container testblob "));
        }
    }


    @Test
    public void testUrlHandler() throws Exception {
        if (isBlobStoreLiveConfigured()) {
            createManagedBlobStoreService("cloudfiles-us");
            BlobStore blobStoreService = getOsgiService(BlobStore.class);
            Thread.sleep(DEFAULT_TIMEOUT);

            FeaturesService featuresService = getOsgiService(FeaturesService.class);
            featuresService.installFeature("jclouds-url-handler");

            //Let's add a bundle to S3
            String groupId =  "org.jclouds.api";
            String artifactId =  "byon";
            String version =   System.getProperty("jclouds.version");

            System.err.println(executeCommand("jclouds:blobstore-list"));
            System.err.println(executeCommand("jclouds:blobstore-create itest-container"));

            URL artifactUrl = new URL("mvn:"+groupId+"/"+artifactId+"/"+version);
            URL blobUrl = new URL("blob:aws-s3/itest-container/maven2/org/jclouds/api/byon/" +version+"/"+artifactId+"-"+version+".jar" );
            InputStream is = artifactUrl.openConnection().getInputStream();
            OutputStream os = blobUrl.openConnection().getOutputStream();
            try {
                ByteStreams.copy(is, os);
            } finally {
                Closeables.closeQuietly(is);
                Closeables.closeQuietly(os);
            }

            //Make sure that only S3 is available as a repo.
            System.err.println(executeCommands("config:edit org.ops4j.pax.url.mvn",
                    "config:propset org.ops4j.pax.url.mvn.localRepository " + System.getProperty("karaf.base") + File.separatorChar + "none",
                    "config:propset org.ops4j.pax.url.mvn.repositories blob:cloudfiles-us/itest-container/maven2@snapshots ",
                    "config:update"));
            Thread.sleep(DEFAULT_TIMEOUT);

            final Set<String> installedSymbolicNames = new LinkedHashSet<String>();

            //Add a Bundle Listener
            bundleContext.addBundleListener(new BundleListener(){
                @Override
                public void bundleChanged(BundleEvent event) {
                  if (event.getType() == BundleEvent.INSTALLED) {
                      installedSymbolicNames.add(event.getBundle().getSymbolicName());
                  }
                }
            });

            //Install the bundle the from S3.
            System.err.println(executeCommand("osgi:install mvn:org.jclouds.api/byon/" + version));

            //Verify that no other bundle can be installed.
            System.err.println(executeCommand("osgi:install mvn:org.jclouds.api/nova/" + version));
            assertTrue(installedSymbolicNames.contains("byon"));
            assertFalse(installedSymbolicNames.contains("nova"));


        }
    }

    @Configuration
    public Option[] config() {
        return new Option[]{
                jcloudsDistributionConfiguration(), keepRuntimeFolder(), logLevel(LogLevelOption.LogLevel.ERROR),
                systemProperty("jclouds.rackspace.identity"),
                systemProperty("jclouds.rackspace.credential"),
                systemProperty("jclouds.karaf.version",MavenUtils.getArtifactVersion(JCLOUDS_KARAF_GROUP_ID, JCLOUDS_KARAF_ARTIFACT_ID)),
                systemProperty("jclouds.version",MavenUtils.getArtifactVersion(JCLOUDS_GROUP_ID, JCLOUDS_ARTIFACT_ID)),
                systemProperty("jclouds.featureURL",String.format(JCLOUDS_FEATURE_FORMAT, MavenUtils.getArtifactVersion(JCLOUDS_KARAF_GROUP_ID, JCLOUDS_KARAF_ARTIFACT_ID))),
                scanFeatures(String.format(JCLOUDS_FEATURE_FORMAT, MavenUtils.getArtifactVersion(JCLOUDS_KARAF_GROUP_ID, JCLOUDS_KARAF_ARTIFACT_ID)),"jclouds", "jclouds-commands", "jclouds-cloudfiles-us").start()
        };
    }
}
