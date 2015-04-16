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
