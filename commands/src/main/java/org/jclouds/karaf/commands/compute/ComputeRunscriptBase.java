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

package org.jclouds.karaf.commands.compute;

import org.apache.felix.gogo.commands.Option;
import org.jclouds.scriptbuilder.domain.Statement;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class ComputeRunscriptBase extends RunScriptBase {


   @Option(name = "-s", aliases = "--script-url", description = "The url script of the script to run.", required = false, multiValued = false)
   private String scriptUrl;

   @Option(name = "-d", aliases = "--direct", description = "A direct command passed to the node to run. Example: jclouds:xxxx-runscript -d uptime xxxx. ", required = false, multiValued = false)
   private String directCommand;

    @Option(name = "-r", aliases = "--run-as-root", description = "Flag to execute script as root. ", required = false, multiValued = false)
    private boolean runAsRoot;

    /**
     * Returns the script to run. If url is specified the script is read from the url.
     */
    public String getScript() {
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
                    builder.append(line).append("\n");
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

    @Override
    public Statement getStatement() {
        return null;
    }

    public boolean runAsRoot() {
        return runAsRoot;
    }
}
