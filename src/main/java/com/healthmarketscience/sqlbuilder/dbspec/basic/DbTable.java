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

import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.Table;

/**
 * Representation of a table in a database schema.
 *
 * @author James Ahlborn
 */
public class DbTable extends DbObject<DbSchema> implements Table {

  /** alias to use for this table in queries (should be unique) */
  private final String _alias;
  /** columns currently created for this table */
  private final List<DbColumn> _columns = new ArrayList<DbColumn>();
  /** constraints currently defined for this table */
  private final List<DbConstraint> _constraints = new ArrayList<DbConstraint>();

  public DbTable(DbSchema parent, String name) {
    this(parent, name, parent.getSpec().getNextAlias());
  }

  public DbTable(DbSchema parent, String name, String alias) {
    super(parent, name);
    _alias = alias;
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
   * @param names name(s) of the column(s) to find
   * @return the column(s) previously added to this table with the given
   *         name(s), or {@code null} if none.
   */
  public DbColumn[] findColumns(String... names) {
    if(names == null) {
      return null;
    }
    DbColumn[] cols = new DbColumn[names.length];
    for(int i = 0; i < names.length; ++i) {
      cols[i] = findObject(_columns, names[i]);
    }
    return cols;
  }

  /**
   * Creates and adds an untyped column with the given name to this table.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new column
   * @return the freshly created column
   */
  public DbColumn addColumn(String name) {
    return addColumn(name, null, null, null);
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
    return addColumn(name, typeName, typeLength, null);
  }    

  /**
   * Creates and adds an typed column with the given parameters to this table.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new column
   * @param typeName type name for the column
   * @param typePrecision optional precision specification for the column
   * @param typeScale optional scale specification for the column
   * @return the freshly created column
   */
  public DbColumn addColumn(String name, String typeName, 
                            Integer typePrecision, Integer typeScale) {
    DbColumn column = getSpec().createColumn(this, name, typeName,
                                             typePrecision, typeScale);
    return addColumn(column);
  }    

  /**
   * Creates and adds an typed column with the given parameters to this table.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new column
   * @param type type for the column (one of {@link java.sql.Types})
   * @param typeLength optional length specification for the column
   * @return the freshly created column
   */
  public DbColumn addColumn(String name, int type, Integer typeLength) {
    return addColumn(name, type, typeLength, null);
  }    

  /**
   * Creates and adds an typed column with the given parameters to this table.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param name the name of the new column
   * @param type type for the column (one of {@link java.sql.Types})
   * @param typePrecision optional precision specification for the column
   * @param typeScale optional scale specification for the column
   * @return the freshly created column
   */
  public DbColumn addColumn(String name, int type, Integer typePrecision, 
                            Integer typeScale) {
    return addColumn(name, DbColumn.getTypeName(type), typePrecision, 
                     typeScale);
  }    

  /**
   * Adds the given column to this table.
   * <p>
   * Note, no effort is made to make sure the column is unique.
   * @param column the column to be added
   * @return the given column
   */
  public <T extends DbColumn> T addColumn(T column) {
    _columns.add(checkOwnership(column));
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
    DbConstraint constraint = getSpec().createTableConstraint(
        this, name, Constraint.Type.UNIQUE, colNames);
    return addConstraint(constraint);
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
    DbConstraint constraint = getSpec().createTableConstraint(
        this, name, Constraint.Type.PRIMARY_KEY, colNames);
    return addConstraint(constraint);
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
    DbForeignKeyConstraint fkConstraint =
      getSpec().createTableForeignKeyConstraint(
        this, name, referencedTable, colNames, referencedColNames);
    return addConstraint(fkConstraint);
  }

  /**
   * Creates and adds check constraint with the given parameters to this
   * table.
   * <p>
   * Note, no effort is made to make sure the given name is unique.
   * @param condition the check condition
   */
  public DbCheckConstraint checkCondition(String name, Condition condition) {
    DbCheckConstraint constraint = getSpec().createTableCheckConstraint(
        this, name, condition);
    return addConstraint(constraint);
  }

  /**
   * Adds the given constraint to this table.
   * <p>
   * Note, no effort is made to make sure the given constraint is unique.
   * @param constraint the constraint to be added
   * @return the given constraint
   */
  public <T extends DbConstraint> T addConstraint(T constraint) {
    _constraints.add(checkOwnership(constraint));
    return constraint;
  }
  
  @Override
  public String toString() {
    return super.toString() + "(" + getAlias() + ")";
  }
  
}
