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

package org.jclouds.karaf.commands.table.internal;

import java.util.Comparator;

/**
 * This is an updated version with enhancements made by Daniel Migowski,
 * Andre Bogus, and David Koelle.
  */
public class AlphanumericComparator implements Comparator<String>
{
  private final boolean isDigit(char ch)
  {
    return ch >= 48 && ch <= 57;
  }

  /** Length of string is passed in for improved efficiency (only need to calculate it once) **/
  private final String getChunk(String s, int slength, int marker)
  {
    StringBuilder chunk = new StringBuilder();
    char c = s.charAt(marker);
    chunk.append(c);
    marker++;
    if (isDigit(c))
    {
      while (marker < slength)
      {
        c = s.charAt(marker);
        if (!isDigit(c))
          break;
        chunk.append(c);
        marker++;
      }
    } else
    {
      while (marker < slength)
      {
        c = s.charAt(marker);
        if (isDigit(c))
          break;
        chunk.append(c);
        marker++;
      }
    }
    return chunk.toString();
  }

  public int compare(String left, String right)
  {
    int leftMarker = 0;
    int rightMarker = 0;
    int leftLength = left.length();
    int rightLength = right.length();

    while (leftMarker < leftLength && rightMarker < rightLength)
    {
      String leftChunk = getChunk(left, leftLength, leftMarker);
      leftMarker += leftChunk.length();

      String rightChunk = getChunk(right, rightLength, rightMarker);
      rightMarker += rightChunk.length();

      // If both chunks contain numeric characters, sort them numerically
      int result = 0;
      if (isDigit(leftChunk.charAt(0)) && isDigit(rightChunk.charAt(0)))
      {
        // Simple chunk comparison by length.
        int leftChunkLength = leftChunk.length();
        result = leftChunkLength - rightChunk.length();
        // If equal, the first different number counts
        if (result == 0)
        {
          for (int i = 0; i < leftChunkLength; i++)
          {
            result = leftChunk.charAt(i) - rightChunk.charAt(i);
            if (result != 0)
            {
              return result;
            }
          }
        }
      } else
      {
        result = leftChunk.compareTo(rightChunk);
      }

      if (result != 0)
        return result;
    }

    return leftLength - rightLength;
  }
}
