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

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Index;
import com.healthmarketscience.sqlbuilder.dbspec.Table;

/**
 * Query which generates a CREATE INDEX statement.
 * 
 * @author Tim McCune
 */
public class CreateIndexQuery extends BaseCreateQuery<CreateIndexQuery>
{
  
  protected SqlObject _table;

  public CreateIndexQuery(Index index) {
    this((Object)index.getTable(), (Object)index);

    // add all the columns for this table
    _columns.addObjects(Converter.COLUMN_TO_OBJ, index.getColumns());
  }
  
  public CreateIndexQuery(Table table, String indexName) {
    this((Object)table, (Object)indexName);
  }
  
  public CreateIndexQuery(Table table, Object index) {
    this((Object)table, index);
  }
  
  /**
   * Index {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomIndexSqlObject(Object)}.
   * Table {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public CreateIndexQuery(Object tableStr, Object index) {
    super(Converter.toCustomIndexSqlObject(index));
    _table = Converter.toCustomTableSqlObject(tableStr);
  }

  /**
   * Sets the name of the table which is being indexed.
   */
  public CreateIndexQuery setTableName(String nameName) {
    return setCustomTableName(nameName);
  }

  /**
   * Sets the name of the table which is being indexed.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public CreateIndexQuery setCustomTableName(Object name) {
    _table = Converter.toCustomTableSqlObject(name);
    return this;
  }

  @Override
  public DropQuery getDropQuery() {
    return new DropQuery(DropQuery.Type.INDEX, _object);
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   */
  @Override
  public CreateIndexQuery addCustomColumns(Object... columnStrs) {
    _columns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    return this;
  }
  
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _table.collectSchemaObjects(vContext);
  }

  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    app.append("CREATE INDEX ").append(_object).append(" ON ").append(_table)
        .append(" (").append(_columns).append(")");
    appendTableSpace(app);
  }
   
}
