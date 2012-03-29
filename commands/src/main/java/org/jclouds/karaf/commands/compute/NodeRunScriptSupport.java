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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.felix.gogo.commands.Option;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeState;
import org.jclouds.domain.Credentials;

import com.google.common.base.Predicate;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
public abstract class NodeRunScriptSupport extends ComputeCommandSupport {

    @Option(name = "-u", aliases = "--user", description = "The user that will run the script.", required = false, multiValued = false)
    private String user;

    @Option(name = "-s", aliases = "--script-url", description = "The url script of the script to run.", required = false, multiValued = false)
    private String scriptUrl;

    @Option(name = "-d", aliases = "--direct", description = "A direct command passed to the node to run. ", required = false, multiValued = false)
    private String directCommand;


    public abstract String getId();

    public abstract String getGroup();

    @Override
    protected Object doExecute() throws Exception {
        Set<? extends NodeMetadata> nodeMetaDataSet = getComputeService().listNodesDetailsMatching(getComputeFilter());
        if (nodeMetaDataSet != null && !nodeMetaDataSet.isEmpty()) {
            NodeMetadata nodeMetadata = nodeMetaDataSet.toArray(new NodeMetadata[0])[0];
            Credentials credentials = nodeMetadata.getCredentials();

            if (credentials == null) {
                credentials = new Credentials(null, null);
            }

            if (user != null) {
                credentials = new Credentials(user, credentials.credential);
            }

             Map<? extends NodeMetadata, ExecResponse> responseMap = getComputeService().runScriptOnNodesMatching(getNodeFilter(), getScript(), overrideCredentialsWith(credentials).runAsRoot(false));

            for (Map.Entry<? extends NodeMetadata, ExecResponse> entry : responseMap.entrySet()) {
                ExecResponse response = entry.getValue();
                NodeMetadata node = entry.getKey();
                System.out.println("Node:" + node);
                if (response.getOutput() != null && !response.getOutput().isEmpty()) {
                    System.out.println("Output:" + response.getOutput());
                }

                if (response.getError() != null && !response.getError().isEmpty()) {
                    System.out.println("Error:" + response.getError());
                }
            }
        }

        return null;
    }

    public Predicate<NodeMetadata> getNodeFilter() {
        return new Predicate<NodeMetadata>() {
            @Override
            public boolean apply(@Nullable NodeMetadata input) {
                boolean applies = true;
                if (!input.getState().equals(NodeState.RUNNING)) {
                    return false;
                }

                if (getId() != null && !getId().isEmpty() && !input.getId().equals(getId())) {
                    applies = false;
                }
                if (getGroup() != null && !getGroup().isEmpty() && !input.getGroup().equals(getGroup())) {
                    applies = false;
                }
                return applies;
            }
        };
    }

    public Predicate<ComputeMetadata> getComputeFilter() {
        return new Predicate<ComputeMetadata>() {
            @Override
            public boolean apply(@Nullable ComputeMetadata input) {
                boolean applies = true;
                if (input instanceof NodeMetadata) {

                    if (!((NodeMetadata) input).getState().equals(NodeState.RUNNING)) {
                        return false;
                    }

                    if (getId() != null && !getId().isEmpty() && !input.getId().equals(getId())) {
                        applies = false;
                    }

                    if (getGroup() != null && !getGroup().isEmpty() && !((NodeMetadata) input).getGroup().equals(getGroup())) {
                        applies = false;
                    }
                }
                return applies;
            }
        };
    }

    /**
     * Returns the script to run.
     * If url is specified the script is read from the url.
     */
    private String getScript() {
        if (directCommand != null) {
            return directCommand;
        }
        if (scriptUrl != null) {
            InputStream is = null;
            DataInputStream in = null;
            BufferedReader br = null;
            StringBuilder builder = new StringBuilder();
            try {
                URL url = new URL(scriptUrl);
                is = url.openStream();
                in = new DataInputStream(is);
                br = new BufferedReader(new InputStreamReader(in));
                String line = null;

                while ((line = br.readLine()) != null) {
                    builder.append(line ).append("\n");
                }

                return builder.toString();
            } catch (MalformedURLException e) {
                System.err.println("The provided script url is invalid.");
            } catch (IOException e) {
                System.err.println("Cannot read script from url.");
            } finally {
                try {
                    br.close();
                } catch (Exception ex) {
                }
                try {
                    in.close();
                } catch (Exception ex) {
                }
                try {
                    is.close();
                } catch (Exception ex) {
                }
            }
        }
        return "";
    }
}
