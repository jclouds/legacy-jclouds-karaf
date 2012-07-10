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
import org.jclouds.karaf.utils.EnvHelper;
import org.jclouds.karaf.utils.compute.ComputeHelper;
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
public abstract class ComputeCommandSupport extends AbstractAction {


    public static final String NODEFORMAT = "%s%-30s %-32s %-20s %-12s %-12s";
    private static final String NODE_DETAILS_FORMAT = "%20s %-60s";
    public static final String HARDWAREFORMAT = "%s%-20s %5s %7s %6s";
    public static final String IMAGEFORMAT = "%s%-30s %-32s %s";
    public static final String LOCATIONFORMAT = "%-32s %-9s %s";
    public static final String PROVIDERFORMAT = "%-24s %-12s %-12s";


    private ConfigurationAdmin configurationAdmin;
    private List<ComputeService> computeServices = new ArrayList<ComputeService>();
    protected CacheProvider cacheProvider = new BasicCacheProvider();

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
    public Object execute(CommandSession session) throws Exception {
        try {
            this.session = session;
            return doExecute();
        } catch (AuthorizationException ex) {
            System.err.println("Authorization error. Please make sure you provided valid identity and credential.");
            return null;
        }
    }

    protected void printComputeProviders(Map<String, ProviderMetadata> providers, List<ComputeService> computeServices, String indent, PrintStream out) {
        out.println(String.format(PROVIDERFORMAT, "[id]", "[type]", "[service]"));
        for (String provider : providers.keySet()) {
            boolean registered = false;
            for (ComputeService computeService : computeServices) {
                if (computeService.getContext().unwrap().getId().equals(provider)) {
                    registered = true;
                    break;
                }
            }
            out.println(String.format(PROVIDERFORMAT, provider, "compute", registered));
        }
    }

    protected void printComputeApis(Map<String, ApiMetadata> apis, List<ComputeService> computeServices, String indent, PrintStream out) {
        out.println(String.format(PROVIDERFORMAT, "[id]", "[type]", "[service]"));
        for (String api : apis.keySet()) {
            boolean registered = false;
            for (ComputeService computeService : computeServices) {
                if (computeService.getContext().unwrap().getId().equals(api)) {
                    registered = true;
                    break;
                }
            }
            out.println(String.format(PROVIDERFORMAT, api, "compute", registered));
        }
    }

    protected void printNodes(Set<? extends ComputeMetadata> nodes, String indent, PrintStream out) {
        out.println(String.format(NODEFORMAT, indent, "[id]", "[location]", "[hardware]", "[group]", "[state]"));
        for (ComputeMetadata metadata : nodes) {
            NodeMetadata node = (NodeMetadata) metadata;
            out.println(String.format(NODEFORMAT, indent, node.getId(), node.getLocation().getId(), node.getHardware().getId(), node.getGroup(), node.getState().toString().toLowerCase()));
            cacheProvider.getProviderCacheForType(Constants.GROUP).put(node.getProviderId(), node.getGroup());
        }
    }

    protected void printHardwares(Set<? extends Hardware> hardwares, String indent, PrintStream out) {
        out.println(String.format(HARDWAREFORMAT, indent, "[id]", "[cpu]", "[cores]", "[ram]", "[disk]"));
        for (Hardware hardware : hardwares) {
            out.println(String.format(HARDWAREFORMAT, indent, hardware.getId(), getCpuUnits(hardware), getCpuCores(hardware), getMemory(hardware)));
            cacheProvider.getProviderCacheForType(Constants.HARDWARE_CACHE).put(hardware.getProviderId(), hardware.getId());
        }
    }


    protected void printImages(Set<? extends Image> images, String indent, PrintStream out) {
        out.println(String.format(IMAGEFORMAT, indent, "[id]", "[location]", "[description]"));
        for (Image image : images) {
            String id = image.getId();
            String location = image.getLocation() != null ? image.getLocation().getId() : "";
            String description = image.getDescription();
            out.println(String.format(IMAGEFORMAT, indent, id, location, description));
            cacheProvider.getProviderCacheForType(Constants.IMAGE_CACHE).put(image.getProviderId(), image.getId());
        }
    }

    protected void printLocations(ComputeService computeService, String indent, PrintStream out) {
        out.println(String.format(LOCATIONFORMAT, indent + "[id]", "[scope]", "[description]"));
        printLocations(getAllLocations(computeService), null, indent, out);
    }

    protected void printLocations(Set<? extends Location> locations, Location parent, String indent, PrintStream out) {
        for (Location location : locations) {
            if (location.getParent() == parent) {
                out.println(String.format(LOCATIONFORMAT, indent + location.getId(), location.getScope(), location.getDescription()));
                printLocations(locations, location, indent + "  ", out);
            }
        }
    }

    protected Set<? extends Location> getAllLocations(ComputeService computeService) {
        Set<Location> all = new HashSet<Location>();
        for (Location loc : computeService.listAssignableLocations()) {
            for (Location p = loc; p != null; p = p.getParent()) {
                all.add(p);
                cacheProvider.getProviderCacheForType(Constants.LOCATION_CACHE).put(computeService.getContext().unwrap().getId(), p.getId());
            }
        }
        return all;
    }

    /**
     * Returns a String that displays the {@link OperatingSystem} details.
     *
     * @param node
     * @return
     */
    protected String getOperatingSystemDetails(NodeMetadata node) {
        if (node != null) {
            OperatingSystem os = node.getOperatingSystem();
            if (os != null) {
                return node.getOperatingSystem().getFamily().value() + " " + node.getOperatingSystem().getArch() + " " + node.getOperatingSystem().getVersion();
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

    protected void printNodeInfo(Set<? extends NodeMetadata> nodes, boolean details, PrintStream out) {
        printNodes(nodes, "", out);
        if (details) {
            for (NodeMetadata node : nodes) {
                out.println();
                out.println(String.format(NODE_DETAILS_FORMAT, "Operating System:", getOperatingSystemDetails(node)));
                out.println(String.format(NODE_DETAILS_FORMAT, "Configured User:", node.getCredentials() != null ? node.getCredentials().getUser() : "n/a"));
                out.println(String.format(NODE_DETAILS_FORMAT, "Public Address:", getPublicAddresses(node)));
                out.println(String.format(NODE_DETAILS_FORMAT, "Private Address:", getPrivateAddresses(node)));
                out.println(String.format(NODE_DETAILS_FORMAT, "Image Id:", node.getImageId()));
            }
        }
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
        return configurationAdmin;
    }

    public void setConfigAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    public void setComputeServices(List<ComputeService> computeServices) {
        this.computeServices = computeServices;
    }

    protected List<ComputeService> getComputeServices() {
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
        if (computeServices != null && computeServices.size() == 1) {
            return computeServices.get(0);
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
            computeService = ComputeHelper.getComputeService(providerOrApiValue, computeServices);
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
                ContextBuilder builder = ContextBuilder.newBuilder(providerOrApiValue).credentials(identityValue, credentialValue).modules(ImmutableSet.<Module>of(new SshjSshClientModule()));
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

    public CacheProvider getCacheProvider() {
        return cacheProvider;
    }

    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }
}
