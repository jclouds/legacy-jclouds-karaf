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

import static junit.framework.Assert.assertTrue;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.logLevel;

import org.apache.karaf.features.FeaturesService;
import org.openengsb.labs.paxexam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;

public class JcloudsFeaturesTestSupport extends JcloudsKarafTestSupport {

    public static final String JCLOUDS_FEATURE_FORMAT = "mvn:org.jclouds.karaf/jclouds-karaf/%s/xml/features";
    public static final String JCLOUDS_FEATURE_VERSION_PROPERTY =  "jclouds.feature.version";

    /**
     * Returns the URL of the jclouds-karaf feature.
     * <p>Note: This method is intended to be invoked inside the test container</p>
     * @return
     */
    public String getJcloudsKarafFeatureURL() {
        return String.format(JCLOUDS_FEATURE_FORMAT, System.getProperty(JCLOUDS_FEATURE_VERSION_PROPERTY));
    }

    /**
     * Installs a feature and checks that feature is properly installed.
     * @param feature
     * @throws Exception
     */
    public void installAndCheckFeature(String feature) throws Exception {
        System.err.println(executeCommand("features:install " + feature));
        FeaturesService featuresService = getOsgiService(FeaturesService.class);
        System.err.println(executeCommand("osgi:list -t 0"));
        assertTrue("Expected " + feature + " feature to be installed.", featuresService.isInstalled(featuresService.getFeature(feature)));
    }


    @Configuration
    public Option[] config() {
        return new Option[]{
                jcloudsDistributionConfiguration(), keepRuntimeFolder(),
                systemProperty(JCLOUDS_FEATURE_VERSION_PROPERTY, MavenUtils.getArtifactVersion(JCLOUDS_KARAF_GROUP_ID, JCLOUDS_KARAF_ARTIFACT_ID)),
                logLevel(LogLevelOption.LogLevel.ERROR)};
    }
}
