/*
Copyright (c) 2008 Health Market Science, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.healthmarketscience.sqlbuilder.dbspec.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a group of functions in a database schema (similar to a
 * schema for functions).  For databases which do not support function
 * packages or for builtin functions, the default function package can be used
 * as it is largely transparent.
 *
 * @author James Ahlborn
 */
public class DbFunctionPackage extends DbObject<DbSchema> {
  
  /** functions currently created for this db spec */
  private final List<DbFunction> _functions = new ArrayList<DbFunction>();

  public DbFunctionPackage(DbSchema parent, String name) {
    super(parent, name);
  }

  public DbSchema getSchema() {
    return getParent();
  }
    
  /**
   * @param name name of the function to find
   * @return the function previously added to this package with the given
   *         name, or {@code null} if none.
   */
  public DbFunction findFunction(String name) {
    return findObject(_functions, name);
  }

  /**
   * Creates and adds a function with the given name to this package.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new function
   * @return the freshly created function
   */
  public DbFunction addFunction(String name) {
    DbFunction function = getSpec().createFunction(this, name);
    return addFunction(function);
  }    

  /**
   * Adds the given function to this package.
   * <p>
   * Note, no effort is made to make sure the given function is unique.
   * @param function the function to be added
   * @return the given function
   */
  public <T extends DbFunction> T addFunction(T function) {
    _functions.add(checkOwnership(function));
    return function;
  }    

}
