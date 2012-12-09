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

package org.jclouds.karaf.commands.compute;

import static org.jclouds.compute.options.RunScriptOptions.Builder.overrideLoginCredentials;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.felix.gogo.commands.Option;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.karaf.utils.EnvHelper;

import com.google.common.base.Predicate;
import org.jclouds.scriptbuilder.domain.Statement;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public abstract class RunScriptBase extends ComputeCommandWithOptions {

   @Option(name = "-u", aliases = "--user", description = "The user that will run the script.", required = false, multiValued = false)
   protected String user;

   @Option(name = "-p", aliases = "--password", description = "Optional password for the user to run the script.", required = false, multiValued = false)
   protected String password;


   public abstract String getId();

   public abstract String getGroup();

   public abstract String getScript();

   public abstract Statement getStatement();

   public abstract boolean runAsRoot();

   @Override
   protected Object doExecute() throws Exception {
      ComputeService service = null;
      try {
         service = getComputeService();
      } catch (Throwable t) {
         System.err.println(t.getMessage());
         return null;
      }
      Set<? extends NodeMetadata> nodeMetaDataSet = service.listNodesDetailsMatching(getComputeFilter());
      if (nodeMetaDataSet != null && !nodeMetaDataSet.isEmpty()) {
         NodeMetadata nodeMetadata = nodeMetaDataSet.toArray(new NodeMetadata[0])[0];

         // If we have multiple nodes we just want the headers, but not full details.
         printNodeInfo(service, nodeMetaDataSet, nodeMetaDataSet.size() == 1, System.out);

         LoginCredentials credentials = nodeMetadata.getCredentials();

         user = EnvHelper.getUser(user);
         password = EnvHelper.getPassword(password);

         if (user != null) {
            LoginCredentials.Builder loginBuilder;
            if (credentials == null) {
               loginBuilder = LoginCredentials.builder();
            } else {
               loginBuilder = credentials.toBuilder();
            }
            if (password != null) {
               credentials = loginBuilder.user(user).password(password).build();
            } else {
               credentials = loginBuilder.user(user).build();
            }
         }

         Map<? extends NodeMetadata, ExecResponse> responseMap = null;

          if (getScript() != null) {
              responseMap = service.runScriptOnNodesMatching(getNodeFilter(),
                      getScript(), overrideLoginCredentials(credentials).runAsRoot(runAsRoot()));
          } else if (getStatement() != null) {
             responseMap = service.runScriptOnNodesMatching(getNodeFilter(),
                      getStatement(), overrideLoginCredentials(credentials).runAsRoot(runAsRoot()));
          }

          for (Map.Entry<? extends NodeMetadata, ExecResponse> entry : responseMap.entrySet()) {
            ExecResponse response = entry.getValue();
            NodeMetadata node = entry.getKey();
            System.out.println("");
            if (response.getOutput() != null && !response.getOutput().isEmpty()) {
               System.out.println("Node:" + node.getId() + " Output:" + response.getOutput());
            }

            if (response.getError() != null && !response.getError().isEmpty()) {
               System.out.println("Node:" + node.getId() + " Error:" + response.getError());
            }
         }
      }

      return null;
   }

   public Predicate<NodeMetadata> getNodeFilter() {
      return new Predicate<NodeMetadata>() {
         @Override
         public boolean apply(@Nullable NodeMetadata input) {
            boolean applies = true;
            if (!input.getStatus().equals(NodeMetadata.Status.RUNNING)) {
               return false;
            }

            if (getId() != null && !getId().isEmpty() && !input.getId().equals(getId())) {
               applies = false;
            }
            if (getGroup() != null && !getGroup().isEmpty() && !input.getGroup().equals(getGroup())) {
               applies = false;
            }
            return applies;
         }
      };
   }

   public Predicate<ComputeMetadata> getComputeFilter() {
      return new Predicate<ComputeMetadata>() {
         @Override
         public boolean apply(@Nullable ComputeMetadata input) {
            boolean applies = true;
            if (input instanceof NodeMetadata) {

               if (!((NodeMetadata) input).getStatus().equals(NodeMetadata.Status.RUNNING)) {
                  return false;
               }

               if (getId() != null && !getId().isEmpty() && !getId().equals(input.getId())) {
                  applies = false;
               }

               if (getGroup() != null && !getGroup().isEmpty() && getGroup().equals(((NodeMetadata) input).getGroup())) {
                  applies = false;
               }
            }
            return applies;
         }
      };
   }
}
