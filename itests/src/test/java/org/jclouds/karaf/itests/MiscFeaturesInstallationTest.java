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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class MiscFeaturesInstallationTest extends JcloudsFeaturesTestSupport {

    @Before
    public void setUp() {
        System.err.println(executeCommand("features:addurl " + getJcloudsKarafFeatureURL()));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testManagementFeature() throws Exception {
        installAndCheckFeature("jclouds-management");
    }

    @Test
    public void testBasicFeaturesInstallation() throws Exception {
        installAndCheckFeature("jclouds-commands");
    }

    @Test
    public void testBlueLockFeature() throws Exception {
        installAndCheckFeature("jclouds-bluelock-vcloud-zone01");
    }


    @Test
    public void testCloudOneStorageFeature() throws Exception {
        installAndCheckFeature("jclouds-cloudonestorage");
    }

    @Test
    public void testCloudSigmaZrhFeature() throws Exception {
        installAndCheckFeature("jclouds-cloudsigma-zrh");
    }

    @Test
    public void testGoGridFeature() throws Exception {
        installAndCheckFeature("jclouds-gogrid");
    }

    @Test
    public void testGo2CloudFeature() throws Exception {
        installAndCheckFeature("jclouds-go2cloud-jhb1");
    }


    @Test
    public void testGreenHouseDataFeature() throws Exception {
        installAndCheckFeature("jclouds-greenhousedata-element-vcloud");
    }

    @Test
    public void testNineFoldStorageFeature() throws Exception {
        installAndCheckFeature("jclouds-ninefold-storage");
    }

    @Test
    public void testOpenHostingEast1Feature() throws Exception {
        installAndCheckFeature("jclouds-openhosting-east1");
    }

    @Test
    public void testServerloveZ1ManFeature() throws Exception {
        installAndCheckFeature("jclouds-serverlove-z1-man");
    }

    @Test
    public void testSkalicloudSdgMyFeature() throws Exception {
        installAndCheckFeature("jclouds-skalicloud-sdg-my");
    }

    @Test
    public void testSoftlayerFeature() throws Exception {
        installAndCheckFeature("jclouds-softlayer");
    }

    @Test
    public void testTrmkVcloudExpressFeature() throws Exception {
        installAndCheckFeature("jclouds-trmk-vcloudexpress");
    }

    @Test
    public void testTrmkEcloudFeature() throws Exception {
        installAndCheckFeature("jclouds-trmk-ecloud");
    }

    @Test
    public void testAzureBlobFeature() throws Exception {
        installAndCheckFeature("jclouds-azureblob");
    }

    @Test
    public void testDynectFeature() throws Exception {
        installAndCheckFeature("jclouds-dynect");
    }

    @Test
    public void testGlesysFeature() throws Exception {
        installAndCheckFeature("jclouds-glesys");
    }
}
