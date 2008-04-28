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

import com.healthmarketscience.common.util.Tuple2;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import java.util.ArrayList;

/**
 * Object used to accummulate state during query validation.
 *
 * @author james
 */
public class ValidationContext {

  public static final boolean DEFAULT_LOCAL_ONLY = false;
  
  private final ValidationContext _parent;

  private Collection<Column> _columns;
  private Collection<Table> _tables;
  /** whether or not collection/validation should proceed into nested
      subqueries */
  private boolean _localOnly;
  private Collection<Tuple2<ValidationContext,Verifiable>> _verifiables;

  public ValidationContext() {
    this(null, null, null, DEFAULT_LOCAL_ONLY);
  }

  public ValidationContext(ValidationContext parent) {
    this(parent, null, null, DEFAULT_LOCAL_ONLY);
  }
  
  public ValidationContext(boolean localOnly) {
    this(null, null, null, localOnly);
  }
  
  public ValidationContext(Collection<Table> tables,
                           Collection<Column> columns) {
    this(null, tables, columns, DEFAULT_LOCAL_ONLY);
  }
  
  public ValidationContext(ValidationContext parent,
                           Collection<Table> tables,
                           Collection<Column> columns,
                           boolean localOnly) {
    _parent = parent;
    _tables = ((tables != null) ? tables : new HashSet<Table>());
    _columns = ((columns != null) ? columns : new HashSet<Column>());
    _localOnly = localOnly;
    _verifiables = ((_parent != null) ? _parent._verifiables :
                    new ArrayList<Tuple2<ValidationContext,Verifiable>>(2));
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

  public boolean isLocalOnly() {
    return _localOnly;
  }

  public void setLocalOnly(boolean newLocalOnly) {
    _localOnly = newLocalOnly;
  }

  public void addVerifiable(Verifiable verifiable)
  {
    if(verifiable == null) {
      throw new IllegalArgumentException("verifiable was null");
    }
    _verifiables.add(Tuple2.create(this, verifiable));
  }

  public void validateAll() throws ValidationException {
    for(Tuple2<ValidationContext,Verifiable> verifiable : _verifiables) {
      try {
        verifiable.get1().validate(verifiable.get0());
      } catch(ValidationException e) {
        e.setFailedVerifiable(verifiable);
        throw e;
      }
    }
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
