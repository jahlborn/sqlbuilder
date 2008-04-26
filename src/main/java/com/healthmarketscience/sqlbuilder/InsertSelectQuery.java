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

    if((_selectQuery != null) && !vContext.isLocalOnly()) {
      // treat select query as a separate subquery
      _selectQuery.collectSchemaObjects(new ValidationContext(vContext));
    }
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
