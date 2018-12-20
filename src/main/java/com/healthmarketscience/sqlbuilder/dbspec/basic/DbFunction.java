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

import com.healthmarketscience.sqlbuilder.dbspec.Function;

/**
 * Representation of a function in a database function package.
 *
 * @author James Ahlborn
 */
public class DbFunction extends DbObject<DbFunctionPackage>
  implements Function {

  public DbFunction(DbFunctionPackage parent, String name) {
    super(parent, name);
  }

  public DbFunctionPackage getFunctionPackage() {
    return getParent();
  }

  @Override
  public String getFunctionNameSQL() {
    return getAbsoluteName();
  }
     
}
