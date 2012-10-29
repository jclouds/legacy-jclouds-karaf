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

package org.jclouds.karaf.services.modules;

import org.jclouds.domain.Credentials;
import org.jclouds.karaf.core.CredentialStore;
import org.jclouds.rest.ConfiguresCredentialStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@ConfiguresCredentialStore
public class PropertiesCredentialStore extends CredentialStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesCredentialStore.class);
    private final String PROPERTIES_FILE_PATH = System.getProperty("karaf.home") + File.separatorChar + "etc" + File.separatorChar + "org.jclouds.credentials.cfg";

    public PropertiesCredentialStore() {
        store = new PropertiesBacking();
    }

    /**
     * Configures a {@link com.google.inject.Binder} via the exposed methods.
     */
    @Override
    protected void configure() {
    }

    private class PropertiesBacking implements Map<String, Credentials> {
        private final Map<String, Credentials> credentialsMap = new LinkedHashMap<String, Credentials>();
        private Properties properties = new Properties();

        private PropertiesBacking() {
            load();
            Enumeration keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                if (key != null && key.startsWith("node#")) {
                    String stripedKey = key.substring(0, key.lastIndexOf("/"));
                    if (!credentialsMap.containsKey(stripedKey)) {
                        String identityKey = stripedKey + "/identity";
                        String credentialKey = stripedKey + "/credential";
                        String identity = (String) properties.get(identityKey);
                        String credential = (String) properties.get(credentialKey);
                        Credentials credentials = new Credentials(identity, credential);
                        credentialsMap.put(stripedKey, credentials);
                    }
                }
            }
            save();
        }

        private void save() {
            File f = new File(PROPERTIES_FILE_PATH);
            FileOutputStream fos = null;
            try {
                if (f.exists()) {
                    f.createNewFile();
                }
                fos = new FileOutputStream(f);
                properties.store(fos, "jclouds credentials");
            } catch (IOException e) {
                LOGGER.warn("Failed to store jclouds credentials to file.", e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Throwable t) {
                    }
                }
            }
        }

        private void load() {
            File f = new File(PROPERTIES_FILE_PATH);
            FileInputStream fis = null;
            try {
                if (!f.exists()) {
                    f.createNewFile();
                }
                fis = new FileInputStream(f);
                properties.load(fis);
            } catch (IOException e) {
                LOGGER.warn("Failed to load jclouds credentials from file.", e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Throwable t) {
                    }
                }
            }
        }


        public int size() {
            return credentialsMap.size();
        }

        public boolean isEmpty() {
            return credentialsMap.isEmpty();
        }

        public boolean containsKey(Object o) {
            return credentialsMap.containsKey(o);
        }

        public boolean containsValue(Object o) {
            return credentialsMap.containsValue(o);
        }

        public Credentials get(Object o) {
            return credentialsMap.get(o);
        }

        public Credentials put(String s, Credentials credentials) {
            if (credentials != null) {
                String identityKey = s + "/identity";
                String credentialKey = s + "/credential";
                if (credentials.identity != null) {
                    properties.put(identityKey, credentials.identity);
                }
                if (credentials.credential != null) {
                    properties.put(credentialKey, credentials.credential);
                }
                save();
                return credentialsMap.put(s, credentials);
            }
            return credentials;
        }

        public Credentials remove(Object o) {
            String identityKey = o + "/identity";
            properties.remove(identityKey);
            save();
            return credentialsMap.remove(o);
        }

        public void putAll(Map<? extends String, ? extends Credentials> map) {
            for (Entry<? extends String, ? extends Credentials> entry : map.entrySet()) {
                String s = entry.getKey();
                Credentials credential = entry.getValue();
                put(s, credential);
            }
        }

        public void clear() {
            properties.clear();
            save();
            credentialsMap.clear();
        }

        public Set<String> keySet() {
            return credentialsMap.keySet();
        }

        public Collection<Credentials> values() {
            return credentialsMap.values();
        }

        public Set<Entry<String, Credentials>> entrySet() {
            return credentialsMap.entrySet();
        }

        @Override
        public boolean equals(Object o) {
            return credentialsMap.equals(o);
        }

        @Override
        public int hashCode() {
            return credentialsMap.hashCode();
        }
    }
}
