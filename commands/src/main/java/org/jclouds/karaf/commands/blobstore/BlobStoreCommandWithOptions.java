/**
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
package org.jclouds.karaf.commands.blobstore;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import com.google.inject.Module;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.AbstractAction;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.util.BlobStoreUtils;
import org.jclouds.karaf.cache.BasicCacheProvider;
import org.jclouds.karaf.cache.CacheProvider;
import org.jclouds.karaf.utils.EnvHelper;
import org.jclouds.karaf.utils.blobstore.BlobStoreHelper;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.rest.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author iocanel
 */
public abstract class BlobStoreCommandWithOptions extends BlobStoreCommandBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreCommandWithOptions.class);

    @Option(name = "--provider", description = "The provider to use.")
    protected String provider;

    @Option(name = "--api", description = "The api to use.")
    protected String api;

    @Option(name = "--identity", description = "The identity to use for creating a blob store.")
    protected String identity;

    @Option(name = "--credential", description = "The credential to use for a blob store.")
    protected String credential;

    @Option(name = "--endpoint", description = "The endpoint to use for a blob store.")
    protected String endpoint;


    @Override
    public Object execute(CommandSession session) throws Exception {
        try {
            this.session = session;
            return doExecute();
        } catch (AuthorizationException ex) {
            System.err.println("Authorization error. Please make sure you provided valid identity and credential.");
            return null;
        }
    }

    protected List<BlobStore> getBlobStoreServices() {
        if (provider == null && api == null) {
            return super.getBlobStoreServices();
        } else {
            try {
                return Collections.singletonList(getBlobStore());
            } catch (Throwable t) {
                return Collections.emptyList();
            }
        }
    }

    protected BlobStore getBlobStore() {
        if (getBlobStoreServices() != null && getBlobStoreServices().size() == 1) {
            return getBlobStoreServices().get(0);
        }
        BlobStore blobStore = null;
        String providerValue = EnvHelper.getBlobStoreProvider(provider);
        String apiValue = EnvHelper.getBlobStoreApi(api);
        String identityValue = EnvHelper.getBlobStoreIdentity(identity);
        String credentialValue = EnvHelper.getBlobStoreCredential(credential);
        String endpointValue = EnvHelper.getBlobStoreEndpoint(endpoint);

        boolean canCreateService = (!Strings.isNullOrEmpty(providerValue) || !Strings.isNullOrEmpty(apiValue))
                && !Strings.isNullOrEmpty(identityValue) && !Strings.isNullOrEmpty(credentialValue);

        String providerOrApiValue = !Strings.isNullOrEmpty(providerValue) ? providerValue : apiValue;

        try {
            blobStore = BlobStoreHelper.getBlobStore(providerOrApiValue, getBlobStoreServices());
        } catch (Throwable t) {
            if (!canCreateService) {
                StringBuilder sb = new StringBuilder();
                sb.append("Insufficient information to create blobstore service:").append("\n");
                if (providerOrApiValue == null) {
                    sb.append("Missing provider or api. Please specify either using the --provider / --api options, or the JCLOUDS_BLOBSTORE_PROVIDER / JCLOUDS_BLOBSTORE_API environmental variables.").append("\n");
                }
                if (identityValue == null) {
                    sb.append("Missing identity. Please specify either using the --identity option, or the JCLOUDS_BLOBSTORE_IDENTITY environmental variable.").append("\n");
                }
                if (credentialValue == null) {
                    sb.append("Missing credential. Please specify either using the --credential option, or the JCLOUDS_BLOBSTORE_CREDENTIAL environmental variable.").append("\n");
                }
                throw new RuntimeException(sb.toString());
            }
        }
        if (blobStore == null && canCreateService) {
            try {
                ContextBuilder builder = ContextBuilder.newBuilder(providerOrApiValue).credentials(identityValue, credentialValue).modules(ImmutableSet.<Module>of(new Log4JLoggingModule()));
                if (!Strings.isNullOrEmpty(endpointValue)) {
                    builder = builder.endpoint(endpoint);
                }
                BlobStoreContext context = builder.build(BlobStoreContext.class);
                blobStore = context.getBlobStore();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create service:" + ex.getMessage());
            }
        }
        return blobStore;
    }
}
