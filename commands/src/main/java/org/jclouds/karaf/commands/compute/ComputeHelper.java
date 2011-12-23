/*
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Processor;
import org.jclouds.domain.Location;

public class ComputeHelper {

    public static final String NODEFORMAT = "%s%-30s %-20s %-20s %-20s";
    public static final String HARDWAREFORMAT = "%s%-20s %5s %7s %6s";
    public static final String IMAGEFORMAT = "%s%-30s %-20s %s";
    public static final String LOCATIONFORMAT = "%-30s %-9s %s";


    public static ComputeService getComputeService(String provider, List<ComputeService> services) {
        if (provider != null) {
            ComputeService service = null;
            for (ComputeService svc : services) {
                if (provider.equals(service.getContext().getProviderSpecificContext().getId())) {
                    service = svc;
                    break;
                }
            }
            if (service == null) {
                throw new IllegalArgumentException("Provider " + provider + " not found");
            }
            return service;
        } else {
            if (services.size() != 1) {
                StringBuilder sb = new StringBuilder();
                for (ComputeService svc : services) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(svc.getContext().getProviderSpecificContext().getId());
                }
                throw new IllegalArgumentException("Multiple providers are present, please select one using the --provider argument in the following values: " + sb.toString());
            }
            return services.get(0);
        }
    }


    public static void printNodes(Set<? extends ComputeMetadata> nodes, String indent, PrintStream out) {
        out.println(String.format(NODEFORMAT, indent, "[id]", "[location]", "[hardware]", "[state]"));
        for (ComputeMetadata metadata : nodes) {
            NodeMetadata node = (NodeMetadata) metadata;
            out.println(String.format("%s%-30s %-20s %-20s %-20s", indent, node.getId(), node.getLocation().getId(), node.getHardware().getId(), node.getState().toString().toLowerCase()));
        }
    }

    public static void printHardwares(Set<? extends Hardware> hardwares, String indent, PrintStream out) {
        out.println(String.format(HARDWAREFORMAT, indent, "[id]", "[cpu]", "[cores]", "[ram]", "[disk]"));
        for (Hardware hardware : hardwares) {
            out.println(String.format(HARDWAREFORMAT, indent, hardware.getId(), getCpuUnits(hardware), getCpuCores(hardware), getMemory(hardware)));
        }
    }


    public static void printImages(Set<? extends Image> images, String indent, PrintStream out) {
        out.println(String.format(IMAGEFORMAT, indent, "[id]", "[location]", "[description]"));
        for (Image image : images) {
            out.println(String.format(IMAGEFORMAT, indent, image.getId(), image.getLocation().getId(), image.getDescription()));
        }
    }

    public static void printLocations(Set<? extends Location> locations, String indent, PrintStream out) {
        out.println(String.format(LOCATIONFORMAT, indent + "[id]", "[scope]", "[description]"));
        printLocations(getAllLocations(locations), null, indent, out);
    }

    public static void printLocations(Set<? extends Location> locations, Location parent, String indent, PrintStream out) {
        for (Location location : locations) {
            if (location.getParent() == parent) {
                out.println(String.format(LOCATIONFORMAT, indent + location.getId(), location.getScope(), location.getDescription()));
                printLocations(locations, location, indent + "  ", out);
            }
        }
    }

    public static Set<? extends Location> getAllLocations(Set<? extends Location> locations) {
        Set<Location> all = new HashSet<Location>();
        for (Location loc : locations) {
            for (Location p = loc; p != null; p = p.getParent()) {
                all.add(p);
            }
        }
        return all;
    }


    public static double getMemory(Hardware hardware) {
        return hardware.getRam();
    }

    public static double getCpuCores(Hardware hardware) {
        int nb = 0;
        for (Processor p : hardware.getProcessors()) {
            nb += p.getCores();
        }
        return nb;
    }

    public static double getCpuUnits(Hardware hardware) {
        double nb = 0;
        for (Processor p : hardware.getProcessors()) {
            nb += p.getCores() * p.getSpeed();
        }
        return nb;
    }
}
