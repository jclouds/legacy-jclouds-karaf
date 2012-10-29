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

import com.google.common.base.Strings;
import org.jclouds.karaf.commands.table.internal.AlphanumericComparator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public abstract class BasicShellTable implements ShellTable {

  private String type;
  private List<String> headers;
  private List<String> displayExpression;
  private List<String> alignments;
  private String sortBy;
  private boolean ascending;
  private Collection displayData;

  private final Comparator comparator = new AlphanumericComparator();

  /**
   * Evaluate an expression on the given Object and return a {@link String} repressenation of the result.
   * @param object
   * @param expression
   * @return
   */
  public abstract String evaluate(Object object, String expression);

  /**
   * Displays the content of the table to the specified {@link java.io.PrintStream}.
   * @param out
   */
  public void display(PrintStream out, boolean showHeaders, boolean showData) {
    List<String[]> table = new ArrayList<String[]>();

    //Populate table
    for (Object obj : displayData) {
      String[] values = new String[displayExpression.size()];
      for (int i=0; i < displayExpression.size(); i++) {
        values[i] = evaluate(obj, displayExpression.get(i));
      }
      table.add(values);
    }

    List<Integer> sizes = calculateSizes(headers, table);
    if (!Strings.isNullOrEmpty(sortBy) && headers.contains(sortBy))
      Collections.sort(table, new Comparator<String[]>() {

        @Override
        public int compare(String[] left, String[] right) {
          int column = headers.indexOf(sortBy);
          String leftValue = left[column];
          String rightValue = right[column];
          return comparator.compare(leftValue,rightValue);
        }
      });

    String dataFormat = getStringFormat(sizes, alignments);
    //Check if we need to display headers
    if (showHeaders) {
      out.println(String.format(dataFormat, headers.toArray(new String[headers.size()])));
    }

    //Check if we need and can display data.
    if (showData && table != null) {
      for (String[] row : table) {
        //Populate display data
        out.println(String.format(dataFormat, row));
      }
    }
  }

  /**
   * Scans the table and calculates the sizes.
   * @param headers
   * @param displayData
   * @return
   */
  private List<Integer> calculateSizes(List<String> headers, List<String[]> displayData) {
    List<Integer> sizes = new LinkedList<Integer>();
    for (int i=0; i < headers.size(); i ++) {
      String header = headers.get(i);
      int size = header.length();
      for (String[] row : displayData) {
        String value = row[i];
        int valueSize = value.length();
        size = size > valueSize ? size : valueSize;
      }
      sizes.add(size);
    }
    return sizes;
  }

  private String getStringFormat(List<Integer> sizes, List<String> alignments) {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i < sizes.size(); i++) {
      //Add a separator
      if (i > 0) {
        sb.append(" ");
      }

      int size = sizes.get(i);
      String alignment = alignments != null && alignments.size() > i ? alignments.get(i) : LEFT;

      if (RIGHT.equals(alignment)){
        sb.append("%").append(size).append("s");
      } else {
        sb.append("%-").append(size).append("s");
      }
    }
    return sb.toString();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  //Getters and Setters
  public List<String> getHeaders() {
    return headers;
  }

  public void setHeaders(List<String> headers) {
    this.headers = headers;
  }

  public List<String> getDisplayExpression() {
    return displayExpression;
  }

  public void setDisplayExpression(List<String> displayExpression) {
    this.displayExpression = displayExpression;
  }

  public List<String> getAlignments() {
    return alignments;
  }

  public void setAlignments(List<String> alignments) {
    this.alignments = alignments;
  }

  public String getSortBy() {
    return sortBy;
  }

  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  public boolean getAscending() {
    return ascending;
  }

  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }

  public Collection getDisplayData() {
    return displayData;
  }

  public void setDisplayData(Collection displayData) {
    this.displayData = displayData;
  }
}
