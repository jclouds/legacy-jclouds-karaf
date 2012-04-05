/*
 * Copyright (C) 2011, the original authors
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.karaf.itests.live;

import static org.junit.Assert.assertTrue;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.CoreOptions.scanFeatures;

import org.jclouds.compute.ComputeService;
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

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class RackspaceLiveTest extends JcloudsLiveTestSupport {

    String group = "karaf";

    @Before
    public void setUp() throws Exception {
        identity = System.getProperty("jclouds.rackspace.identity");
        credential = System.getProperty("jclouds.rackspace.credential");
        regions = System.getProperty("jclouds.rackspace.region");
        image = System.getProperty("jclouds.rackspace.image");
        location = System.getProperty("jclouds.rackspace.location");
        user = System.getProperty("jclouds.rackspace.user");

        if (isComputeLiveConfigured()) {
            installAndCheckFeature("jclouds-commands");
        } else {
            System.err.println("Aborting test.");
            System.err.flush();
        }
    }

    @After
    public void tearDown() {
        if (isComputeLiveConfigured()) {
            executeCommand("jclouds:node-destroy-all ");
        }
    }

    @Test
    public void testNodeCreateLive() throws InterruptedException {
        if (isComputeLiveConfigured()) {
            createManagedComputeService("cloudservers-us");
            ComputeService computeService = getOsgiService(ComputeService.class);
            Thread.sleep(DEFAULT_TIMEOUT);
            System.err.println(executeCommand("jclouds:image-list"));
            System.err.println(executeCommand("jclouds:node-create --image " + image + " --location" + location + " " + group));
            System.err.println(executeCommand("jclouds:group-runscript -d ls -u " + user + " " + group));
            assertTrue("Expected at least one node", computeService.listNodes().size() >= 1);
        }
    }

    @Configuration
    public Option[] config() {
        return new Option[]{
                jcloudsDistributionConfiguration(), keepRuntimeFolder(), logLevel(LogLevelOption.LogLevel.ERROR),
                systemProperty("jclouds.rackspace.identity"),
                systemProperty("jclouds.rackspace.credential"),
                systemProperty("jclouds.rackspace.region"),
                systemProperty("jclouds.rackspace.image"),
                systemProperty("jclouds.rackspace.location"),
                systemProperty("jclouds.rackspace.user"),
                scanFeatures(String.format(JCLOUDS_FEATURE_FORMAT, MavenUtils.getArtifactVersion(JCLOUDS_KARAF_GROUP_ID, JCLOUDS_KARAF_ARTIFACT_ID)), "jclouds", "jclouds-compute", "jclouds-cloudserver-us").start()
        };
    }
}
