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
public class AwsFeaturesInstallationTest extends JcloudsFeaturesTestSupport {

    @Before
    public void setUp() {
        System.err.println(executeCommand("features:addurl " + getJcloudsKarafFeatureURL()));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testAwsRoute53Feature() throws Exception {
        installAndCheckFeature("jclouds-aws-route53");
    }

    @Test
    public void testAwsSqsFeature() throws Exception {
        installAndCheckFeature("jclouds-aws-sqs");
    }

    @Ignore
    @Test
    public void testAwsStsFeature() throws Exception {
        installAndCheckFeature("jclouds-aws-sts");
    }

    @Test
    public void testAwsS3Feature() throws Exception {
        installAndCheckFeature("jclouds-aws-s3");
    }

    @Test
    public void testAwsEc2Feature() throws Exception {
        installAndCheckFeature("jclouds-aws-ec2");
    }

    @Test
    public void testAwsCloudwatchFeature() throws Exception {
        installAndCheckFeature("jclouds-aws-cloudwatch");
    }
}
