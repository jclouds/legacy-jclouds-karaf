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

package org.jclouds.karaf.commands.blobstore;

import com.google.common.reflect.TypeToken;
import org.apache.felix.gogo.commands.Command;
import org.jclouds.apis.Apis;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.providers.Providers;

@Command(scope = "jclouds", name = "blobstore-service-list", description = "List available BlobStore services.", detailedDescription = "classpath:blobstore-service-list.txt")
public class BlobStoreServiceListCommand extends BlobStoreCommandBase {


    @Override
    protected Object doExecute() throws Exception {
        try {
            System.out.println("BlobStore APIs:");
            System.out.println("---------------");
            printBlobStoreApis(Apis.viewableAs(TypeToken.of(BlobStoreContext.class)), getBlobStoreServices(), "", System.out);
            System.out.println();
            System.out.println();

            System.out.println("BlobStore Providers:");
            System.out.println("--------------------");
            printBlobStoreProviders(Providers.viewableAs(TypeToken.of(BlobStoreContext.class)), getBlobStoreServices(), "", System.out);
        } catch (Exception ex) {
            // noop
        }
        return null;
    }
}
