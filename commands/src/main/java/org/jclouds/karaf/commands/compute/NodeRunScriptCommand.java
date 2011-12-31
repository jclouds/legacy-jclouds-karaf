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
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.Credentials;


import static org.jclouds.compute.options.RunScriptOptions.Builder.overrideCredentialsWith;

/**
 * @author <a href="mailto:gnodet[at]gmail.com">Guillaume Nodet (gnodet)</a>
 */
@Command(scope = "jclouds", name = "node-runscript")
public class NodeRunScriptCommand extends ComputeCommandSupport {

    @Argument(name = "id", index = 0, description = "The id of the node.", required = true, multiValued = false)
    private String id;

    @Option(name = "-u", aliases = "--user", description = "The user that will run the script.", required = false, multiValued = false)
    private String user;

    @Option(name = "-s", aliases = "--script-url", description = "The url script of the script to run.", required = false, multiValued = false)
    private String scriptUrl;

    @Override
    protected Object doExecute() throws Exception {

        NodeMetadata nodeMetadata = getComputeService().getNodeMetadata(id);
        Credentials credentials = nodeMetadata.getCredentials();

        if (credentials == null) {
            credentials = new Credentials(null, null);
        }

        if (user != null) {
            credentials = new Credentials(user, credentials.credential);
        }

        ExecResponse response = getComputeService().runScriptOnNode(id, getScript(), overrideCredentialsWith(credentials));

        if (response.getOutput() != null && !response.getOutput().isEmpty()) {
            System.out.println("Output:" + response.getOutput());
        }

        if (response.getError() != null && !response.getError().isEmpty()) {
            System.out.println("Error:" + response.getError());
        }

        return null;
    }

    /**
     * Returns the script to run.
     * If url is specified the script is read from the url.
     */
    private String getScript() {
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
                    builder.append(line);
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
