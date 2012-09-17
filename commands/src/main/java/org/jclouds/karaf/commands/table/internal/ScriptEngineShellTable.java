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

import org.jclouds.karaf.commands.table.BasicShellTable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * A shell table implementation that works with groovy expressions.
 */
public class ScriptEngineShellTable extends BasicShellTable {

  private final String engine;
  private final ScriptEngineManager scriptEngineFactory = new ScriptEngineManager();
  private final ScriptEngine scriptEngine;

  /**
   * Constructor
   * @param engine
   */
  public ScriptEngineShellTable(String engine) {
    this.engine = engine;
    this.scriptEngine = scriptEngineFactory.getEngineByName(engine);
  }

  /**
   * Evaluates an expression.
   * @param obj
   * @param expression
   * @return
   */
  public String evaluate(Object obj, String expression) {
    String result = "";
    try {
      scriptEngine.put(getType(), obj);
      result = String.valueOf(scriptEngine.eval(expression));
    } catch (Exception ex) {
      //Ignore
    }
    return result;
  }
}
