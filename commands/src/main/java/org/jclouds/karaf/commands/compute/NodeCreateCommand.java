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
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "node-create")
public class NodeCreateCommand extends ComputeCommandSupport {

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


    @Argument(name = "group", index = 0, multiValued = false, required = true, description = "Image")
    private String imageId;

    @Argument(name = "group", index = 1, multiValued = false, required = true, description = "Location")
    private String locationId;


    @Argument(name = "group", index = 2, multiValued = false, required = true, description = "Node group")
    private String group;


    @Override
    protected Object doExecute() throws Exception {
        ComputeService service = getComputeService();

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
        if (ec2SecurityGroups != null) {
            builder.build().getOptions().as(EC2TemplateOptions.class).securityGroups(ec2SecurityGroups);
        }
        if (ec2KeyPair != null) {
            builder.build().getOptions().as(EC2TemplateOptions.class).keyPair(ec2KeyPair);
        }
        if (ec2NoKeyPair != null) {
            builder.build().getOptions().as(EC2TemplateOptions.class).noKeyPair();
        }

        Set<? extends NodeMetadata> metadatas = null;

        try {
            metadatas = service.createNodesInGroup(group, 1, builder.build());
        } catch (RunNodesException ex) {
            System.out.println("Failed to create nodes:" + ex.getMessage());
        }

        if (metadatas != null && !metadatas.isEmpty()) {
            System.out.println("Created nodes:");
            ComputeHelper.printNodes(metadatas, "", System.out);
        }
        return null;
    }

}
