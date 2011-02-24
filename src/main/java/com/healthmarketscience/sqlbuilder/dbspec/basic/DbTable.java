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
import com.healthmarketscience.sqlbuilder.dbspec.Table;

/**
 * Representation of a table in a database schema.
 *
 * @author James Ahlborn
 */
public class DbTable extends DbObject<DbSchema> implements Table {

  /** alias to use for this table in queries (should be unique) */
  private String _alias;
  /** columns currently created for this table */
  private List<DbColumn> _columns = new ArrayList<DbColumn>();
  /** constraints currently defined for this table */
  private List<DbConstraint> _constraints = new ArrayList<DbConstraint>();

  public DbTable(DbSchema parent, String name) {
    super(parent, name);
    _alias = parent.getSpec().getNextAlias();
  }

  public String getAlias() {
    return _alias;
  }
    
  public String getTableNameSQL() {
    return getAbsoluteName();
  }

  public List<DbColumn> getColumns() {
    return _columns;
  }

  public List<DbConstraint> getConstraints() {
    return _constraints;
  }
    
  /**
   * @param name name of the column to find
   * @return the column previously added to this table with the given name, or
   *         {@code null} if none.
   */
  public DbColumn findColumn(String name) {
    return findObject(_columns, name);
  }

  /**
   * Creates and adds an untyped column with the given name to this table.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new column
   * @return the freshly created column
   */
  public DbColumn addColumn(String name) {
    return addColumn(name, null, null);
  }
  
  /**
   * Creates and adds an typed column with the given parameters to this table.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new column
   * @param typeName type name for the column
   * @param typeLength optional length specification for the column
   * @return the freshly created column
   */
  public DbColumn addColumn(String name, String typeName, Integer typeLength) {
    DbColumn column = new DbColumn(this, name, typeName, typeLength);
    _columns.add(column);
    return column;
  }    

  /**
   * Creates and adds unique constraint with the given parameters to this
   * table.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   * @param colNames the name of the constrained columns
   */
  public DbConstraint unique(String name, String... colNames) {
    DbConstraint constraint = new DbConstraint(
        this, name, Constraint.Type.UNIQUE, colNames);
    _constraints.add(constraint);
    return constraint;
  }

  /**
   * Creates and adds primary key constraint with the given parameters to this
   * table.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   * @param colNames the name of the constrained columns
   */
  public DbConstraint primaryKey(String name, String... colNames) {
    DbConstraint constraint = new DbConstraint(
        this, name, Constraint.Type.PRIMARY_KEY, colNames);
    _constraints.add(constraint);
    return constraint;
  }

  /**
   * Creates and adds foreign key constraint with the given parameters to this
   * table.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new constraint
   * @param colNames the name of the constrained columns
   * @param referencedSchemaName the name of the referenced schema
   * @param referencedTableName the name of the referenced table
   * @param referencedColNames the names of the referenced columns
   */
  public DbForeignKeyConstraint foreignKey(String name, String[] colNames, 
                                           String referencedSchemaName,
                                           String referencedTableName,
                                           String[] referencedColNames)
  {
    DbTable referencedTable = getSpec().findSchema(referencedSchemaName)
      .findTable(referencedTableName);
    DbForeignKeyConstraint fkConstraint = new DbForeignKeyConstraint(
        this, name, referencedTable, colNames, referencedColNames);
    _constraints.add(fkConstraint);
    return fkConstraint;
  }

  @Override
  public String toString() {
    return super.toString() + "(" + getAlias() + ")";
  }
  
}
