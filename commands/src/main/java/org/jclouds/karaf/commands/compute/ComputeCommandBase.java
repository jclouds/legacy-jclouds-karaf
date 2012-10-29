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

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.AbstractAction;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.compute.domain.Processor;
import org.jclouds.domain.Location;
import org.jclouds.karaf.cache.BasicCacheProvider;
import org.jclouds.karaf.cache.CacheProvider;
import org.jclouds.karaf.commands.table.internal.PropertyShellTableFactory;
import org.jclouds.karaf.commands.table.ShellTable;
import org.jclouds.karaf.commands.table.ShellTableFactory;
import org.jclouds.karaf.core.Constants;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.rest.AuthorizationException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import static org.jclouds.karaf.utils.compute.ComputeHelper.findCacheKeysForService;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public abstract class ComputeCommandBase extends AbstractAction {

   public static final String NODE_DETAILS_FORMAT = "%20s %-60s";
   public static final String PROVIDERFORMAT = "%-24s %-12s %-12s";
   public static final String FACTORY_FILTER = "(service.factoryPid=%s)";

   protected ConfigurationAdmin configAdmin;
   protected CacheProvider cacheProvider = new BasicCacheProvider();
   protected List<ComputeService> computeServices = new ArrayList<ComputeService>();
   protected ShellTableFactory shellTableFactory = new PropertyShellTableFactory();

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

   protected void printComputeProviders(Map<String, ProviderMetadata> providers, List<ComputeService> computeServices,
            String indent, PrintStream out) {
      out.println(String.format(PROVIDERFORMAT, "[id]", "[type]", "[service]"));
      for (String provider : providers.keySet()) {
         StringBuilder sb = new StringBuilder();
         sb.append("[ ");
         for (ComputeService computeService : computeServices) {
            String contextName = (String) computeService.getContext().unwrap().getName();
            if (computeService.getContext().unwrap().getId().equals(provider) && contextName != null) {
               sb.append(contextName).append(" ");
            }
         }
         sb.append("]");
         out.println(String.format(PROVIDERFORMAT, provider, "compute", sb.toString()));
      }
   }

   protected void printComputeApis(Map<String, ApiMetadata> apis, List<ComputeService> computeServices, String indent,
            PrintStream out) {
      out.println(String.format(PROVIDERFORMAT, "[id]", "[type]", "[service]"));
      for (String api : apis.keySet()) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
         for (ComputeService computeService : computeServices) {
           String contextName = (String) computeService.getContext().unwrap().getName();
            if (computeService.getContext().unwrap().getId().equals(api) && contextName != null) {
              sb.append(contextName).append(" ");
            }
         }
        sb.append("]");
         out.println(String.format(PROVIDERFORMAT, api, "compute", sb.toString()));
      }
   }

   protected void printNodes(ComputeService service, Set<? extends ComputeMetadata> nodes, PrintStream out) {
     ShellTable table = shellTableFactory.build("node");
     table.setDisplayData(nodes);
     table.display(out, true, true);

      for (ComputeMetadata metadata : nodes) {
         NodeMetadata node = (NodeMetadata) metadata;
        for (String cacheKey : findCacheKeysForService(service)) {
          cacheProvider.getProviderCacheForType(Constants.GROUP).put(cacheKey, node.getGroup());
        }
      }
   }

   protected void printHardwares(ComputeService service, Set<? extends Hardware> hardwares, PrintStream out) {
     ShellTable table = shellTableFactory.build("hardware");
     table.setDisplayData(hardwares);
     table.display(out, true, true);

      for (Hardware hardware : hardwares) {
         for (String cacheKey : findCacheKeysForService(service)) {
           cacheProvider.getProviderCacheForType(Constants.HARDWARE_CACHE).put(cacheKey, hardware.getId());
         }
      }
   }

   protected void printImages(ComputeService service, Set<? extends Image> images, PrintStream out) {
      ShellTable table = shellTableFactory.build("image");
      table.setDisplayData(images);
      table.display(out, true, true);

      for (Image image : images) {
        for (String cacheKey : findCacheKeysForService(service)) {
         cacheProvider.getProviderCacheForType(Constants.IMAGE_CACHE).put(cacheKey, image.getId());
        }
      }
   }

   protected void printLocations(ComputeService computeService, PrintStream out) {
     ShellTable table = shellTableFactory.build("location");
     table.setDisplayData(getAllLocations(computeService));
     table.display(out, true, true);
   }

   protected Set<? extends Location> getAllLocations(ComputeService computeService) {
      Set<Location> all = new HashSet<Location>();
      for (Location loc : computeService.listAssignableLocations()) {
         for (Location p = loc; p != null; p = p.getParent()) {
            all.add(p);
            for (String cacheKey : findCacheKeysForService(computeService)) {
            cacheProvider.getProviderCacheForType(Constants.LOCATION_CACHE).put(cacheKey, p.getId());
            }
         }
      }
      return all;
   }

   /**
    * Returns a String that displays the {@link org.jclouds.compute.domain.OperatingSystem} details.
    * 
    * @param node
    * @return
    */
   protected String getOperatingSystemDetails(NodeMetadata node) {
      if (node != null) {
         OperatingSystem os = node.getOperatingSystem();
         if (os != null) {
            return node.getOperatingSystem().getFamily().value() + " " + node.getOperatingSystem().getArch() + " "
                     + node.getOperatingSystem().getVersion();
         }
      }
      return "";
   }

   /**
    * Returns a comma separated list of the {@NodeMetadata} public addresses.
    * 
    * @param node
    * @return
    */
   protected String getPublicAddresses(NodeMetadata node) {
      StringBuilder sb = new StringBuilder();
      if (node != null && node.getPublicAddresses() != null && !node.getPublicAddresses().isEmpty()) {
         Set<String> publicAddresses = node.getPublicAddresses();
         Iterator<String> addressIterator = publicAddresses.iterator();
         while (addressIterator.hasNext()) {
            sb.append(addressIterator.next());
            if (addressIterator.hasNext()) {
               sb.append(" , ");
            }
         }
      }
      return sb.toString();
   }

   /**
    * Returns a comma separated list of the {@NodeMetadata} private addresses.
    * 
    * @param node
    * @return
    */
   protected String getPrivateAddresses(NodeMetadata node) {
      StringBuilder sb = new StringBuilder();
      if (node != null && node.getPrivateAddresses() != null && !node.getPrivateAddresses().isEmpty()) {
         Set<String> privateAddresses = node.getPrivateAddresses();
         Iterator<String> addressIterator = privateAddresses.iterator();
         while (addressIterator.hasNext()) {
            sb.append(addressIterator.next());
            if (addressIterator.hasNext()) {
               sb.append(" , ");
            }
         }
      }
      return sb.toString();
   }

   protected void printNodeInfo(ComputeService service, Set<? extends NodeMetadata> nodes, boolean details, PrintStream out) {
      printNodes(service, nodes,  out);
      if (details) {
         for (NodeMetadata node : nodes) {
            out.println();
            out.println(String.format(NODE_DETAILS_FORMAT, "Operating System:", getOperatingSystemDetails(node)));
            out.println(String.format(NODE_DETAILS_FORMAT, "Configured User:", node.getCredentials() != null ? node
                     .getCredentials().getUser() : "n/a"));
            out.println(String.format(NODE_DETAILS_FORMAT, "Public Address:", getPublicAddresses(node)));
            out.println(String.format(NODE_DETAILS_FORMAT, "Private Address:", getPrivateAddresses(node)));
            out.println(String.format(NODE_DETAILS_FORMAT, "Image Id:", node.getImageId()));
         }
      }
   }


  /**
   * Finds a {@link org.osgi.service.cm.Configuration} if exists, or creates a new one.
   *
   * @param configurationAdmin
   * @param factoryPid
   * @param provider
   * @param api
   * @return
   * @throws java.io.IOException
   */
  protected Configuration findOrCreateFactoryConfiguration(ConfigurationAdmin configurationAdmin, String factoryPid, String id, String provider, String api) throws IOException {
    Configuration configuration = null;
    if (configurationAdmin != null) {
      try {
        Configuration[] configurations = configurationAdmin.listConfigurations(String.format(FACTORY_FILTER, factoryPid));
        if (configurations != null) {
          for (Configuration conf : configurations) {
            Dictionary<?, ?> dictionary = conf.getProperties();
            //If id has been specified only try to match by id, ignore the rest.
            if (dictionary != null && id != null) {
              if (id.equals(dictionary.get(Constants.NAME))) {
                return conf;
              }
            } else {
              if (dictionary != null && provider != null && provider.equals(dictionary.get("provider"))) {
                return conf;
              } else if (dictionary != null && api != null && api.equals(dictionary.get("api"))) {
                return conf;
              }
            }
          }
        }
      } catch (Exception e) {
        // noop
      }
      configuration = configurationAdmin.createFactoryConfiguration(factoryPid, null);
    }
    return configuration;
  }

   protected double getMemory(Hardware hardware) {
      return hardware.getRam();
   }

   protected double getCpuCores(Hardware hardware) {
      int nb = 0;
      for (Processor p : hardware.getProcessors()) {
         nb += p.getCores();
      }
      return nb;
   }

   protected double getCpuUnits(Hardware hardware) {
      double nb = 0;
      for (Processor p : hardware.getProcessors()) {
         nb += p.getCores() * p.getSpeed();
      }
      return nb;
   }

   public ConfigurationAdmin getConfigAdmin() {
      return configAdmin;
   }

   public void setConfigAdmin(ConfigurationAdmin configAdmin) {
      this.configAdmin = configAdmin;
   }

   public CacheProvider getCacheProvider() {
      return cacheProvider;
   }

   public void setCacheProvider(CacheProvider cacheProvider) {
      this.cacheProvider = cacheProvider;
   }

   public List<ComputeService> getComputeServices() {
      return computeServices;
   }

   public void setComputeServices(List<ComputeService> computeServices) {
      this.computeServices = computeServices;
   }

  public ShellTableFactory getShellTableFactory() {
    return shellTableFactory;
  }

  public void setShellTableFactory(ShellTableFactory shellTableFactory) {
    this.shellTableFactory = shellTableFactory;
  }
}
