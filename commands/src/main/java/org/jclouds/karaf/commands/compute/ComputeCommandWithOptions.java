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
package org.jclouds.karaf.commands.compute;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.AbstractAction;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.compute.domain.Processor;
import org.jclouds.domain.Location;
import org.jclouds.karaf.cache.CacheProvider;
import org.jclouds.karaf.cache.BasicCacheProvider;
import org.jclouds.karaf.services.modules.PropertiesCredentialStore;
import org.jclouds.karaf.utils.EnvHelper;
import org.jclouds.karaf.utils.compute.ComputeHelper;
import org.jclouds.logging.config.NullLoggingModule;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public abstract class ComputeCommandWithOptions extends ComputeCommandBase {

    @Option(name = "--provider", description = "The provider or use.")
    protected String provider;

    @Option(name = "--api", description = "The api or use.")
    protected String api;

    @Option(name = "--identity", description = "The identity to use for creating a compute service.")
    protected String identity;

    @Option(name = "--credential", description = "The credential to use for a compute service.")
    protected String credential;

    @Option(name = "--endpoint", description = "The endpoint to use for a compute service.")
    protected String endpoint;


    @Override
    public List<ComputeService> getComputeServices() {
        if (provider == null && api == null) {
            return super.getComputeServices();
        } else {
            try {
                ComputeService service = getComputeService();
                return Collections.singletonList(service);
            } catch (Throwable t) {
                return Collections.emptyList();
            }
        }
    }

    protected ComputeService getComputeService() {
        if (getComputeServices() != null && getComputeServices().size() == 1) {
            return getComputeServices().get(0);
        }
        ComputeService computeService = null;
        String providerValue = EnvHelper.getComputeProvider(provider);
        String apiValue = EnvHelper.getComputeApi(api);
        String identityValue = EnvHelper.getComputeIdentity(identity);
        String credentialValue = EnvHelper.getComputeCredential(credential);
        String endpointValue = EnvHelper.getComputeEndpoint(endpoint);
        boolean canCreateService = (!Strings.isNullOrEmpty(providerValue) || !Strings.isNullOrEmpty(apiValue))
                && !Strings.isNullOrEmpty(identityValue) && !Strings.isNullOrEmpty(credentialValue);

        String providerOrApiValue = !Strings.isNullOrEmpty(providerValue) ? providerValue : apiValue;

        try {
            computeService = ComputeHelper.getComputeService(providerOrApiValue, getComputeServices());
        } catch (Throwable t) {
            if (!canCreateService) {
                StringBuilder sb = new StringBuilder();
                sb.append("Insufficient information to create compute service:").append("\n");
                if (providerOrApiValue == null) {
                    sb.append("Missing provider or api. Please specify either using the --provider / --api options, or the JCLOUDS_COMPUTE_PROVIDER / JCLOUDS_COMPUTE_API environmental variables.").append("\n");
                }
                if (identityValue == null) {
                    sb.append("Missing identity. Please specify either using the --identity option, or the JCLOUDS_COMPUTE_IDENTITY environmental variable.").append("\n");
                }
                if (credentialValue == null) {
                    sb.append("Missing credential. Please specify either using the --credential option, or the JCLOUDS_COMPUTE_CREDENTIAL environmental variable.").append("\n");
                }
                throw new RuntimeException(sb.toString());
            }
        }

        if (computeService == null && canCreateService) {
            try {
                //This may run in or inside OSGi, so we choose explicitly set a credential store which should be compatible with both.
                ContextBuilder builder = ContextBuilder.newBuilder(providerOrApiValue).credentials(identityValue, credentialValue).modules(ImmutableSet.<Module>of(new SshjSshClientModule(), new Log4JLoggingModule(), new PropertiesCredentialStore()));
                if (!Strings.isNullOrEmpty(endpointValue)) {
                    builder = builder.endpoint(endpointValue);
                }
                computeService = builder.build(ComputeServiceContext.class).getComputeService();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create service:" + ex.getMessage());
            }
        }
        return computeService;
    }
}
