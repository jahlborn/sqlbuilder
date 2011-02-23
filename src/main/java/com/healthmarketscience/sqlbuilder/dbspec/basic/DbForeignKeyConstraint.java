// Copyright (c) 2011 James Ahlborn

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
    super(parent, name, Type.FOREIGN_KEY);
    _referencedTable = referencedTable;
    initReferencedColumns(refColName);
  }

  public DbForeignKeyConstraint(DbTable parent, String name,
                                DbTable referencedTable,
                                String[] colNames, String[] refColNames) {
    super(parent, name, Type.FOREIGN_KEY, colNames);
    _referencedTable = referencedTable;
    initReferencedColumns(refColNames);
  }

  /**
   * Updates _referencedColumns with the columns from the _referencedTable
   * with the given names.
   */
  private void initReferencedColumns(String... refColNames) {
    if(refColNames != null) {
      for(String refColName : refColNames) {
        _referencedColumns.add(_referencedTable.findColumn(refColName));
      }
    }
  }

  public DbTable getReferencedTable() {
    return _referencedTable;
  }

  public List<DbColumn> getReferencedColumns() {
    return _referencedColumns;
  }

}
