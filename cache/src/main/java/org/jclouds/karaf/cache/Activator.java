package org.jclouds.karaf.cache;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.compute.ComputeService;
import org.jclouds.karaf.cache.utils.CacheUtils;
import org.jclouds.karaf.recipe.RecipeProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Properties;

public class Activator implements BundleActivator {

     private ServiceTracker computeServiceTracker;
     private ServiceTracker computeCacheableTracker;

     private ServiceTracker blobStoreTracker;
     private ServiceTracker blobStoreCacheableTracker;

     private ServiceTracker recipeProviderTracker;
     private ServiceTracker recipeCacheableTracker;

     private ServiceRegistration cacheProviderRegistration;

     private final CacheManager<ComputeService> computeCacheManager = new CacheManager<ComputeService>();
     private final CacheManager<BlobStore> blobCacheManager = new CacheManager<BlobStore>();
     private final CacheManager<RecipeProvider> recipeCacheManager = new CacheManager<RecipeProvider>();



    /**
     * Called when this bundle is started so the Framework can perform the
     * bundle-specific activities necessary to start this bundle. This method
     * can be used to register services or to allocate any resources that this
     * bundle needs.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being started.
     * @throws Exception If this method throws an exception, this
     *                   bundle is marked as stopped and the Framework will remove this
     *                   bundle's listeners, unregister all services registered by this
     *                   bundle, and release all services used by this bundle.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        CacheProvider cacheProvider = new BasicCacheProvider();
        cacheProviderRegistration = context.registerService(CacheProvider.class.getName(), cacheProvider, new Properties());

        computeServiceTracker = CacheUtils.createServiceCacheTracker(context, ComputeService.class, computeCacheManager);
        computeCacheableTracker = CacheUtils.createCacheableTracker(context, "jclouds.computeservice",computeCacheManager);

        blobStoreTracker = CacheUtils.createServiceCacheTracker(context, BlobStore.class, blobCacheManager);
        blobStoreCacheableTracker = CacheUtils.createCacheableTracker(context, "jclouds.blobstore",blobCacheManager);

        recipeProviderTracker = CacheUtils.createServiceCacheTracker(context, RecipeProvider.class, recipeCacheManager);
        recipeCacheableTracker = CacheUtils.createCacheableTracker(context, "jclouds.recipeprovider", recipeCacheManager);

        computeServiceTracker.open();
        computeCacheableTracker.open();

        blobStoreTracker.open();
        blobStoreCacheableTracker.open();

        recipeProviderTracker.open();
        recipeCacheableTracker.open();

        computeCacheManager.init();
        blobCacheManager.init();
        recipeCacheManager.init();

    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle. In general, this
     * method should undo the work that the <code>BundleActivator.start</code>
     * method started. There should be no active threads that were started by
     * this bundle when this bundle returns. A stopped bundle must not call any
     * Framework objects.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the
     *                   bundle is still marked as stopped, and the Framework will remove
     *                   the bundle's listeners, unregister all services registered by the
     *                   bundle, and release all services used by the bundle.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        computeCacheManager.destroy();
        blobCacheManager.destroy();
        recipeCacheManager.destroy();

        if (cacheProviderRegistration != null) {
            cacheProviderRegistration.unregister();
        }
        if (computeServiceTracker != null) {
            computeServiceTracker.close();
        }
        if (computeCacheableTracker != null) {
            computeCacheableTracker.close();
        }
        if (blobStoreTracker != null) {
            blobStoreTracker.close();
        }
        if (blobStoreCacheableTracker != null) {
            blobStoreCacheableTracker.close();
        }
        if (recipeProviderTracker != null) {
            recipeProviderTracker.close();
        }
        if (recipeCacheableTracker != null) {
            recipeCacheableTracker.close();
        }
    }
}
