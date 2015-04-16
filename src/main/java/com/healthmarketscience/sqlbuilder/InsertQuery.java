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
import java.util.Arrays;
import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import java.util.Collection;


/**
 * Query which generates a simple INSERT statement.
 *
 * @author James Ahlborn
 */
public class InsertQuery extends BaseInsertQuery<InsertQuery>
{
  private SqlObjectList<SqlObject> _values = SqlObjectList.create();
    
  /** @param table table into which to insert the values. */
  public InsertQuery(Table table) {
    this((Object)table);
  }

  /**
   * @param tableStr name of the table into which to insert the values.
   *
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public InsertQuery(Object tableStr) {
    super(Converter.toCustomTableSqlObject(tableStr));
  }
  
  /**
   * Adds the given column and its corresponding value to the query.
   * @param columnStr {@code Object} -&gt; {@code SqlObject} conversions
   *                  handled by {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   * @param value {@code Object} -&gt; {@code SqlObject} conversions
   *              handled by {@link Converter#VALUE_TO_OBJ}.
   */
  public InsertQuery addCustomColumn(Object columnStr, Object value) {
    return addCustomColumns(new Object[]{columnStr},
                            new Object[]{value});
  }

  /**
   * Adds the given columns and their corresponding values to the query.
   * Arrays must be the same length.
   * @param columnStrs {@code Object} -&gt; {@code SqlObject} conversions
   *                   handled by {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   * @param values {@code Object} -&gt; {@code SqlObject} conversions
   *               handled by {@link Converter#VALUE_TO_OBJ}.
   */
  public InsertQuery addCustomColumns(Object[] columnStrs,
                                      Object[] values)
  {
    _columns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    _values.addObjects(Converter.VALUE_TO_OBJ, values);
    return this;
  }
    
  /** Adds the given column and its corresponding value to the query. */
  public InsertQuery addColumn(Column column, Object value) {
    return addCustomColumn(column, value);
  }

  /** Adds the given columns and their corresponding values to the query.
      Arrays must be the same length. */
  public InsertQuery addColumns(Column[] columns, Object[] values) {
    return addCustomColumns(columns, values);
  }

  /**
   * Adds the given columns and an equal number of QUESTION_MARK values to
   * the query.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   */
  public InsertQuery addCustomPreparedColumns(Object... columnStrs) {
    SqlObject[] values = null;
    if(columnStrs != null) {
      values = new SqlObject[columnStrs.length];
      Arrays.fill(values, QUESTION_MARK);
    }
    return addCustomColumns(columnStrs, values);
  }
    
  /** Adds the given columns and an equal number of QUESTION_MARK values to
      the query. */
  public InsertQuery addPreparedColumns(Column... columns) {
    return addCustomPreparedColumns((Object[])columns);
  }

  /** Adds the given columns and an equal number of QUESTION_MARK values to
      the query. */
  public InsertQuery addPreparedColumnCollection(
      Collection<? extends Column> columns)
  {
    if(columns != null) {
      for(Column column : columns) {
        addCustomColumn(column, QUESTION_MARK);
      }
    }
    return this;
  }
  
  /**
   * Does Query.validate() and additionally verifies that there are an equal
   * number of columns and values.
   */
  @Override
  public void validate(ValidationContext vContext)
    throws ValidationException
  {
    // check super
    super.validate(vContext);
      
    if(_columns.size() != _values.size()) {
      throw new ValidationException("mismatched columns and values for insert");
    }
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _values.collectSchemaObjects(vContext);
  }
  
  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    appendPrefixTo(app);
    app.append("VALUES (").append(_values).append(")");
  }
}
