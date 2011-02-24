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

package com.healthmarketscience.sqlbuilder.dbspec;

import java.util.ArrayList;

import java.util.List;

/**
 * Utility class for using a table multiple times in the same query with
 * different aliases.  All Columns returned by this class will also use the
 * new alias.  All other methods return information from the original table.
 *
 * @author James Ahlborn
 */
public class RejoinTable implements Table
{
  /** the original table */
  private Table _table;
  /** the new alias to use for this table */
  private String _alias;
  /** the wrapped columns from the original table */
  private List<RejoinColumn> _columns;

  
  public RejoinTable(Table table, String alias) {
    _table = table;
    _alias = alias;
    _columns = new ArrayList<RejoinColumn>(_table.getColumns().size());
    for(Column column : _table.getColumns()) {
      _columns.add(new RejoinColumn(column));
    }
  }

  public Table getOriginalTable() { return _table; }
  
  public String getAlias() { return _alias; }
  
  public String getTableNameSQL() { return _table.getTableNameSQL(); }

  public List<RejoinColumn> getColumns() { return _columns; }

  public List<? extends Constraint> getConstraints() { return _table.getConstraints(); }

  public RejoinColumn findColumnByName(String name) {
    for(RejoinColumn col : getColumns()) {
      if((name == col.getColumnNameSQL()) ||
         ((name != null) && name.equals(col.getColumnNameSQL()))) {
        return col;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "Rejoin: " + getOriginalTable().toString() + "(" + getAlias() + ")";
  }
  
  /**
   * Utility class which wraps a Column and returns a reference to the
   * RejoinTable instead of the original table.  All other methods return the
   * information from the original column.
   */
  public class RejoinColumn implements Column
  {
    /** the original column object */
    private Column _column;

    private RejoinColumn(Column column) {
      _column = column;
    }

    public Column getOriginalColumn() { return _column; }
    
    public RejoinTable getTable() { return RejoinTable.this; }
  
    public String getColumnNameSQL() { return _column.getColumnNameSQL(); }

    public String getTypeNameSQL() { return _column.getTypeNameSQL(); }

    public Integer getTypeLength() { return _column.getTypeLength(); }

    public List<? extends Constraint> getConstraints() { return _column.getConstraints(); }

    @Override
    public String toString() {
      return "Rejoin: " + getOriginalColumn().toString() +
        "(" + getTable() + ")";
    }
  }
  
}
