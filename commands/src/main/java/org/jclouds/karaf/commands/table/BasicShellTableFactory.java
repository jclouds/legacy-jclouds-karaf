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

package org.jclouds.karaf.commands.table;

import org.jclouds.karaf.commands.table.internal.GroovyShellTable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A basic {@link org.jclouds.karaf.commands.table.ShellTableFactory} implementation which is backed by a {@link Map}.
 */
public class BasicShellTableFactory implements ShellTableFactory {

   private final Map properties = new HashMap();
  /**
   * Creates a table based on the values of the dictionary
   *
   * @param type
   * @return
   */
  @Override
  public ShellTable build(String type) {
      String headersValue = String.valueOf(properties.get(type + "." + HEADERS_KEY));
      String expressionsValue = String.valueOf(properties.get(type + "." + EXPRESSIONS_KEY));
      String alignValue = String.valueOf(properties.get(type + "." + ALIGN_KEY));
      String shortByValue = String.valueOf(properties.get(type + "." + SHORTBY_KEY));
      Boolean ascendingValue = Boolean.parseBoolean(String.valueOf(properties.get(type + "." + ASCENDING_KEY)));

      List<String> headers =  Arrays.asList(headersValue.split(","));
      List<String> expressions =  Arrays.asList(expressionsValue.split(","));
      List<String> alignments =  Arrays.asList(alignValue.split(","));
      //TODO: Make it easier to have the implementation pluggable / configurable.
      ShellTable shellTable = new GroovyShellTable();

      shellTable.setType(type);
      shellTable.setHeaders(headers);
      shellTable.setDisplayExpression(expressions);
      shellTable.setAlignments(alignments);
      shellTable.setSortBy(shortByValue);
      shellTable.setAscending(ascendingValue);
      return shellTable;
  }

  public Map getProperties() {
    return properties;
  }
}
