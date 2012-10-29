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

package org.jclouds.karaf.itests.live;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jclouds.karaf.itests.JcloudsFeaturesTestSupport;

public class JcloudsLiveTestSupport extends JcloudsFeaturesTestSupport {

    String identity;
    String credential;
    String image;
    String location;
    String user;
    String regions;


    /**
     * Checks if Live Test parameters are properly configured for compute tests.
     *
     * @return
     */
    public boolean isComputeLiveConfigured() {
        return
                identity != null && credential != null && image != null && location != null
                        && !identity.isEmpty() && !credential.isEmpty() && !image.isEmpty() && !location.isEmpty();
    }

    /**
     * Checks if Live Test parameters are properly configured for blobstore tests.
     *
     * @return
     */
    public boolean isBlobStoreLiveConfigured() {
        return
                identity != null && credential != null
                        && !identity.isEmpty() && !credential.isEmpty();
    }

    /**
     * Creates a Manged Compute Service using the configured system properties.
     */
    public void createManagedComputeService(String provider, boolean eventSupport) {
        List<String> options = new LinkedList<String>();
        if (regions != null && !regions.isEmpty()) {
            options.add("jclouds.regions=" + regions);
        }
        String cmd = "jclouds:compute-service-create --provider " + provider + " --identity " + identity + " --credential " + credential;
        for (String option : options) {
            cmd += " --add-option " + option;
        }
        executeCommand(cmd);
    }


    /**
     * Creates a Manged Compute Service using the configured system properties.
     */
    public void createManagedBlobStoreService(String provider) {
        String cmd = "jclouds:blobstore-service-create --provider " + provider + " --identity " + identity + " --credential " + credential;
        executeCommand(cmd);
    }

    /**
     * @return the IP address of the client on which this code is running.
     * @throws java.io.IOException
     */
    protected String getOriginatingIp() throws IOException {
        URL url = new URL("http://checkip.amazonaws.com/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        return IOUtils.toString(connection.getInputStream()).trim() + "/32";
    }

}
