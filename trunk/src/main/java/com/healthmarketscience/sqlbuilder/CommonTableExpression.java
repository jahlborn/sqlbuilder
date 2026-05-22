/*
Copyright (c) 2016 James Ahlborn

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.Table;

/**
 * Outputs the Common Table Expression (CTE) definition as part of a {@code
 * "WITH "} clause.  Useable with queries which extend {@link BaseCTEQuery}.
 * <p>
 * A CommonTableExpression creates a pseudo {@link Table} and pseudo {@link
 * Column}s which can subsequently be used in the queries to which this
 * definition applies.
 *
 * @see "SQL 99"
 * @author James Ahlborn
 */
public class CommonTableExpression extends SqlObject
  implements Verifiable<CommonTableExpression>
{
  private final CTETable _table;
  private SqlObject _query;

  /** @param name name of this common table expression. */
  public CommonTableExpression(String name) {
    _table = new CTETable(name);
  }

  /**
   * Returns the pseudo Table instance for this CTE.
   */
  public Table getTable() {
    return _table;
  }

  /**
   * Adds a new column with the given name to this CTE and returns the
   * pseudo Column instane.
   */
  public Column addColumn(String name) {
    CTEColumn col = new CTEColumn(name, _table);
    _table._columns.add(col);
    return col;
  }

  /**
   * Returns a previously defined pseudo Column from this CTE definition with
   * the given name, or {@code null} if one cannot be found.
   */
  public Column findColumn(String name) {
    for(CTEColumn col : _table._columns) {
      if(col.getColumnNameSQL().equals(name)) {
        return col;
      }
    }
    return null;
  }

  /**
   * Sets the pseudo Table alias for use by the pseudo Columns when used in
   * subsequent queries.
   * <p>
   * Note, setting this is optional.  If unset, a unique alias will be
   * generated for this CTE when it is added to a BaseCTEQuery instance.
   */
  public CommonTableExpression setTableAlias(String alias) {
    _table._alias = alias;
    return this;
  }

  /**
   * Returns {@code true} if a table alias has been set for this CTE.
   */
  boolean hasTableAlias() {
    return (_table._alias != null);
  }

  /**
   * Sets the CTE definition query.
   *
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject(Object)}.
   */
  public CommonTableExpression setQuery(Object query) {
    _query = Converter.toCustomSqlObject(query);
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {

    vContext.addVerifiable(this);

    // note, although this expression _does_ have a Table (and maybe Columns),
    // it is not "using" the table (or columns), only defining them.  Therefore
    // none of them should be added to the vContext

    // treat cte query as a separate subquery
    vContext.collectNestedQuerySchemaObjects(_query);
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(_table);

    if(!_table._columns.isEmpty()) {
      app.append(" (").append(_table._columns, SqlObjectList.DEFAULT_DELIMITER)
        .append(")");
    }

    SqlContext context = (SqlContext)app.getContext();
    SqlContext parentContex = ((context != null) ? context.getParent() : null);
    app.setContext(parentContex);

    app.append(" AS ").append('(').append(_query).append(')');

    app.setContext(context);
  }

  @Override
  public final CommonTableExpression validate()
    throws ValidationException
  {
    doValidate();
    return this;
  }

  @Override
  public void validate(ValidationContext vContext)
    throws ValidationException
  {
    if(_query == null) {
      throw new ValidationException("missing cte query");
    }

    // see if we can find a SelectQuery inside this CTE query
    SqlObject query = _query;
    if(query instanceof SetOperationQuery<?>) {
      query = ((SetOperationQuery<?>)_query).getFirstQuery();
    }

    if(query instanceof SelectQuery) {

      SelectQuery selectQuery = (SelectQuery)query;

      // attempt to do some basic column validation

      // cte query cannot us "*" for columns
      if(selectQuery.hasAllColumns()) {
        throw new ValidationException(
            "Common table expression queries cannot use '*' syntax");
      }

      int numCTECols = _table._columns.size();
      if(numCTECols > 0) {

        // col count should match query
        if(numCTECols != selectQuery.getColumns().size()) {
          throw new ValidationException(
              "Mismatched number of columns in common table expression, found " + numCTECols +
              " while query has " + selectQuery.getColumns().size());
        }
      }
    }
  }

  /**
   * Column implementation for the CTE pseudo-table
   */
  @SuppressWarnings("deprecation")
  private static final class CTEColumn implements Column
  {
    private final String _name;
    private final CTETable _table;

    private CTEColumn(String name, CTETable table) {
      _name = name;
      _table = table;
    }

    @Override
    public CTETable getTable() {
      return _table;
    }

    @Override
    public String getColumnNameSQL() {
      return _name;
    }

    @Override
    public String getTypeNameSQL() {
      return null;
    }

    @Override
    public Integer getTypeLength() {
      return null;
    }

    @Override
    public List<?> getTypeQualifiers() {
      return Collections.emptyList();
    }

    @Override
    public List<? extends Constraint> getConstraints() {
      return Collections.emptyList();
    }

    @Override
    public Object getDefaultValue() {
      return null;
    }

    @Override
    public String toString() {
      return getColumnNameSQL();
    }
  }

  /**
   * Table implementation for the CTE pseudo-table
   */
  private static final class CTETable implements Table
  {
    private final String _name;
    private String _alias;
    private final List<CTEColumn> _columns = new ArrayList<CTEColumn>();

    private CTETable(String name) {
      _name = name;
    }

    @Override
    public String getAlias() {
      return _alias;
    }

    @Override
    public String getTableNameSQL() {
      return _name;
    }

    @Override
    public List<? extends Column> getColumns() {
      return _columns;
    }

    @Override
    public List<? extends Constraint> getConstraints() {
      return Collections.emptyList();
    }

    @Override
    public String toString() {
      return getTableNameSQL();
    }
  }

}
