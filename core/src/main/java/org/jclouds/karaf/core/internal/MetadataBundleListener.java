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

package org.jclouds.karaf.core.internal;

import com.google.common.reflect.TypeToken;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.ApiPredicates;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.karaf.core.BlobStoreProviderOrApiListener;
import org.jclouds.karaf.core.ComputeProviderOrApiListener;
import org.jclouds.karaf.core.ProviderOrApiListener;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.ProviderPredicates;
import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetadataBundleListener implements BundleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataBundleListener.class);

    private Map<Long, ProviderMetadata> providerMetadataMap = new HashMap<Long, ProviderMetadata>();
    private Map<Long, ApiMetadata> apiMetadataMap = new HashMap<Long, ApiMetadata>();


    private Set<ProviderMetadata> computeProviders = new HashSet<ProviderMetadata>();
    private Set<ProviderMetadata> blobStoreProviders = new HashSet<ProviderMetadata>();

    private Set<ApiMetadata> computeApis = new HashSet<ApiMetadata>();
    private Set<ApiMetadata> blobStoreApis = new HashSet<ApiMetadata>();

    private final List<ComputeProviderOrApiListener> computeOrApiListeners = new LinkedList<ComputeProviderOrApiListener>();
    private final List<BlobStoreProviderOrApiListener> blobStoreOrApiListeners = new LinkedList<BlobStoreProviderOrApiListener>();


    private BundleContext bundleContext;

    public MetadataBundleListener() {
    }

    public void init() {
        bundleContext.addBundleListener(this);
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getState() == Bundle.ACTIVE) {
                ProviderMetadata providerMetadata = getProviderMetadata(bundle);
                if (providerMetadata != null) {
                    notifyInstallation(providerMetadata);
                    providerMetadataMap.put(bundle.getBundleId(), providerMetadata);
                }

                ApiMetadata apiMetadata = getApiMetadata(bundle);
                if (apiMetadata != null) {
                    notifyInstallation(apiMetadata);
                    apiMetadataMap.put(bundle.getBundleId(), apiMetadata);
                }
            }
        }
    }

    public void destroy() {
        bundleContext.removeBundleListener(this);
    }


    @Override
    public void bundleChanged(BundleEvent event) {
        ProviderMetadata providerMetadata;
        ApiMetadata apiMetadata;
        switch (event.getType()) {
            case BundleEvent.STARTED:
                providerMetadata = getProviderMetadata(event.getBundle());
                apiMetadata = getApiMetadata(event.getBundle());
                if (providerMetadata != null) {
                    notifyInstallation(providerMetadata);
                    providerMetadataMap.put(event.getBundle().getBundleId(), providerMetadata);
                }
                if (apiMetadata != null) {
                    notifyInstallation(apiMetadata);
                    apiMetadataMap.put(event.getBundle().getBundleId(), apiMetadata);
                }
                break;
            case BundleEvent.STOPPING:
            case BundleEvent.STOPPED:
                providerMetadata = providerMetadataMap.remove(event.getBundle().getBundleId());
                apiMetadata = apiMetadataMap.remove(event.getBundle().getBundleId());
                if (providerMetadata != null) {
                    notifyRemoval(providerMetadata);
                }
                if (apiMetadata != null) {
                    notifyRemoval(apiMetadata);
                }
                break;
        }
    }

    /**
     * Creates an instance of {@link ProviderMetadata} from the {@link Bundle}.
     *
     * @param bundle
     * @return
     */
    public ProviderMetadata getProviderMetadata(Bundle bundle) {
        ProviderMetadata metadata = null;
        String className = getProviderMetadataClassName(bundle);
        if (className != null && !className.isEmpty()) {
            try {
                Class<? extends ProviderMetadata> providerMetadataClass = bundle.loadClass(className);
                metadata = providerMetadataClass.newInstance();
            } catch (ClassNotFoundException e) {
                // ignore
            } catch (InstantiationException e) {
                // ignore
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
        return metadata;
    }

    /**
     * Creates an instance of {@link ApiMetadata} from the {@link Bundle}.
     *
     * @param bundle
     * @return
     */
    public ApiMetadata getApiMetadata(Bundle bundle) {
        ApiMetadata metadata = null;
        String className = getApiMetadataClassName(bundle);
        if (className != null && !className.isEmpty()) {
            try {
                Class<? extends ApiMetadata> apiMetadataClass = bundle.loadClass(className);
                metadata = apiMetadataClass.newInstance();
            } catch (ClassNotFoundException e) {
                // ignore
            } catch (InstantiationException e) {
                // ignore
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
        return metadata;
    }


    public String getMetadataClassName(Bundle bundle, String pathToMetadata) {
        URL resource = bundle.getEntry(pathToMetadata);
        InputStream is = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        StringBuilder sb = new StringBuilder();

        try {
            is = resource.openStream();
            reader = new InputStreamReader(is, "UTF-8");
            bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Throwable e) {
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Throwable e) {
            }
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (Throwable e) {
            }
            try {
                is.close();
            } catch (Throwable e) {
            }

        }
        return sb.toString().trim();
    }

    /**
     * Retrieves the {@link ProviderMetadata} class name for the bundle if it exists.
     *
     * @param bundle
     * @return
     */
    public String getProviderMetadataClassName(Bundle bundle) {
        return getMetadataClassName(bundle, "/META-INF/services/org.jclouds.providers.ProviderMetadata");
    }

    /**
     * Retrieves the {@link ProviderMetadata} class name for the bundle if it exists.
     *
     * @param bundle
     * @return
     */
    public String getApiMetadataClassName(Bundle bundle) {
        return getMetadataClassName(bundle, "/META-INF/services/org.jclouds.apis.ApiMetadata");
    }


    /**
     * Notifies {@link ProviderOrApiListener}s with the installation of a {@link ProviderMetadata}.
     *
     * @param metadata
     */
    public void notifyInstallation(ProviderMetadata metadata) {
        if (metadata != null) {
            if (ProviderPredicates.viewableAs(TypeToken.of(ComputeServiceContext.class)).apply(metadata)) {
                computeProviders.add(metadata);
                for (ProviderOrApiListener providerOrApiListener : computeOrApiListeners) {
                    providerOrApiListener.providerInstalled(metadata);
                }
            } else if (ProviderPredicates.viewableAs(TypeToken.of(BlobStoreContext.class)).apply(metadata)) {
                blobStoreProviders.add(metadata);
                for (ProviderOrApiListener providerOrApiListener : blobStoreOrApiListeners) {
                    providerOrApiListener.providerInstalled(metadata);
                }
            }
        }
    }

    /**
     * Notifies {@link ProviderOrApiListener}s with the installation of a {@link ApiMetadata}.
     *
     * @param metadata
     */
    public void notifyInstallation(ApiMetadata metadata) {
        if (metadata != null) {
            if (ApiPredicates.viewableAs(TypeToken.of(ComputeServiceContext.class)).apply(metadata)) {
                computeApis.add(metadata);
                for (ProviderOrApiListener providerOrApiListener : computeOrApiListeners) {
                    providerOrApiListener.apiInstalled(metadata);
                }
            } else if (ApiPredicates.viewableAs(TypeToken.of(BlobStoreContext.class)).apply(metadata)) {
                blobStoreApis.add(metadata);
                for (ProviderOrApiListener providerOrApiListener : blobStoreOrApiListeners) {
                    providerOrApiListener.apiInstalled(metadata);
                }
            }
        }
    }

    /**
     * Notifies {@link ProviderOrApiListener}s with the removal of {@link ProviderMetadata}.
     *
     * @param metadata
     */
    private void notifyRemoval(ProviderMetadata metadata) {
        if (metadata != null) {
            if (ProviderPredicates.viewableAs(TypeToken.of(ComputeServiceContext.class)).apply(metadata)) {
                computeProviders.remove(metadata);
                for (ProviderOrApiListener providerOrApiListener : computeOrApiListeners) {
                    providerOrApiListener.providerUninstalled(metadata);
                }
            } else if (ProviderPredicates.viewableAs(TypeToken.of(BlobStoreContext.class)).apply(metadata)) {
                blobStoreProviders.remove(metadata);
                for (ProviderOrApiListener providerOrApiListener : blobStoreOrApiListeners) {
                    providerOrApiListener.providerUninstalled(metadata);
                }
            }
        }
    }

    /**
     * Notifies {@link ProviderOrApiListener}s with the removal of {@link ApiMetadata}.
     *
     * @param metadata
     */
    private void notifyRemoval(ApiMetadata metadata) {
        if (metadata != null) {
            if (ApiPredicates.viewableAs(TypeToken.of(ComputeServiceContext.class)).apply(metadata)) {
                computeApis.remove(metadata);
                for (ProviderOrApiListener providerOrApiListener : computeOrApiListeners) {
                    providerOrApiListener.apiUninstalled(metadata);
                }
            } else if (ApiPredicates.viewableAs(TypeToken.of(BlobStoreContext.class)).apply(metadata)) {
                blobStoreApis.remove(metadata);
                for (ProviderOrApiListener providerOrApiListener : blobStoreOrApiListeners) {
                    providerOrApiListener.apiUninstalled(metadata);
                }
            }
        }
    }


    /**
     * Registers a {@link org.jclouds.karaf.core.ComputeProviderOrApiListener}
     *
     * @param providerOrApiListener
     */
    public void registerComputeListener(ComputeProviderOrApiListener providerOrApiListener) {
        this.computeOrApiListeners.add(providerOrApiListener);
        for (ProviderMetadata provider : computeProviders) {
            providerOrApiListener.providerInstalled(provider);
        }
        for (ApiMetadata api : computeApis) {
            providerOrApiListener.apiInstalled(api);
        }

    }

    public void unregisterComputeListener(ComputeProviderOrApiListener providerOrApiListener) {
        this.computeOrApiListeners.remove(providerOrApiListener);
        for (ProviderMetadata provider : computeProviders) {
            providerOrApiListener.providerUninstalled(provider);
        }
        for (ApiMetadata api : computeApis) {
            providerOrApiListener.apiInstalled(api);
        }
    }

    /**
     * Registers a {@link org.jclouds.karaf.core.ComputeProviderOrApiListener}
     *
     * @param providerOrApiListener
     */
    public void registerBlobStoreListener(BlobStoreProviderOrApiListener providerOrApiListener) {
        this.blobStoreOrApiListeners.add(providerOrApiListener);
        for (ProviderMetadata provider : blobStoreProviders) {
            providerOrApiListener.providerInstalled(provider);
        }
        for (ApiMetadata api : blobStoreApis) {
            providerOrApiListener.apiInstalled(api);
        }
    }

    public void unregisterBlobStoreListener(BlobStoreProviderOrApiListener providerOrApiListener) {
        this.blobStoreOrApiListeners.remove(providerOrApiListener);
        for (ProviderMetadata provider : blobStoreProviders) {
            providerOrApiListener.providerUninstalled(provider);
        }
        for (ApiMetadata api : computeApis) {
            providerOrApiListener.apiUninstalled(api);
        }
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
