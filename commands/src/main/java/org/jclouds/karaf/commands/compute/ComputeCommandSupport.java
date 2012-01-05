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
import org.jclouds.karaf.commands.cache.CacheProvider;
import org.jclouds.karaf.utils.compute.ComputeHelper;
import org.osgi.service.cm.ConfigurationAdmin;


/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public abstract class ComputeCommandSupport extends OsgiCommandSupport {


    public static final String NODEFORMAT = "%s%-30s %-20s %-20s %-20s %-20s";
    public static final String HARDWAREFORMAT = "%s%-20s %5s %7s %6s";
    public static final String IMAGEFORMAT = "%s%-30s %-20s %s";
    public static final String LOCATIONFORMAT = "%-30s %-9s %s";

    private ConfigurationAdmin configurationAdmin;
    private List<ComputeService> services;

    @Option(name = "--provider")
    protected String provider;



    protected void printNodes(Set<? extends ComputeMetadata> nodes, String indent, PrintStream out) {
        out.println(String.format(NODEFORMAT, indent, "[id]", "[location]", "[hardware]", "[group]", "[state]"));
        for (ComputeMetadata metadata : nodes) {
            NodeMetadata node = (NodeMetadata) metadata;
            out.println(String.format(NODEFORMAT, indent, node.getId(), node.getLocation().getId(), node.getHardware().getId(), node.getGroup(), node.getState().toString().toLowerCase()));
            CacheProvider.getCache("node").add(node.getId());
            CacheProvider.getCache("group").add(node.getGroup());
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
            out.println(String.format(IMAGEFORMAT, indent, image.getId(), image.getLocation().getId(), image.getDescription()));
            CacheProvider.getCache("image").add(image.getId());
        }
    }

    protected void printLocations(Set<? extends Location> locations, String indent, PrintStream out) {
        out.println(String.format(LOCATIONFORMAT, indent + "[id]", "[scope]", "[description]"));
        printLocations(getAllLocations(locations), null, indent, out);
    }

    protected void printLocations(Set<? extends Location> locations, Location parent, String indent, PrintStream out) {
        for (Location location : locations) {
            if (location.getParent() == parent) {
                out.println(String.format(LOCATIONFORMAT, indent + location.getId(), location.getScope(), location.getDescription()));
                printLocations(locations, location, indent + "  ", out);
            }
        }
    }

    protected Set<? extends Location> getAllLocations(Set<? extends Location> locations) {
        Set<Location> all = new HashSet<Location>();
        for (Location loc : locations) {
            for (Location p = loc; p != null; p = p.getParent()) {
                all.add(p);
                CacheProvider.getCache("location").add(p.getId());
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

    public void setServices(List<ComputeService> services) {
        this.services = services;
    }

    protected List<ComputeService> getComputeServices() {
        if (provider == null) {
            return services;
        } else {
            return Collections.singletonList(getComputeService());
        }
    }

    protected ComputeService getComputeService() {
        return ComputeHelper.getComputeService(provider, services);
    }
}
