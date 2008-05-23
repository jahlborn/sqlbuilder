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

  /**
   * @throws UnsupportedOperationException
   */
  @Override
  public final CreateViewQuery setTableSpace(String tableSpace) {
    throw new UnsupportedOperationException();
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
