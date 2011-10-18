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
import java.util.HashSet;
import java.util.Set;

import org.apache.felix.gogo.commands.Command;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Processor;
import org.jclouds.domain.Location;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "list")
public class ListCommand extends ComputeCommandSupport {

    @Override
    protected Object doExecute() throws Exception {
        for (ComputeService service : getComputeServices()) {
            String txt = "Instances on " + service.getContext().getProviderSpecificContext().getId();
            System.out.println(txt);
            for (int i = 0; i < txt.length(); i++) {
                System.out.print('=');
            }
            System.out.println();

            System.out.println("  Locations");
            System.out.println("  ---------");
            printLocations(service.listAssignableLocations(), "    ", System.out);

            System.out.println("  Images");
            System.out.println("  ------");
            printImages(service.listImages(), "    ", System.out);

            System.out.println("  Hardware");
            System.out.println("  --------");
            printHardwares(service.listHardwareProfiles(), "    ", System.out);

            System.out.println("  Nodes");
            System.out.println("  -----");
            printNodes(service.listNodes(), "    ", System.out);
        }
        return null;
    }

    private void printNodes(Set<? extends ComputeMetadata> nodes, String indent, PrintStream out) {
        out.println(String.format("%s%-30s %-20s %-20s %-20s", indent, "[id]", "[location]", "[hardware]", "[state]"));
        for (ComputeMetadata metadata : nodes) {
            NodeMetadata node = (NodeMetadata) metadata;
            out.println(String.format("%s%-30s %-20s %-20s %-20s", indent, node.getId(), node.getLocation().getId(), node.getHardware().getId(), node.getState().toString().toLowerCase()));
        }
    }

    private void printHardwares(Set<? extends Hardware> hardwares, String indent, PrintStream out) {
        out.println(String.format("%s%-20s %5s %7s %6s", indent, "[id]", "[cpu]", "[cores]", "[ram]", "[disk]"));
        for (Hardware hardware : hardwares) {
            out.println(String.format("%s%-20s %5.1f %7.1f %6.0f", indent, hardware.getId(), getCpuUnits(hardware), getCpuCores(hardware), getMemory(hardware)));
        }
    }

    private double getMemory(Hardware hardware) {
        return hardware.getRam();
    }

    private double getCpuCores(Hardware hardware) {
        int nb = 0;
        for (Processor p : hardware.getProcessors()) {
            nb += p.getCores();
        }
        return nb;
    }

    private double getCpuUnits(Hardware hardware) {
        double nb = 0;
        for (Processor p : hardware.getProcessors()) {
            nb += p.getCores() * p.getSpeed();
        }
        return nb;
    }

    private void printImages(Set<? extends Image> images, String indent, PrintStream out) {
        out.println(String.format("%s%-30s %-20s %s", indent, "[id]", "[location]", "[description]"));
        for (Image image : images) {
            out.println(String.format("%s%-30s %-20s %s", indent, image.getId(), image.getLocation().getId(), image.getDescription()));
        }
    }

    private void printLocations(Set<? extends Location> locations, String indent, PrintStream out) {
        out.println(String.format("%-30s %-9s %s", indent + "[id]", "[scope]", "[description]"));
        printLocations(getAllLocations(locations), null, indent, out);
    }

    private void printLocations(Set<? extends Location> locations, Location parent, String indent, PrintStream out) {
        for (Location location : locations) {
            if (location.getParent() == parent) {
                out.println(String.format("%-30s %-9s %s", indent + location.getId(), location.getScope(), location.getDescription()));
                printLocations(locations, location, indent + "  ", out);
            }
        }
    }

    private Set<? extends Location> getAllLocations(Set<? extends Location> locations) {
        Set<Location> all = new HashSet<Location>();
        for (Location loc : locations) {
            for (Location p = loc; p != null; p = p.getParent()) {
                all.add( p );
            }
        }
        return all;
    }

}
