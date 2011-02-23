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

import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.Column;

/**
 * Representation of a column in a database schema.
 *
 * @author James Ahlborn
 */
public class DbColumn extends DbObject<DbTable>
  implements Column
{
  private final String _typeName;
  private final Integer _typeLength;
  private List<DbConstraint> _constraints = new ArrayList<DbConstraint>();

  public DbColumn(DbTable parent, String name,
                  String typeName, Integer typeLength) {
    super(parent, name);
    _typeName = typeName;
    _typeLength = typeLength;
  }

  public DbTable getTable() {
    return getParent();
  }
    
  public String getColumnNameSQL() {
    return getName();
  }
    
  public String getTypeNameSQL() {
    return _typeName;
  }
    
  public Integer getTypeLength() {
    return _typeLength;
  }

  public List<DbConstraint> getConstraints() {
    return _constraints;
  }

  /**
   * Creates and adds not null constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   */
  public DbConstraint notNull() {
    return notNull(null);
  }

  /**
   * Creates and adds not null constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   */
  public DbConstraint notNull(String name) {
    DbConstraint constraint = new DbConstraint(
        this, name, Constraint.Type.NOT_NULL);
    _constraints.add(constraint);
    return constraint;
  }

  /**
   * Creates and adds unique constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   */
  public DbConstraint unique() {
    return unique(null);
  }

  /**
   * Creates and adds unique constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   */
  public DbConstraint unique(String name) {
    DbConstraint constraint = new DbConstraint(
        this, name, Constraint.Type.UNIQUE);
    _constraints.add(constraint);
    return constraint;
  }

  /**
   * Creates and adds primary key constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   */
  public DbConstraint primaryKey() {
    return primaryKey(null);
  }

  /**
   * Creates and adds primary key constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   */
  public DbConstraint primaryKey(String name) {
    DbConstraint constraint = new DbConstraint(
        this, name, Constraint.Type.PRIMARY_KEY);
    _constraints.add(constraint);
    return constraint;
  }

  /**
   * Creates and adds foreign key constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param referencedTableName the name of the referenced table
   */
  public DbForeignKeyConstraint references(String referencedTableName) {
    return references(null, referencedTableName);
  }

  /**
   * Creates and adds foreign key constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   * @param referencedTableName the name of the referenced table
   */
  public DbForeignKeyConstraint references(String name, 
                                           String referencedTableName) {
    return references(name, referencedTableName, null);
  }

  /**
   * Creates and adds foreign key constraint with the given parameters to this
   * column.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   * @param referencedTableName the name of the referenced table
   * @param referencedColName the names of the referenced column
   */
  public DbForeignKeyConstraint references(String name, 
                                           String referencedTableName, 
                                           String referencedColName) {
    DbTable table = getTable().getParent().findTable(referencedTableName);
    DbForeignKeyConstraint fkConstraint = new DbForeignKeyConstraint(
        this, name, table, referencedColName);
    _constraints.add(fkConstraint);
    return fkConstraint;
  }

}
