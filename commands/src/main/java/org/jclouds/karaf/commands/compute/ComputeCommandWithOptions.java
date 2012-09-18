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

import java.util.Collections;
import java.util.List;

import org.apache.felix.gogo.commands.Option;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.karaf.services.modules.PropertiesCredentialStore;
import org.jclouds.karaf.utils.EnvHelper;
import org.jclouds.karaf.utils.compute.ComputeHelper;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public abstract class ComputeCommandWithOptions extends ComputeCommandBase {

   @Option(name = "--id", description = "The service id. Used to distinct between multiple service of the same provider/api. Only ")
   protected String id;

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
         return computeServices;
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
      if ((id == null && provider == null && api == null) &&(computeServices != null && computeServices.size() == 1)) {
         return computeServices.get(0);
      }

      ComputeService computeService = null;
      String providerValue = EnvHelper.getComputeProvider(provider);
      String apiValue = EnvHelper.getComputeApi(api);
      String identityValue = EnvHelper.getComputeIdentity(identity);
      String credentialValue = EnvHelper.getComputeCredential(credential);
      String endpointValue = EnvHelper.getComputeEndpoint(endpoint);
      boolean serviceIdProvided = !Strings.isNullOrEmpty(id);
      boolean canCreateService = (!Strings.isNullOrEmpty(providerValue) || !Strings.isNullOrEmpty(apiValue))
               && !Strings.isNullOrEmpty(identityValue) && !Strings.isNullOrEmpty(credentialValue);

      String providerOrApiValue = !Strings.isNullOrEmpty(providerValue) ? providerValue : apiValue;

      try {
         computeService = ComputeHelper.getComputeService(id, providerOrApiValue, computeServices);
      } catch (Throwable t) {
         if (serviceIdProvided) {
           throw new RuntimeException("Could not find compute service with id:" + id);
         } else if (!canCreateService) {
            StringBuilder sb = new StringBuilder();
            sb.append("Insufficient information to create compute service:").append("\n");
            if (providerOrApiValue == null) {
               sb.append(
                        "Missing provider or api. Please specify either using the --provider / --api options, or the JCLOUDS_COMPUTE_PROVIDER / JCLOUDS_COMPUTE_API environmental variables.")
                        .append("\n");
            }
            if (identityValue == null) {
               sb.append(
                        "Missing identity. Please specify either using the --identity option, or the JCLOUDS_COMPUTE_IDENTITY environmental variable.")
                        .append("\n");
            }
            if (credentialValue == null) {
               sb.append(
                        "Missing credential. Please specify either using the --credential option, or the JCLOUDS_COMPUTE_CREDENTIAL environmental variable.")
                        .append("\n");
            }
            throw new RuntimeException(sb.toString());
         }
      }

      if (computeService == null && canCreateService) {
         try {
            // This may run in or inside OSGi, so we choose explicitly set a credential store which
            // should be compatible with both.
            ContextBuilder builder = ContextBuilder
                     .newBuilder(providerOrApiValue)
                     .credentials(identityValue, credentialValue)
                     .modules(ImmutableSet.<Module> of(new SshjSshClientModule(), new Log4JLoggingModule(),
                              new PropertiesCredentialStore()));
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
