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
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Table;



/**
 * Query which generates an INSERT statement where the data is generated from
 * a SELECT query.
 *
 * @author James Ahlborn
 */
public class InsertSelectQuery extends BaseInsertQuery<InsertSelectQuery>
{
  private SelectQuery _selectQuery;

  /** @param table table into which to insert the values. */
  public InsertSelectQuery(Table table) {
    this((Object)table);
  }

  /**
   * @param tableStr name of the table into which to insert the values.
   *
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public InsertSelectQuery(Object tableStr) {
    super(Converter.toCustomTableSqlObject(tableStr));
  }

  /** Sets the SELECT query which will generate the data to insert into the
      table */
  public InsertSelectQuery setSelectQuery(SelectQuery selectQuery) {
    _selectQuery = selectQuery;
    return this;
  }

  /**
   * Adds the given columns to the query
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   */
  public InsertSelectQuery addCustomColumns(Object... columnStrs)
  {
    _columns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    return this;
  }
    
  /** Adds the given columns to the query */
  public InsertSelectQuery addColumns(Column... columns) {
    return addCustomColumns((Object[])columns);
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);

    // treat select query as a separate subquery
    vContext.collectNestedQuerySchemaObjects(_selectQuery);
  }
    
  @Override
  public void validate(ValidationContext vContext)
    throws ValidationException
  {
    // check super
    super.validate(vContext);
      
    if(_selectQuery == null) {
      throw new ValidationException("missing select query");
    }
    SqlObjectList<SqlObject> selectColumns = _selectQuery.getColumns();

    // if we are using the "*" syntax, then we can't really compare the number
    // of columns
    if((_columns.size() != selectColumns.size()) &&
       !_selectQuery.hasAllColumns()) {
      throw new ValidationException(
          "mismatched columns and select columns for insert");
    }
  }

  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    appendPrefixTo(app);
    app.append(_selectQuery);
  }
  
}
