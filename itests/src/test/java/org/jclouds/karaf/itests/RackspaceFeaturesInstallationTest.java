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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class RackspaceFeaturesInstallationTest extends JcloudsFeaturesTestSupport {

    @Before
    public void setUp() {
        System.err.println(executeCommand("features:addurl " + getJcloudsKarafFeatureURL()));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testRackspaceCloudServersUsFeature() throws Exception {
        installAndCheckFeature("jclouds-rackspace-cloudservers-us");
    }

    @Test
    public void testRackspaceCloudServersUkFeature() throws Exception {
        installAndCheckFeature("jclouds-rackspace-cloudservers-uk");
    }

    @Test
    public void testCloudServersUsFeature() throws Exception {
        installAndCheckFeature("jclouds-cloudserver-us");
    }

    @Test
    public void testCloudServersUkFeature() throws Exception {
        installAndCheckFeature("jclouds-cloudserver-uk");
    }

    @Test
    public void testCloudFilesUsFeature() throws Exception {
        installAndCheckFeature("jclouds-cloudfiles-us");
    }

    @Test
    public void testCloudFilesUkFeature() throws Exception {
        installAndCheckFeature("jclouds-cloudfiles-uk");
    }

    @Test
    public void testCloudDnsUsFeature() throws Exception {
        installAndCheckFeature("jclouds-rackspace-clouddns-us");
    }

    @Test
    public void testCloudDnsUkFeature() throws Exception {
        installAndCheckFeature("jclouds-rackspace-clouddns-uk");
    }

    @Test
    public void testCloudLoadBalancersUsFeature() throws Exception {
        installAndCheckFeature("jclouds-rackspace-cloudloadbalancers-us");
    }

    @Test
    public void testCloudLoadBalancersUkFeature() throws Exception {
        installAndCheckFeature("jclouds-rackspace-cloudloadbalancers-uk");
    }

    @Test
    public void testCloudBlockStorageUsFeature() throws Exception {
        installAndCheckFeature("jclouds-rackspace-cloudblockstorage-us");
    }

    @Test
    public void testCloudBlockStorageUkFeature() throws Exception {
        installAndCheckFeature("jclouds-rackspace-cloudblockstorage-uk");
    }


}
