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

import java.io.IOException;
import java.util.Collection;

import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import com.healthmarketscience.common.util.AppendableExt;

/**
 * Query which generates an {@code ALTER TABLE} statement.
 *
 * @author James Ahlborn
 */
public class AlterTableQuery extends Query
{

  private SqlObject _table;
  private SqlObject _action;
  
  public AlterTableQuery(Table table) {
    this((Object)table);
  }

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public AlterTableQuery(Object tableStr) {
    _table = Converter.toCustomTableSqlObject(tableStr);
  }

  /**
   * Sets the alter table action.
   *
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject(Object)}.
   */
  public AlterTableQuery setAction(Object action) {
    _action = Converter.toCustomSqlObject(action);
    return this;
  }

  @Override
  protected void collectSchemaObjects(Collection<Table> tables,
                                  Collection<Column> columns) {
    _table.collectSchemaObjects(tables, columns);
    _action.collectSchemaObjects(tables, columns);
  }
  
  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    app.append("ALTER TABLE ").append(_table).append(_action);
  }

  /**
   * "Action" for adding a unique constraint to a table.,
   * e.g. {@code "... ADD UNIQUE (<col1> ... [<coln>])}.
   */
  public static class AddUniqueConstraintAction extends SqlObject
  {
    private SqlObjectList<SqlObject> _columns = SqlObjectList.create();

    /**
     * Adds a column to the unique constraint definition.
     */
    public AddUniqueConstraintAction addColumns(Column... columns) {
      return addCustomColumns((Object[])columns);
    }

    /**
     * Adds a custom column to the unique constraint definition.
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
     */
    public AddUniqueConstraintAction addCustomColumns(Object... columnStrs) {
      _columns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
      return this;
    }

    @Override
    protected void collectSchemaObjects(Collection<Table> tables,
                                    Collection<Column> columns) {
      _columns.collectSchemaObjects(tables, columns);
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append(" ADD UNIQUE ").append("(").append(_columns).append(")");
    }
  }
  
  /**
   * "Action" for adding a primary key constraint to a table,
   * e.g. {@code "... ADD PRIMARY KEY (<col1> ... [<coln>])}.
   */
  public static class AddPrimaryConstraintAction extends SqlObject
  {
    private SqlObjectList<SqlObject> _columns = SqlObjectList.create();
    
    /**
     * Adds a column to the primary key constraint definition.
     */
    public AddPrimaryConstraintAction addColumns(Column... columns) {
      return addCustomColumns((Object[])columns);
    }

    /**
     * Adds a custom column to the primary key constraint definition.
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
     */
    public AddPrimaryConstraintAction addCustomColumns(Object... columnStrs) {
      _columns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
      return this;
    }

    @Override
    protected void collectSchemaObjects(Collection<Table> tables,
                                    Collection<Column> columns) {
      _columns.collectSchemaObjects(tables, columns);
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append(" ADD PRIMARY KEY ").append("(").append(_columns).append(")");
    }
  }


  /**
   * "Action" for adding a foreign key constraint to a table,
   * e.g. 
   * {@code "... ADD FOREIGN KEY (<c1>...[<cn>]) REFERENCES t2 [(<c1>...<cn>)]}.
   */
  public static class AddForeignConstraintAction extends SqlObject
  {

    /** The table referenced by this constraint */
    private SqlObject _referencedTable;

    /** Columns in the referencing table */
    private SqlObjectList<SqlObject> _columns = SqlObjectList.create();
    
    /** Columns in the referenced table */    
    private SqlObjectList<SqlObject> _referencedColumns =
      SqlObjectList.create();

    /** 
     * Creates a new {@link AddForeignConstraintAction} which references
     * the given {@link Table}.
     */
    public AddForeignConstraintAction(Table table) {
      this((Object)table);
    }

    /** 
     * Creates a new {@link AddForeignConstraintAction} which references
     * the given {@link Table}.
     *
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#toCustomTableSqlObject(Object)}.
     */
    public AddForeignConstraintAction(Object object) {
      _referencedTable = Converter.toCustomTableSqlObject(object);
    }

    /** 
     * Adds {@code col} as a reference to the primary key in the referenced
     * table.
     */
    public AddForeignConstraintAction addPrimaryKeyReference(Column col) {
      return addReference(col, null);
    }

    /** 
     * Adds {@code col} as a reference to the primary key in the referenced
     * table.
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
     */
    public AddForeignConstraintAction addCustomPrimaryKeyReference(Object col){
      return addCustomReference((Object)col, null);
    }

    /**
     * Adds {@code col} as a reference to {@code referencedCol} in the
     * referenced table.
     */
    public AddForeignConstraintAction addReference(
        Column col, Column referencedCol) {
      return addCustomReference((Object)col, (Object)referencedCol);
    }

    /**
     * Adds {@code col} as a reference to {@code referencedCol} in the
     * referenced table.  If referencedCol is {@code null}, it is ignored
     * (useful for referencing the primary key in the referenced table).
     * <p>
     * {@code Object} -&gt; {@code SqlObject} conversions handled by
     * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
     */
    public AddForeignConstraintAction addCustomReference(
        Object col, Object referencedCol) {
      _columns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, col);
      if (referencedCol != null) {
        _referencedColumns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ,
                                      referencedCol);
      }
      return this;
    }

    @Override
    protected void collectSchemaObjects(Collection<Table> tables,
                                    Collection<Column> columns) {
      _columns.collectSchemaObjects(tables, columns);
      _referencedColumns.collectSchemaObjects(tables, columns);
      _referencedTable.collectSchemaObjects(tables, columns);
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append(" ADD FOREIGN KEY ")
        .append("(").append(_columns).append(") ")
        .append("REFERENCES ")
        .append(_referencedTable);
      if (!_referencedColumns.isEmpty()) {
        app.append(" (").append(_referencedColumns).append(")");
      }
    }
  }
  
}
