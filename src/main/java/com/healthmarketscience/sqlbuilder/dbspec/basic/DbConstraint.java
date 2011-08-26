/*
Copyright (c) 2011 James Ahlborn

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
  private List<DbColumn> _columns = new ArrayList<DbColumn>();

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

  public Type getType() {
    return _type;
  }

  public String getConstraintNameSQL() {
    return getName();
  }

  public List<DbColumn> getColumns() {
    return _columns;
  }

  @Override
  public String toString() {
    String name = super.toString();
    return "" + getType() + ((name != null) ? (" " + name) : "");
  }
  
}
