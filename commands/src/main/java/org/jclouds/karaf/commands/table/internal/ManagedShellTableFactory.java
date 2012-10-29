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

package org.jclouds.karaf.commands.table.internal;

import org.jclouds.karaf.commands.table.BasicShellTableFactory;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import java.util.Dictionary;
import java.util.Enumeration;

public class ManagedShellTableFactory extends BasicShellTableFactory implements ManagedService {

  /**
   * Update the configuration for a Managed Service.
   * <p/>
   * <p/>
   * When the implementation of <code>updated(Dictionary)</code> detects any
   * kind of error in the configuration properties, it should create a new
   * <code>ConfigurationException</code> which describes the problem. This
   * can allow a management system to provide useful information to a human
   * administrator.
   * <p/>
   * <p/>
   * If this method throws any other <code>Exception</code>, the
   * Configuration Admin service must catch it and should log it.
   * <p/>
   * The Configuration Admin service must call this method asynchronously
   * which initiated the callback. This implies that implementors of Managed
   * Service can be assured that the callback will not take place during
   * registration when they execute the registration in a synchronized method.
   *
   * @param properties A copy of the Configuration properties, or
   *                   <code>null</code>. This argument must not contain the
   *                   "service.bundleLocation" property. The value of this property may
   *                   be obtained from the <code>Configuration.getBundleLocation</code>
   *                   method.
   * @throws org.osgi.service.cm.ConfigurationException
   *          when the update fails
   */
  @Override
  public void updated(Dictionary properties) throws ConfigurationException {
    if (properties != null) {
      Enumeration keys = properties.keys();
      getProperties().clear();
      while (keys.hasMoreElements()) {
        Object key = keys.nextElement();
        Object value = properties.get(key);
        getProperties().put(key, value);
      }
    }
  }
}
