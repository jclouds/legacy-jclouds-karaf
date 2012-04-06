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

import org.jclouds.karaf.core.BlobStoreProviderListener;
import org.jclouds.karaf.core.ComputeProviderListener;
import org.jclouds.karaf.core.ProviderListener;
import org.jclouds.providers.ProviderMetadata;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ProviderBundleListener implements BundleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderBundleListener.class);

    private Set<String> computeProviders = new HashSet<String>();
    private Set<String> blobStoreProviders = new HashSet<String>();

    private final List<ComputeProviderListener> computeListeners = new LinkedList<ComputeProviderListener>();
    private final List<BlobStoreProviderListener> blboStoreListeners = new LinkedList<BlobStoreProviderListener>();


    private BundleContext bundleContext;

    public ProviderBundleListener() {
    }

    public void init() {
        bundleContext.addBundleListener(this);
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getState() == Bundle.ACTIVE) {
                ProviderMetadata metadata = getProviderMetadata(bundle);
                notifyListeners(metadata);
            }
        }
    }

    public void destroy() {
        bundleContext.removeBundleListener(this);
    }

    public void notifyListeners(ProviderMetadata metadata) {
        if (metadata != null) {
            String id = metadata.getId();
            if (metadata.getType().equals(ProviderMetadata.COMPUTE_TYPE)) {
                computeProviders.add(metadata.getId());
                for (ProviderListener providerListener : computeListeners) {
                    providerListener.providerInstalled(id);
                }
            }
            if (metadata.getType().equals(ProviderMetadata.BLOBSTORE_TYPE)) {
                blobStoreProviders.add(metadata.getId());
                for (ProviderListener providerListener : blboStoreListeners) {
                    providerListener.providerInstalled(id);
                }
            }
        }
    }

    /**
     * Receives notification that a bundle has had a lifecycle change.
     * Whenever a bundle is installed it checks for available {@link ProviderMetadata} and notifies {@link ProviderListener}
     * if the metadata are found.
     *
     * @param event The <code>BundleEvent</code>.
     */
    @Override
    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case BundleEvent.STARTED:
            ProviderMetadata metadata = getProviderMetadata(event.getBundle());
            notifyListeners(metadata);
            break;
        }
    }


    /**
     * Creates an instance of {@link ProviderMetadata} from the {@link Bundle}.
     * @param bundle
     * @return
     */
    public ProviderMetadata getProviderMetadata(Bundle bundle) {
        ProviderMetadata metadata = null;
        String className = getProviderMetadataClassName(bundle);
        if (className != null && !className.isEmpty()) {
            try {
                Class<? extends ProviderMetadata> provideClass = bundle.loadClass(className);
                metadata = provideClass.newInstance();
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Could not load provider metadata class.", e);
            } catch (InstantiationException e) {
                LOGGER.warn("Could not instantiate metadata class:" + className, e);
            } catch (IllegalAccessException e) {
                LOGGER.warn("Could not instantiate metadata class:" + className, e);
            }
        }
        return metadata;
    }

    /**
     * Retrieves the {@link ProviderMetadata} class name for the bundle if it exists.
     * @param bundle
     * @return
     */
    public String getProviderMetadataClassName(Bundle bundle) {
        URL resource = bundle.getResource("/META-INF/services/org.jclouds.providers.ProviderMetadata");
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
     * Registers a {@link ComputeProviderListener}
     * @param providerListener
     */
    public void registerComputeListener(ComputeProviderListener providerListener) {
        this.computeListeners.add(providerListener);
        for (String provider : computeProviders) {
            providerListener.providerInstalled(provider);
        }
    }

    public void unregisterComputeListener(ComputeProviderListener providerListener) {
        this.computeListeners.remove(providerListener);
    }

    /**
     * Registers a {@link ComputeProviderListener}
     * @param providerListener
     */
    public void registerBlobStoreListener(BlobStoreProviderListener providerListener) {
        this.blboStoreListeners.add(providerListener);
        for (String provider : blobStoreProviders) {
            providerListener.providerInstalled(provider);
        }
    }

    public void unregisterBlobStoreListener(BlobStoreProviderListener providerListener) {
        this.blboStoreListeners.remove(providerListener);
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
