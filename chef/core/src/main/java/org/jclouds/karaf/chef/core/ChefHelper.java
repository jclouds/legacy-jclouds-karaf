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

package org.jclouds.karaf.chef.core;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.chef.ChefContext;
import org.jclouds.chef.ChefService;
import org.jclouds.chef.config.ChefProperties;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Charsets.UTF_8;

public class ChefHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChefHelper.class);

    public static final String JCLOUDS_CHEF_API = "JCLOUDS_CHEF_API";
    public static final String JCLOUDS_CHEF_CLIENT_NAME = "JCLOUDS_COMPUTE_IDENTITY";
    public static final String JCLOUDS_CHEF_CLIENT_KEY_FILE = "JCLOUDS_CHEF_CLIENT_KEY_FILE";
    public static final String JCLOUDS_CHEF_CLIENT_CREDENTIAL = "JCLOUDS_CHEF_CLIENT_CREDENTIAL";
    public static final String JCLOUDS_CHEF_VALIDATOR_NAME = "JCLOUDS_CHEF_VALIDATOR_NAME";
    public static final String JCLOUDS_CHEF_VALIDATOR_KEY_FILE = "JCLOUDS_CHEF_VALIDATOR_KEY_FILE";
    public static final String JCLOUDS_CHEF_VALIDATOR_CREDENTIAL = "JCLOUDS_CHEF_VALIDATOR_CREDENTIAL";
    public static final String JCLOUDS_CHEF_ENDPOINT = "JCLOUDS_CHEF_ENDPOINT";

    private ChefHelper() {
        //Utility Class
    }

    /**
     * Returns the provider value and falls back to env if the specified value is null.
     *
     * @param api
     * @return
     */
    public static String getChefApi(String api) {
        if (api != null) {
            return api;
        } else {
            return System.getenv(JCLOUDS_CHEF_API);
        }
    }

    /**
     * Returns the client name value and falls back to env if the specified value is null.
     *
     * @param clientName
     * @return
     */
    public static String getClientName(String clientName) {
        if (clientName != null) {
            return clientName;
        } else {
            return System.getenv(JCLOUDS_CHEF_CLIENT_NAME);
        }
    }

    /**
     * Returns the client pem value and falls back to env if the specified value is null.
     *
     * @param clientKeyFile
     * @return
     */
    public static String getClientKeyFile(String clientKeyFile) {
        if (clientKeyFile != null) {
            return clientKeyFile;
        } else {
            return System.getenv(JCLOUDS_CHEF_CLIENT_KEY_FILE);
        }
    }

    /**
     * Returns the validator name value and falls back to env if the specified value is null.
     *
     * @param validatorName
     * @return
     */
    public static String getValidatorName(String validatorName) {
        if (validatorName != null) {
            return validatorName;
        } else {
            return System.getenv(JCLOUDS_CHEF_VALIDATOR_NAME);
        }
    }

    /**
     * Returns the validator pem value and falls back to env if the specified value is null.
     *
     * @param validatorKeyFile
     * @return
     */
    public static String getValidatorKeyFile(String validatorKeyFile) {
        if (validatorKeyFile != null) {
            return validatorKeyFile;
        } else {
            return System.getenv(JCLOUDS_CHEF_VALIDATOR_KEY_FILE);
        }
    }

    /**
     * Returns the endpoint value and falls back to env if the specified value is null.
     *
     * @param endpoint
     * @return
     */
    public static String getChefEndpoint(String endpoint) {
        if (endpoint != null) {
            return endpoint;
        } else {
            return System.getenv(JCLOUDS_CHEF_ENDPOINT);
        }
    }


    /**
     * Chooses a {@link ChefService} that matches the specified a service id or a  api.
     *
     * @param id
     * @param api
     * @param services
     * @return
     */
    public static ChefService getChefService(String id, String api, List<ChefService> services) {
        if (!Strings.isNullOrEmpty(id)) {
            ChefService service = null;
            for (ChefService svc : services) {
                if (id.equals(svc.getContext().getName())) {
                    service = svc;
                    break;
                }
            }
            if (service == null) {
                throw new IllegalArgumentException("No chef service with id" + id + " found.");
            }
            return service;
        }

        if (!Strings.isNullOrEmpty(api)) {
            ChefService service = null;
            for (ChefService svc : services) {
                if (api.equals(svc.getContext().getId())) {
                    service = svc;
                    break;
                }
            }
            if (service == null) {
                throw new IllegalArgumentException("No Api named " + api + " found.");
            }
            return service;
        } else {
            if (services.size() == 0) {
                throw new IllegalArgumentException("No apis are present.  Note: It takes a couple of seconds for the provider to initialize.");
            } else if (services.size() != 1) {
                StringBuilder sb = new StringBuilder();
                for (ChefService svc : services) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(svc.getContext().getId());
                }
                throw new IllegalArgumentException("Multiple apis are present, please select one using the--api argument in the following values: " + sb.toString());
            } else {
                return services.get(0);
            }
        }
    }

    public static ChefService createChefService(ApiMetadata apiMetadata, String name, String clientName, String clientCredential, String clientKeyFile, String validatorName, String validatorCredential, String validatorKeyFile, String endpoint) throws Exception {
        if (Strings.isNullOrEmpty(clientName) && apiMetadata != null && !apiMetadata.getDefaultCredential().isPresent()) {
            LOGGER.warn("No client specified for api {}.", apiMetadata.getId());
            return null;
        }

        if (Strings.isNullOrEmpty(validatorName) && apiMetadata != null && !apiMetadata.getDefaultCredential().isPresent()) {
            LOGGER.warn("No validator name specified for api {}.", apiMetadata.getId());
            return null;
        }


        if (Strings.isNullOrEmpty(validatorCredential) && !Strings.isNullOrEmpty(validatorKeyFile)) {
            validatorCredential = credentialsFromPath(validatorKeyFile);
        }

        if (Strings.isNullOrEmpty(clientCredential) && !Strings.isNullOrEmpty(clientKeyFile)) {
            clientCredential = credentialsFromPath(clientKeyFile);
        } else if (Strings.isNullOrEmpty(clientCredential)) {
            clientCredential = credentialForClient(clientName);
        }


        Properties chefConfig = new Properties();
        chefConfig.put(ChefProperties.CHEF_VALIDATOR_NAME, validatorName);
        chefConfig.put(ChefProperties.CHEF_VALIDATOR_CREDENTIAL, validatorCredential);

        ContextBuilder builder = null;
        if (apiMetadata != null) {
            builder = ContextBuilder.newBuilder(apiMetadata).overrides(chefConfig);
        }

        if (!Strings.isNullOrEmpty(endpoint)) {
            builder = builder.endpoint(endpoint);
        }

        builder = builder.name(name).modules(ImmutableSet.<Module>of(new SLF4JLoggingModule()));
        builder = builder.name(name).credentials(clientName, clientCredential).overrides(chefConfig);

        ChefContext context = builder.build();
        ChefService service = context.getChefService();
        return service;
    }

    /**
     * Returns credentials for client.
     *
     * @param client
     * @return
     * @throws Exception
     */
    public static String credentialForClient(final String client) throws Exception {
        String pemFile = System.getProperty("user.home") + "/.chef/" + client + ".pem";
        return Files.toString(new File(pemFile), UTF_8);
    }

    /**
     * Returns credentials from a specified path.
     * @param path
     * @return
     * @throws Exception
     */
    public static String credentialsFromPath(final String path) throws Exception {
        return Files.toString(new File(path), UTF_8);
    }

}
