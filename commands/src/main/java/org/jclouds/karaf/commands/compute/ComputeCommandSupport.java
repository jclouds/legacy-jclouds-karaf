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

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Processor;
import org.jclouds.domain.Location;
import org.jclouds.karaf.cache.CacheProvider;
import org.jclouds.karaf.utils.compute.ComputeHelper;
import org.osgi.service.cm.ConfigurationAdmin;


/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public abstract class ComputeCommandSupport extends OsgiCommandSupport {


    public static final String NODEFORMAT = "%s%-30s %-32s %-20s %-12s %-12s";
    public static final String HARDWAREFORMAT = "%s%-20s %5s %7s %6s";
    public static final String IMAGEFORMAT = "%s%-30s %-32s %s";
    public static final String LOCATIONFORMAT = "%-32s %-9s %s";
    public static final String PROVIDERFORMAT = "%-16s %s";

    private ConfigurationAdmin configurationAdmin;
    private List<ComputeService> computeServices;
    private CacheProvider cacheProvider;

    @Option(name = "--provider")
    protected String provider;



     protected void printComputeProviders(List<ComputeService> computeServices, String indent, PrintStream out) {
        out.println(String.format(PROVIDERFORMAT, "[id]", "[type]"));
        for (ComputeService computeService : computeServices) {
            out.println(String.format(PROVIDERFORMAT, computeService.getContext().getProviderSpecificContext().getId(), "compute"));
        }
    }

    protected void printNodes(Set<? extends ComputeMetadata> nodes, String indent, PrintStream out) {
        out.println(String.format(NODEFORMAT, indent, "[id]", "[location]", "[hardware]", "[group]", "[state]"));
        for (ComputeMetadata metadata : nodes) {
            NodeMetadata node = (NodeMetadata) metadata;
            out.println(String.format(NODEFORMAT, indent, node.getId(), node.getLocation().getId(), node.getHardware().getId(), node.getGroup(), node.getState().toString().toLowerCase()));
            cacheProvider.getProviderCacheForType("node").put(node.getProviderId(),node.getId());
            cacheProvider.getProviderCacheForType("group").put(node.getProviderId(),node.getGroup());
        }
    }

    protected void printHardwares(Set<? extends Hardware> hardwares, String indent, PrintStream out) {
        out.println(String.format(HARDWAREFORMAT, indent, "[id]", "[cpu]", "[cores]", "[ram]", "[disk]"));
        for (Hardware hardware : hardwares) {
            out.println(String.format(HARDWAREFORMAT, indent, hardware.getId(), getCpuUnits(hardware), getCpuCores(hardware), getMemory(hardware)));
        }
    }


    protected void printImages(Set<? extends Image> images, String indent, PrintStream out) {
        out.println(String.format(IMAGEFORMAT, indent, "[id]", "[location]", "[description]"));
        for (Image image : images) {
            String id = image.getId();
            String location = image.getLocation() != null ? image.getLocation().getId() : "";
            String description = image.getDescription();
            out.println(String.format(IMAGEFORMAT, indent, id, location, description));
            cacheProvider.getProviderCacheForType("image").put(image.getProviderId(),image.getId());
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
                cacheProvider.getProviderCacheForType("location").put(computeService.getContext().getProviderSpecificContext().getId(),p.getId());
            }
        }
        return all;
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


    public ConfigurationAdmin getConfigurationAdmin() {
        return configurationAdmin;
    }

    public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    public void setComputeServices(List<ComputeService> computeServices) {
        this.computeServices = computeServices;
    }

    protected List<ComputeService> getComputeServices() {
        if (provider == null) {
            return computeServices;
        } else {
            return Collections.singletonList(getComputeService());
        }
    }

    protected ComputeService getComputeService() {
        return ComputeHelper.getComputeService(provider, computeServices);
    }

    public CacheProvider getCacheProvider() {
        return cacheProvider;
    }

    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }
}
