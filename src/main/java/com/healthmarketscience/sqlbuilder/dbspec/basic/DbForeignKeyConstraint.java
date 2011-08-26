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
    addObjects(_referencedColumns, _referencedTable,
               _referencedTable.findColumn(refColName));
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
