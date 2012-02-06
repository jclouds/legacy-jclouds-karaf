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

import com.google.common.base.Predicate;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeState;
import org.jclouds.ec2.EC2Client;
import org.jclouds.ec2.domain.IpProtocol;
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

import javax.annotation.Nullable;

import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.debugConfiguration;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.CoreOptions.scanFeatures;
import static org.junit.Assert.*;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class AwsEc2LiveTest extends JcloudsLiveTestSupport {

    String group = "karaf";

    @Before
    public void setUp() throws Exception {
        identity = System.getProperty("jclouds.aws.identity");
        credential = System.getProperty("jclouds.aws.credential");
        regions = System.getProperty("jclouds.aws.region");
        image = System.getProperty("jclouds.aws.image");
        location = System.getProperty("jclouds.aws.location");
        user = System.getProperty("jclouds.aws.user");

        if (isLiveConfigured()) {
            installAndCheckFeature("jclouds-commands");
        }  else {
            System.err.println("Aborting test.");
            System.err.flush();
        }
    }

    @After
    public void tearDown() {
        if (isLiveConfigured()) {
            executeCommand("jclouds:node-destroy-all ");
        }
    }

    @Test
    public void testLive() throws InterruptedException {
        if (isLiveConfigured()) {
            createManagedComputeService("aws-ec2");
            ComputeService computeService = getOsgiService(ComputeService.class);
            Thread.sleep(DEFAULT_TIMEOUT);

            executeCommand("jclouds:image-list");
            executeCommand("jclouds:node-create " + image + " " + location + " " + group);
            assertTrue("Expected at least one node", computeService.listNodes().size() >= 1);
        }
    }

    @Configuration
    public Option[] config() {
        return new Option[]{
                jcloudsDistributionConfiguration(), keepRuntimeFolder(), logLevel(LogLevelOption.LogLevel.ERROR),
                systemProperty("jclouds.aws.identity"),
                systemProperty("jclouds.aws.credential"),
                systemProperty("jclouds.aws.region"),
                systemProperty("jclouds.aws.image"),
                systemProperty("jclouds.aws.location"),
                systemProperty("jclouds.aws.user"),
                scanFeatures(String.format(JCLOUDS_FEATURE_FORMAT, MavenUtils.getArtifactVersion(JCLOUDS_GROUP_ID, JCLOUDS_ARTIFACT_ID)),"jclouds", "jclouds-compute", "jclouds-aws-ec2").start()
        };
    }
}
