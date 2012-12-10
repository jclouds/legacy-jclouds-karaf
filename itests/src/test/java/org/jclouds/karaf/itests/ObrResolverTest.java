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

package org.jclouds.karaf.itests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class ObrResolverTest extends JcloudsFeaturesTestSupport {

    @Before
    public void setUp() {
        System.err.println(executeCommand("features:addurl " + getJcloudsKarafFeatureURL()));
    }

    @After
    public void tearDown() {

    }

    /**
     * This test checks that when the obr resolver is installed, jclouds-services feature can be properly installed.
     * @throws Exception
     */
    @Test
    public void testJcloudsKarafServicesResolution() throws Exception {
        installAndCheckFeature("obr");
        installAndCheckFeature("jclouds-services");
        String jcloudsServicesBundles = executeCommand("osgi:list -s | grep -i -c org.jclouds.karaf.services");
        assertNotNull(jcloudsServicesBundles);
        int jcloudsServicesBundlesCount = Integer.parseInt(jcloudsServicesBundles.trim());
        assertEquals("Expected only one jclouds-services bundle", 1, jcloudsServicesBundlesCount);
    }

    /**
     * This test checks that when the obr resolver is installed, jclouds-commands feature can be properly installed.
     * @throws Exception
     */
    @Test
    public void testJcloudsKarafCommandsResolution() throws Exception {
        installAndCheckFeature("obr");
        installAndCheckFeature("jclouds-commands");
        String jcloudsCommandsBundles = executeCommand("osgi:list -s | grep -i -c org.jclouds.karaf.commands");
        assertNotNull(jcloudsCommandsBundles);
        int jcloudsCommandsBundlesCount = Integer.parseInt(jcloudsCommandsBundles.trim());
        assertEquals("Expected one jclouds-commands bundle", 1, jcloudsCommandsBundlesCount);

        String jcloudsServicesBundles = executeCommand("osgi:list -s | grep -i -c org.jclouds.karaf.services");
        assertNotNull(jcloudsServicesBundles);
        int jcloudsServicesBundlesCount = Integer.parseInt(jcloudsServicesBundles.trim());
        assertEquals("Expected one jclouds-services bundle", 1, jcloudsServicesBundlesCount);
    }

    /**
     * This test checks that when the obr resolver is installed, jclouds-commands feature can be properly installed.
     * @throws Exception
     */
    @Test
    public void testJcloudsKarafChefResolution() throws Exception {
        installAndCheckFeature("obr");
        installAndCheckFeature("jclouds-chef");
        String jcloudsChefCommandsBundles = executeCommand("osgi:list -s | grep -i -c org.jclouds.karaf.chef.commands");
        assertNotNull(jcloudsChefCommandsBundles);
        int jcloudsCommandsBundlesCount = Integer.parseInt(jcloudsChefCommandsBundles.trim());
        assertEquals("Expected one chef commands bundle", 1, jcloudsCommandsBundlesCount);

        String jcloudsChefServicesBundles = executeCommand("osgi:list -s | grep -i -c org.jclouds.karaf.chef.services");
        assertNotNull(jcloudsChefServicesBundles);
        int jcloudsServicesBundlesCount = Integer.parseInt(jcloudsChefServicesBundles.trim());
        assertEquals("Expected one chef services bundle", 1, jcloudsServicesBundlesCount);
    }

    /**
     * This test checks that when the obr resolver is installed, we don't have multiple jersey bundles.
     * @throws Exception
     */
    @Test
    public void testJerseyResolution() throws Exception {
        installAndCheckFeature("obr");
        executeCommand("osgi:install -s mvn:com.sun.jersey/jersey-core/1.6");
        installAndCheckFeature("jclouds-aws-ec2");
        String jerseyBundles = executeCommand("osgi:list | grep -i -c jersey");
        assertNotNull(jerseyBundles);
        int jerseyBundleCount = Integer.parseInt(jerseyBundles.trim());
        assertEquals("Expected only one jersey bundle",1,jerseyBundleCount);
    }
}
