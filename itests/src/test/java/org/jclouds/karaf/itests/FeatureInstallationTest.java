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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.labs.paxexam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;


import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.logLevel;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class FeatureInstallationTest extends JcloudsKarafTestSupport {

    @Before
    public void setUp() {
        System.err.println(executeCommand("features:addurl mvn:org.jclouds.karaf/jclouds-karaf/1.3.1-SNAPSHOT/xml/features"));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testBasicFeaturesInstallation() throws Exception {
        installAndCheckFeature("jclouds-commands");
    }

    @Test
    public void testFileSystemFeature() throws Exception {
        installAndCheckFeature("jclouds-api-filesystem");
    }

    @Test
    public void testAwsEc2Feature() throws Exception {
        installAndCheckFeature("jclouds-aws-ec2");
    }

    @Test
    public void testAwsS3Feature() throws Exception {
        installAndCheckFeature("jclouds-aws-s3");
    }

    @Test
    public void testAwsCloudwatchFeature() throws Exception {
        installAndCheckFeature("jclouds-aws-cloudwatch");
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
    public void testVcloudFeature() throws Exception {
        installAndCheckFeature("jclouds-api-vcloud");
    }

    @Test
    public void testEucalyptusFeature() throws Exception {
        installAndCheckFeature("jclouds-api-eucalyptus");
    }

    @Test
    public void testEucalyptusEc2Feature() throws Exception {
        installAndCheckFeature("jclouds-eucalyptus-ec2");
    }

    @Test
    public void testEucalyptusS3Feature() throws Exception {
        installAndCheckFeature("jclouds-eucalyptus-s3");
    }

    @Test
    public void testBlueLockFeature() throws Exception {
        installAndCheckFeature("jclouds-bluelock-vcloud-zone01");
    }

    @Test
    public void testCloudLoadBalancersFeature() throws Exception {
        installAndCheckFeature("jclouds-cloudloadbalancers-us");
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
    public void testElasticHostsLonBFeature() throws Exception {
        installAndCheckFeature("jclouds-elastichosts-lon-b");
    }

    @Test
    public void testElasticHostsLonPFeature() throws Exception {
        installAndCheckFeature("jclouds-elastichosts-lon-p");
    }

    @Test
    public void testElasticHostsSatPFeature() throws Exception {
        installAndCheckFeature("jclouds-elastichosts-sat-p");
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
    public void testRimuHostingFeature() throws Exception {
        installAndCheckFeature("jclouds-rimuhosting");
    }

    @Test
    public void testSavvisSymphonyVpdcFeature() throws Exception {
        installAndCheckFeature("jclouds-savvis-symphonyvpdc");
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
    public void testSynapticStorageFeature() throws Exception {
        installAndCheckFeature("jclouds-synaptic-storage");
    }

    @Test
    public void testStratogenVcloudMycloudFeature() throws Exception {
        installAndCheckFeature("jclouds-stratogen-vcloud-mycloud");
    }

    @Test
    public void tesTtrmkVcloudExpressFeature() throws Exception {
        installAndCheckFeature("jclouds-trmk-vcloudexpress");
    }

    @Test
    public void tesTtrmkEcloudFeature() throws Exception {
        installAndCheckFeature("jclouds-trmk-ecloud");
    }


    @Test
    public void testAzureBlobFeature() throws Exception {
        installAndCheckFeature("jclouds-azureblob");
    }
    public void installAndCheckFeature(String feature) throws Exception {
        System.err.println(executeCommand("features:install " + feature));
        FeaturesService featuresService  = getOsgiService(FeaturesService.class);
        System.err.println(executeCommand("osgi:list"));
        assertTrue("Expected "+feature+" feature to be installed.",featuresService.isInstalled(featuresService.getFeature(feature)));
    }

    @Configuration
    public Option[] config() {
        return new Option[]{
                jcloudsDistributionConfiguration(), keepRuntimeFolder(),
                logLevel(LogLevelOption.LogLevel.ERROR)};
    }
}
