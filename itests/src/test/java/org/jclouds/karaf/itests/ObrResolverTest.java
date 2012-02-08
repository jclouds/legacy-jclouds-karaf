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

package org.jclouds.karaf.itests;

import org.apache.karaf.features.FeaturesService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.labs.paxexam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.logLevel;

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
     * This test checks that when the obr resolver is installed
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
