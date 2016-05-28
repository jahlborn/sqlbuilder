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

import com.healthmarketscience.sqlbuilder.dbspec.ForeignKeyConstraint;

/**
 * Representation of a (table or column) foreign key constraint in a database
 * schema.
 *
 * @author James Ahlborn
 */
public class DbForeignKeyConstraint extends DbConstraint
  implements ForeignKeyConstraint
{
  /** the table which is referenced */
  private final DbTable _referencedTable;
  /** the columns of the table which are referenced */
  private final List<DbColumn> _referencedColumns = new ArrayList<DbColumn>();

  public DbForeignKeyConstraint(DbColumn parent, String name, 
                                DbTable referencedTable, String refColName) {
    this(parent, name, referencedTable, referencedTable.findColumn(refColName));
  }

  public DbForeignKeyConstraint(DbColumn parent, String name, 
                                DbTable referencedTable, DbColumn refColumn) {
    super(parent, name, Type.FOREIGN_KEY);
    _referencedTable = referencedTable;
    addObjects(_referencedColumns, _referencedTable, refColumn);
  }

  public DbForeignKeyConstraint(DbTable parent, String name,
                                DbTable referencedTable,
                                String[] colNames, String[] refColNames) {
    this(parent, name, referencedTable, parent.findColumns(colNames),
         referencedTable.findColumns(refColNames));
  }

  public DbForeignKeyConstraint(DbTable parent, String name,
                                DbTable referencedTable,
                                DbColumn[] columns, DbColumn[] refColumns) {
    super(parent, name, Type.FOREIGN_KEY, columns);
    _referencedTable = referencedTable;
    addObjects(_referencedColumns, _referencedTable, refColumns);
  }

  public DbTable getReferencedTable() {
    return _referencedTable;
  }

  public List<DbColumn> getReferencedColumns() {
    return _referencedColumns;
  }

}
