/*
Copyright (c) 2008 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
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
