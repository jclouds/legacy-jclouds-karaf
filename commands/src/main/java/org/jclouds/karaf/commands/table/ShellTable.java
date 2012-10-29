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

package org.jclouds.karaf.commands.table;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

public interface ShellTable {

  String LEFT = "left";
  String RIGHT = "right";

  /**
   * Evaluate an expression on the given Object and return a {@link String} repressenation of the result.
   *
   * @param object
   * @param expression
   * @return
   */
  public String evaluate(Object object, String expression);

  /**
   * Displays the content of the table to the specified {@link PrintStream}.
   *
   * @param out
   */
  public void display(PrintStream out, boolean showHeaders, boolean showData);


  //Getters and Setters
  public String getType();

  public void setType(String type);

  public List<String> getHeaders();

  public void setHeaders(List<String> headers);

  public List<String> getDisplayExpression();

  public void setDisplayExpression(List<String> displayExpression);

  public List<String> getAlignments();

  public void setAlignments(List<String> alignments);

  public String getSortBy();

  public void setSortBy(String sortBy);

  public boolean getAscending();

  public void setAscending(boolean ascending);

  public Collection getDisplayData();

  public void setDisplayData(Collection displayData);
}
