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

package org.jclouds.karaf.itests.special;

import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.logLevel;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.replaceConfigurationFile;

import java.io.File;

import org.jclouds.karaf.itests.live.AwsEc2LiveTest;
import org.junit.Before;
import org.openengsb.labs.paxexam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;

public class JcloudsCamelCxfFeaturesTestSupport extends AwsEc2LiveTest {

    public static final String CAMEL_GROUP_ID = "org.apache.camel.karaf";
    public static final String CAMEL_ARTIFACT_ID = "apache-camel";
    public static final String CAMEL_FEATURE_FORMAT = "mvn:org.apache.camel.karaf/apache-camel/%s/xml/features";
    public static final String CAMEL_FEATURE_VERSION_PROPERTY =  "camel.feature.version";

    @Before
    public void setUp() throws Exception {
        System.err.println(executeCommand("features:addurl " + String.format(CAMEL_FEATURE_FORMAT,System.getProperty(CAMEL_FEATURE_VERSION_PROPERTY))));
        System.err.println(executeCommand("features:removeurl " + String.format(JCLOUDS_FEATURE_FORMAT, "1.3.1")));
        System.err.println(executeCommand("features:addurl " + String.format(JCLOUDS_FEATURE_FORMAT,System.getProperty(JCLOUDS_FEATURE_VERSION_PROPERTY))));
        System.err.println(executeCommand("features:install xml-specs-api"));
        System.err.println(executeCommand("osgi:install mvn:org.apache.servicemix.specs/org.apache.servicemix.specs.jsr250-1.0/1.9.0"));
        System.err.println(executeCommand("features:install camel-cxf"));
        System.err.println(executeCommand("features:install jclouds-aws-ec2"));
        System.err.println(executeCommand("features:install jclouds-services"));
        super.setUp();
    }

    @Configuration
    public Option[] config()  {
        return new Option[]{
                jcloudsDistributionConfiguration(), keepRuntimeFolder(), logLevel(LogLevelOption.LogLevel.ERROR),
                systemProperty("jclouds.aws.identity"),
                systemProperty("jclouds.aws.credential"),
                systemProperty("jclouds.aws.region"),
                systemProperty("jclouds.aws.image"),
                systemProperty("jclouds.aws.location"),
                systemProperty("jclouds.aws.user"),
                systemProperty(JCLOUDS_FEATURE_VERSION_PROPERTY, MavenUtils.getArtifactVersion(JCLOUDS_KARAF_GROUP_ID, JCLOUDS_KARAF_ARTIFACT_ID)),
                systemProperty(CAMEL_FEATURE_VERSION_PROPERTY, MavenUtils.getArtifactVersion(CAMEL_GROUP_ID, CAMEL_ARTIFACT_ID)),
                replaceConfigurationFile("etc/jre.properties", new File("target/test-classes/jre.properties.cxf")),
        };
    }
}
