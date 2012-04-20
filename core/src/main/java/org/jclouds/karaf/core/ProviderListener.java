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

package org.jclouds.karaf.core;

import org.jclouds.providers.ProviderMetadata;

import java.util.Map;

/**
 * A simple listener which receives notifications when a cloud provider has been installed.
 */
public interface ProviderListener {

    void providerInstalled(ProviderMetadata metadata);
    void providerUninstalled(ProviderMetadata metadata);

    Map<String, ProviderMetadata> getInstalledProviders();


}
