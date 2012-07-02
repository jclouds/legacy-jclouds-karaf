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

    public static final String JCLOUDS_COMPUTE_PROVIDER = "JCLOUDS_COMPUTE_PROVIDER";
    public static final String JCLOUDS_COMPUTE_API = "JCLOUDS_COMPUTE_API";
    public static final String JCLOUDS_COMPUTE_IDENTITY = "JCLOUDS_COMPUTE_IDENTITY";
    public static final String JCLOUDS_COMPUTE_CREDENTIAL = "JCLOUDS_COMPUTE_CREDENTIAL";
    public static final String JCLOUDS_COMPUTE_ENDPOINT = "JCLOUDS_COMPUTE_ENDPOINT";


    public static final String JCLOUDS_BLOBSTORE_PROVIDER = "JCLOUDS_BLOBSTORE_PROVIDER";
    public static final String JCLOUDS_BLOBSTORE_API = "JCLOUDS_BLOBSTORE_API";
    public static final String JCLOUDS_BLOBSTORE_IDENTITY = "JCLOUDS_BLOBSTORE_IDENTITY";
    public static final String JCLOUDS_BLOBSTORE_CREDENTIAL = "JCLOUDS_BLOBSTORE_CREDENTIAL";
    public static final String JCLOUDS_BLOBSTORE_ENDPOINT = "JCLOUDS_BLOBSTORE_ENDPOINT";

    public static final String JCLOUDS_USER = "JCLOUDS_USER";
    public static final String JCLOUDS_PASSWORD = "JCLOUDS_PASSWORD";

    private EnvHelper() {
        //Utility Class
    }

    /**
     * Returns the provider value and falls back to env if the specified value is null.
     *
     * @param provider
     * @return
     */
    public static String getComputeProvider(String provider) {
        if (provider != null) {
            return provider;
        } else {
            return System.getenv(JCLOUDS_COMPUTE_PROVIDER);
        }
    }

    /**
     * Returns the provider value and falls back to env if the specified value is null.
     * @param api
     * @return
     */
    public static String getComputeApi(String api) {
        if (api != null) {
            return api;
        } else {
            return System.getenv(JCLOUDS_COMPUTE_API);
        }
    }

    /**
     * Returns the provider value and falls back to env if the specified value is null.
     *
     * @param identity
     * @return
     */
    public static String getComputeIdentity(String identity) {
        if (identity != null) {
            return identity;
        } else {
            return System.getenv(JCLOUDS_COMPUTE_IDENTITY);
        }
    }

    /**
     * Returns the credential value and falls back to env if the specified value is null.
     *
     * @param credential
     * @return
     */
    public static String getComputeCredential(String credential) {
        if (credential != null) {
            return credential;
        } else {
            return System.getenv(JCLOUDS_COMPUTE_CREDENTIAL);
        }
    }

    /**
     * Returns the endpoint value and falls back to env if the specified value is null.
     *
     * @param endpoint
     * @return
     */
    public static String getComputeEndpoint(String endpoint) {
        if (endpoint != null) {
            return endpoint;
        } else {
            return System.getenv(JCLOUDS_COMPUTE_ENDPOINT);
        }
    }

    /**
     * Returns the provider value and falls back to env if the specified value is null.
     *
     * @param provider
     * @return
     */
    public static String getBlobStoreProvider(String provider) {
        if (provider != null) {
            return provider;
        } else {
            return System.getenv(JCLOUDS_BLOBSTORE_PROVIDER);
        }
    }

    /**
     * Returns the provider value and falls back to env if the specified value is null.
     * @param api
     * @return
     */
    public static String getBlobStoreApi(String api) {
        if (api != null) {
            return api;
        } else {
            return System.getenv(JCLOUDS_BLOBSTORE_API);
        }
    }

    /**
     * Returns the provider value and falls back to env if the specified value is null.
     *
     * @param identity
     * @return
     */
    public static String getBlobStoreIdentity(String identity) {
        if (identity != null) {
            return identity;
        } else {
            return System.getenv(JCLOUDS_BLOBSTORE_IDENTITY);
        }
    }

    /**
     * Returns the credential value and falls back to env if the specified value is null.
     *
     * @param credential
     * @return
     */
    public static String getBlobStoreCredential(String credential) {
        if (credential != null) {
            return credential;
        } else {
            return System.getenv(JCLOUDS_BLOBSTORE_CREDENTIAL);
        }
    }

    /**
     * Returns the endpoint value and falls back to env if the specified value is null.
     *
     * @param endpoint
     * @return
     */
    public static String getBlobStoreEndpoint(String endpoint) {
        if (endpoint != null) {
            return endpoint;
        } else {
            return System.getenv(JCLOUDS_BLOBSTORE_ENDPOINT);
        }
    }

    /**
     * Returns the user value and falls back to env if the specified value is null.
     *
     * @param user
     * @return
     */
    public static String getUser(String user) {
        if (user != null) {
            return user;
        } else {
            return System.getenv(JCLOUDS_USER);
        }
    }

    /**
     * Returns the password value and falls back to env if the specified value is null.
     *
     * @param password
     * @return
     */
    public static String getPassword(String password) {
        if (password != null) {
            return password;
        } else {
            return System.getenv(JCLOUDS_PASSWORD);
        }
    }

}
