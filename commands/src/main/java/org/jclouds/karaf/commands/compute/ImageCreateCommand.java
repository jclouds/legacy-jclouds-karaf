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

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.ImageTemplate;
import org.jclouds.compute.extensions.ImageExtension;

@Command(scope = "jclouds", name = "image-create", description = "Create an image from an existing node.")
public class ImageCreateCommand extends ComputeCommandWithOptions {

    @Argument(name = "id", index = 0, description = "The id of the node to use as a template.", required = true, multiValued = false)
    private String id;

    @Argument(name = "name", index = 1, description = "The name of the image template.", required = true, multiValued = false)
    private String name;


    @Override
    protected Object doExecute() throws Exception {
        ComputeService service = null;
        try {
            service = getComputeService();
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            return null;
        }

        if (!service.getImageExtension().isPresent()) {
            System.out.print("Provider " + service.getContext().unwrap().getProviderMetadata().getId() + " does not currently provide image creation support.");
            return null;
        }
        ImageExtension imageExtension = service.getImageExtension().get();
        ImageTemplate imageTemplate = imageExtension.buildImageTemplateFromNode(name, id);
        ListenableFuture<Image> imageFuture = imageExtension.createImage(imageTemplate);
        Image image = imageFuture.get();
        System.out.println("Succesfully created image:" + image.getId());
        return null;
    }

}