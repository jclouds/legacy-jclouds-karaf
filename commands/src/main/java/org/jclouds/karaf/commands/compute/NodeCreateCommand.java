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

import java.util.List;
import java.util.Set;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;
import org.jclouds.scriptbuilder.statements.login.AdminAccess;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "node-create", description = "Creates a node.")
public class NodeCreateCommand extends ComputeCommandWithOptions {
    @Option(name = "--adminAccess")
    private boolean adminAccess;
   
    @Option(name = "--smallest")
    private boolean smallest;

    @Option(name = "--fastest")
    private boolean fastest;

    @Option(name = "--biggest")
    private boolean biggest;

    @Option(name = "--hardwareId")
    private String hardwareId;

    @Option(name = "--ec2-security-groups", multiValued = true)
    private List<String> ec2SecurityGroups;

    @Option(name = "--ec2-key-pair")
    private String ec2KeyPair;

    @Option(name = "--ec2-no-key-pair")
    private String ec2NoKeyPair;

    @Option(name = "--os-family", multiValued = false, required = false, description = "OS Family")
    private String osFamily;

    @Option(name = "--os-version", multiValued = false, required = false, description = "OS Version")
    private String osVersion;

    @Option(name = "--imageId",  multiValued = false, required = false, description = "Image")
    private String imageId;

    @Option(name = "--locationId",  multiValued = false, required = false, description = "Location")
    private String locationId;

    @Argument(name = "group", index = 0, multiValued = false, required = true, description = "Node group")
    private String group;

    @Argument(name = "number", index = 1, multiValued = false, required = false, description = "Number of nodes to create")
    private Integer number  = 1;



    @Override
    protected Object doExecute() throws Exception {
        ComputeService service = null;
        try {
            service = getComputeService();
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            return null;
        }

        TemplateBuilder builder = service.templateBuilder();
        builder.any();
        if (smallest) {
            builder.smallest();
        }
        if (fastest) {
            builder.fastest();
        }
        if (biggest) {
            builder.biggest();
        }
        if (locationId != null) {
            builder.locationId(locationId);
        }
        if (imageId != null) {
            builder.imageId(imageId);
        }
        if (hardwareId != null) {
            builder.hardwareId(hardwareId);
        }

        if (osFamily != null) {
            builder.osFamily(OsFamily.fromValue(osFamily));
        }

        if (osVersion != null) {
            builder.osVersionMatches(osVersion);
        }

        TemplateOptions options = service.templateOptions();
        if (adminAccess) {
            options.runScript(AdminAccess.standard());
        }
        if (ec2SecurityGroups != null) {
            options.as(EC2TemplateOptions.class).securityGroups(ec2SecurityGroups);
        }
        if (ec2KeyPair != null) {
            options.as(EC2TemplateOptions.class).keyPair(ec2KeyPair);
        }
        if (ec2NoKeyPair != null) {
            options.as(EC2TemplateOptions.class).noKeyPair();
        }

        Set<? extends NodeMetadata> metadatas = null;

        try {
            metadatas = service.createNodesInGroup(group, number, builder.options(options).build());
        } catch (RunNodesException ex) {
            System.out.println("Failed to create nodes:" + ex.getMessage());
        }

        if (metadatas != null && !metadatas.isEmpty()) {
            System.out.println("Created nodes:");
            printNodes(metadatas, "", System.out);
        }

        for (NodeMetadata node : metadatas) {
            cacheProvider.getProviderCacheForType(Constants.ACTIVE_NODE_CACHE).put(service.getContext().unwrap().getId(), node.getId());
            cacheProvider.getProviderCacheForType(Constants.INACTIVE_NODE_CACHE).put(service.getContext().unwrap().getId(), node.getId());
            cacheProvider.getProviderCacheForType(Constants.SUSPENDED_NODE_CACHE).put(service.getContext().unwrap().getId(), node.getId());
        }

        return null;
    }

}
