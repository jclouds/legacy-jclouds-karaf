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
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.karaf.core.BlobStoreProviderListener;
import org.jclouds.karaf.core.ComputeProviderListener;
import org.jclouds.karaf.core.ProviderListener;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.ProviderPredicates;
import org.osgi.framework.*;
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

    private Set<ProviderMetadata> computeProviders = new HashSet<ProviderMetadata>();
    private Set<ProviderMetadata> blobStoreProviders = new HashSet<ProviderMetadata>();

    private final List<ComputeProviderListener> computeListeners = new LinkedList<ComputeProviderListener>();
    private final List<BlobStoreProviderListener> blobStoreListeners = new LinkedList<BlobStoreProviderListener>();


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
            if (ProviderPredicates.contextWrappableAs(TypeToken.of(ComputeServiceContext.class)).apply(metadata)){
                computeProviders.add(metadata);
                for (ProviderListener providerListener : computeListeners) {
                    providerListener.providerInstalled(metadata);
                }
            } else if (ProviderPredicates.contextWrappableAs(TypeToken.of(BlobStoreContext.class)).apply(metadata)){
                blobStoreProviders.add(metadata);
                for (ProviderListener providerListener : blobStoreListeners) {
                    providerListener.providerInstalled(metadata);
                }
            }
        }
    }

    private void removeListeners(ProviderMetadata metadata) {
        if (metadata != null) {
            if (ProviderPredicates.contextWrappableAs(TypeToken.of(ComputeServiceContext.class)).apply(metadata)){
                computeProviders.remove(metadata);
                for (ProviderListener providerListener : computeListeners) {
                    providerListener.providerUninstalled(metadata);
                }
            } else if (ProviderPredicates.contextWrappableAs(TypeToken.of(BlobStoreContext.class)).apply(metadata)){
                blobStoreProviders.remove(metadata);
                for (ProviderListener providerListener : blobStoreListeners) {
                    providerListener.providerUninstalled(metadata);
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
        ProviderMetadata metadata;
        switch (event.getType()) {
          case BundleEvent.STARTED:
              metadata = getProviderMetadata(event.getBundle());
              notifyListeners(metadata);
//        TODO: Uninstall is not handled directly yet because ProviderMetadata can't be retrieved
//        from the bundle after it has been closed. Will handle this soon.
/*
          case BundleEvent.STOPPED:
              metadata = getProviderMetadata(event.getBundle());
              removeListeners(metadata);
*/
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
        URL resource = bundle.getEntry("/META-INF/services/org.jclouds.providers.ProviderMetadata");
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
        for (ProviderMetadata provider : computeProviders) {
            providerListener.providerInstalled(provider);
        }
    }

    public void unregisterComputeListener(ComputeProviderListener providerListener) {
        this.computeListeners.remove(providerListener);
        for (ProviderMetadata provider : computeProviders) {
            providerListener.providerUninstalled(provider);
        }
    }

    /**
     * Registers a {@link ComputeProviderListener}
     * @param providerListener
     */
    public void registerBlobStoreListener(BlobStoreProviderListener providerListener) {
        this.blobStoreListeners.add(providerListener);
        for (ProviderMetadata provider : blobStoreProviders) {
            providerListener.providerInstalled(provider);
        }
    }

    public void unregisterBlobStoreListener(BlobStoreProviderListener providerListener) {
        this.blobStoreListeners.remove(providerListener);
        for (ProviderMetadata provider : blobStoreProviders) {
            providerListener.providerUninstalled(provider);
        }
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
