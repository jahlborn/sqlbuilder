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

import com.healthmarketscience.sqlbuilder.dbspec.Table;
import com.healthmarketscience.common.util.AppendableExt;

/**
 * Query which generates a CREATE VIEW statement.
 *
 * @author James Ahlborn
 */
public class CreateViewQuery extends BaseCreateQuery<CreateViewQuery>
{
  private SelectQuery _selectQuery;
  private boolean _withCheckOption;
  
  /**
   * @param table the view to create
   */
  public CreateViewQuery(Table table) {
    this((Object)table);
  }

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public CreateViewQuery(Object tableStr) {
    super(Converter.toCustomTableSqlObject(tableStr));
  }

  /**
   * @return a DropQuery for the object which would be created by this create
   *         query.
   */
  @Override
  public DropQuery getDropQuery() {
    return new DropQuery(DropQuery.Type.VIEW, _object);
  }
  
  /**
   * {@inheritDoc}
   *
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   */
  @Override
  public CreateViewQuery addCustomColumns(Object... columnStrs) {
    _columns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    return this;
  }
  
  /** Sets the SELECT query which will generate the data in the view */
  public CreateViewQuery setSelectQuery(SelectQuery selectQuery) {
    _selectQuery = selectQuery;
    return this;
  }

  /**
   * Sets whether or not inserts/updates to the view are required to affect
   * rows with are in fact visible to the view.
   */
  public CreateViewQuery setWithCheckOption(boolean withCheckOption) {
    _withCheckOption = withCheckOption;
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);

    if((_selectQuery != null) && !vContext.isLocalOnly()) {
      // treat select query as a separate subquery
      _selectQuery.collectSchemaObjects(new ValidationContext(vContext));
    }
  }

  @Override
  public void validate(ValidationContext vContext)
    throws ValidationException
  {
    // validate super
    super.validate(vContext);

    if(_selectQuery == null) {
      throw new ValidationException("missing select query");
    }
    SqlObjectList<SqlObject> selectColumns = _selectQuery.getColumns();

    // if we are using the "*" syntax, then we can't really compare the number
    // of columns.  also, the columns in the the create statement are
    // optional.
    if(!_columns.isEmpty() &&
       (_columns.size() != selectColumns.size()) &&
       !_selectQuery.hasAllColumns()) {
      throw new ValidationException(
          "mismatched columns and select columns for view");
    }
  }
  
  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    app.append("CREATE VIEW ").append(_object);
    if(!_columns.isEmpty()) {
      app.append(" (").append(_columns).append(")");
    }
    app.append(" AS ").append(_selectQuery);
    if(_withCheckOption) {
      app.append(" WITH CHECK OPTION");
    }
  }
  
}
