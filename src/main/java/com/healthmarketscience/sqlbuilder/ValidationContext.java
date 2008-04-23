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

package com.healthmarketscience.sqlbuilder;

import java.util.Collection;
import java.util.HashSet;

import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Table;

/**
 * Object used to accummulate state during query validation.
 *
 * @author james
 */
public class ValidationContext {

  private final ValidationContext _parent;
  private Collection<Column> _columns;
  private Collection<Table> _tables;

  public ValidationContext() {
    this(null, null, null);
  }

  public ValidationContext(ValidationContext parent) {
    this(parent, null, null);
  }
  
  public ValidationContext(Collection<Table> tables,
                           Collection<Column> columns) {
    this(null, tables, columns);
  }
  
  public ValidationContext(ValidationContext parent,
                           Collection<Table> tables,
                           Collection<Column> columns) {
    _parent = parent;
    _tables = ((tables != null) ? tables : new HashSet<Table>());
    _columns = ((columns != null) ? columns : new HashSet<Column>());
  }

  public ValidationContext getParent() {
    return _parent;
  }

  public Collection<Table> getTables() {
    return _tables;
  }

  public void setTables(Collection<Table> newTables) {
    _tables = newTables;
  }

  public void addTable(Table table) {
    _tables.add(table);
  }
  
  public Collection<Column> getColumns() {
    return _columns;
  }

  public void setColumns(Collection<Column> newColumns) {
    _columns = newColumns;
  }
  
  public void addColumn(Column column) {
    _columns.add(column);
  }

  /**
   * Retrieves the tables referenced by the column objects.
   *
   * @return a new columnTables collection
   */
  protected Collection<Table> getColumnTables()
  {
    return getColumnTables(null);
  }
  
  /**
   * Retrieves the tables referenced by the column objects.
   *
   * @param columnTables (out) all tables referenced by the given columns
   * @return the given columnTables collection
   */
  protected Collection<Table> getColumnTables(Collection<Table> columnTables)
  {
    if(columnTables == null) {
      columnTables = new HashSet<Table>();
    }
    // get the tables from the columns referenced
    for(Column column : _columns) {
      columnTables.add(column.getTable());
    }
    return columnTables;
  }
  
}
