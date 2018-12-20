/*
Copyright (c) 2011 James Ahlborn

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

import com.healthmarketscience.sqlbuilder.dbspec.Constraint;

/**
 * Representation of a (table or column) constraint in a database schema.
 *
 * @author James Ahlborn
 */
public class DbConstraint extends DbObject<DbObject<?>> implements Constraint {

  /** the type for this constraint */
  private final Type _type;
  /** constrained columns */
  private final List<DbColumn> _columns = new ArrayList<DbColumn>();

  public DbConstraint(DbColumn parent, String name, Type type) {
    this((DbObject<?>)parent, name, type);
    _columns.add(parent);
  }

  public DbConstraint(DbTable parent, String name, Type type, 
                      String... colNames) {
    this(parent, name, type, parent.findColumns(colNames));
  }

  public DbConstraint(DbTable parent, String name, Type type, 
                      DbColumn... columns) {
    this(parent, name, type);
    addObjects(_columns, parent, columns);
  }

  private DbConstraint(DbObject<?> parent, String name, Type type) {
    super(parent, name);
    _type = type;
  }

  @Override
  public Type getType() {
    return _type;
  }

  @Override
  public String getConstraintNameSQL() {
    return getName();
  }

  @Override
  public List<DbColumn> getColumns() {
    return _columns;
  }

  @Override
  public String toString() {
    String name = super.toString();
    return "" + getType() + ((name != null) ? (" " + name) : "");
  }
  
}
