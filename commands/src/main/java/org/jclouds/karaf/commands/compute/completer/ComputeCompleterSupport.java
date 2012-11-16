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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.ArgumentCompleter;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.apache.karaf.shell.console.jline.CommandSessionHolder;
import org.jclouds.compute.ComputeService;
import org.jclouds.karaf.cache.CacheProvider;
import org.jclouds.karaf.cache.Cacheable;

import com.google.common.collect.Multimap;

public abstract class ComputeCompleterSupport implements Completer, Cacheable<ComputeService> {

   private static final String NAME_OPTION = "--name";
   private static final String PROVIDER_OPTION = "--provider";
   private static final String API_OPTION = "--api";

   protected final StringsCompleter delegate = new StringsCompleter();
   protected CacheProvider cacheProvider;
   protected Multimap<String, String> cache;

   @Override
   public int complete(String buffer, int cursor, List<String> candidates) {
     CommandSession commandSession = CommandSessionHolder.getSession();
     ArgumentCompleter.ArgumentList list = (ArgumentCompleter.ArgumentList) commandSession.get(ArgumentCompleter.ARGUMENTS_LIST);
     delegate.getStrings().clear();

     if (list != null) {
        String contextName = extractContextName(list.getArguments());
       String providerOrApi = extractProviderOrApiFromArguments(list.getArguments());
       Collection<String> values;

       if (contextName != null && cache.containsKey(contextName)) {
         values = cache.get(contextName);
       } else if (providerOrApi != null && cache.containsKey(providerOrApi)) {
         values = cache.get(providerOrApi);
       } else {
         values = cache.values();
       }

       for (String item : values) {
         if (buffer == null || item.startsWith(buffer)) {
           delegate.getStrings().add(item);
         }
       }
     }

     return delegate.complete(buffer, cursor, candidates);
   }

  /**
   * Parses the arguments and extracts the service id.
   * @param args
   * @return
   */
  private String extractContextName(String... args) {
    String id = null;
    if (args != null && args.length > 0) {
      List<String> arguments = Arrays.asList(args);
      if (arguments.contains(NAME_OPTION)) {
        int index = arguments.indexOf(NAME_OPTION);
        if (arguments.size() > index) {
          return arguments.get(index + 1);
        }
      }
    }
    return id;
  }

    /**
     * Parses the arguments and extracts the provider or api option value
     * @param args
     * @return
     */
    private String extractProviderOrApiFromArguments(String... args) {
        String id = null;
        if (args != null && args.length > 0) {
            List<String> arguments = Arrays.asList(args);
            if (arguments.contains(NAME_OPTION)) {
               int index = arguments.indexOf(NAME_OPTION);
               if (arguments.size() > index) {
                  return arguments.get(index + 1);
               }
            }
            if (arguments.contains(PROVIDER_OPTION)) {
                int index = arguments.indexOf(PROVIDER_OPTION);
                if (arguments.size() > index) {
                    return arguments.get(index + 1);
                }
            } else if (arguments.contains(API_OPTION)) {
                int index = arguments.indexOf(API_OPTION);
                if (arguments.size() > index) {
                    return arguments.get(index + 1);
                }
            }
        }
        return id;
    }

   @Override
   public void updateOnRemoved(ComputeService computeService) {
      cache.removeAll(computeService.getContext().unwrap().getId());
   }

   public CacheProvider getCacheProvider() {
      return cacheProvider;
   }

   public void setCacheProvider(CacheProvider cacheProvider) {
      this.cacheProvider = cacheProvider;
   }
}
