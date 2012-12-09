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

package org.jclouds.karaf.commands.compute.completer;

import org.apache.karaf.shell.console.Completer;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.karaf.core.Constants;
import org.jclouds.karaf.utils.ServiceHelper;

import java.util.Set;

public class GroupCompleter extends ComputeCompleterSupport implements Completer {

   public void init() {
      cache = cacheProvider.getProviderCacheForType(Constants.GROUP);
   }

   @Override
   public void updateOnAdded(ComputeService computeService) {
      if (computeService != null) {
         Set<? extends ComputeMetadata> computeMetadatas = computeService.listNodes();
         if (computeMetadatas != null) {
            for (ComputeMetadata compute : computeMetadatas) {
               NodeMetadata node = (NodeMetadata) compute;
               if (apply(node)) {
                 for (String cacheKey : ServiceHelper.findCacheKeysForService(computeService)) {
                   cache.put(cacheKey, node.getGroup());
                 }
               }
            }
         }
      }
   }

   public boolean apply(NodeMetadata node) {
      return true;
   }
}
