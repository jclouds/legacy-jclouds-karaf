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

import org.jclouds.karaf.commands.table.BasicShellTableFactory;
import org.jclouds.karaf.commands.table.ShellTableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class PropertyShellTableFactory extends BasicShellTableFactory implements ShellTableFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(PropertyShellTableFactory.class);

  private final String PROPERTIES_FILE_PATH = System.getProperty("karaf.home") + File.separatorChar + "etc" + File.separatorChar + "org.jclouds.shell.cfg";

  public PropertyShellTableFactory() {
    load();
  }

  private void load() {
    Properties properties = new Properties();
    File f = new File(PROPERTIES_FILE_PATH);
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(f);
      properties.load(fis);
      Enumeration keys = properties.keys();
      while (keys.hasMoreElements()) {
        Object key = keys.nextElement();
        Object value = properties.get(key);
        getProperties().put(key, value);
      }
    } catch (IOException e) {
      LOGGER.warn("Failed to load jclouds shell configuration from file.", e);
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (Throwable t) {
        }
      }
    }
  }

}
