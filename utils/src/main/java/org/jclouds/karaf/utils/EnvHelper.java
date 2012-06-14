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

package org.jclouds.karaf.utils;

public class EnvHelper {

    public static final String JCLOUDS_PROVIDER = "JCLOUDS_PROVIDER";
    public static final String JCLOUDS_IDENTITY = "JCLOUDS_IDENTITY";
    public static final String JCLOUDS_CREDENTIAL = "JCLOUDS_CREDENTIAL";
    public static final String JCLOUDS_ENDPOINT = "JCLOUDS_ENDPOIT";

    private EnvHelper() {
        //Utility Class
    }

    /**
     * Returns the provider value and falls back to env if the specified value is null.
     *
     * @param provider
     * @return
     */
    public static String getProvider(String provider) {
        if (provider != null) {
            return provider;
        } else {
            return System.getenv(JCLOUDS_PROVIDER);
        }
    }

    /**
     * Returns the provider value and falls back to env if the specified value is null.
     *
     * @param identity
     * @return
     */
    public static String getIdentity(String identity) {
        if (identity != null) {
            return identity;
        } else {
            return System.getenv(JCLOUDS_IDENTITY);
        }
    }

    /**
     * Returns the credential value and falls back to env if the specified value is null.
     *
     * @param credential
     * @return
     */
    public static String getCredential(String credential) {
        if (credential != null) {
            return credential;
        } else {
            return System.getenv(JCLOUDS_CREDENTIAL);
        }
    }

    /**
     * Returns the endpoint value and falls back to env if the specified value is null.
     *
     * @param endpoint
     * @return
     */
    public static String getEndpoint(String endpoint) {
        if (endpoint != null) {
            return endpoint;
        } else {
            return System.getenv(JCLOUDS_ENDPOINT);
        }
    }

}
