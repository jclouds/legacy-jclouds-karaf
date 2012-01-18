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
import javax.annotation.Nullable;
import com.google.common.base.Predicate;
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
@Command(scope = "jclouds", name = "group-runscript")
public class GroupRunScriptCommand extends NodeRunScriptSupport {

    @Argument(index = 0, name = "group", description = "The group of nodes.", required = true, multiValued = false)
    private String group;

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getGroup() {
        return group;
    }
}
