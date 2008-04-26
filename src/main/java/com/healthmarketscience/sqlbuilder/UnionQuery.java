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


/**
 * Query which generates a series of SELECT queries joined by UNION clauses.
 *
 * @author James Ahlborn
 */
public class UnionQuery extends Query<UnionQuery> {

  /** Enumeration representing the type of union to use */
  public enum Type
  {
    UNION(" UNION "),
    UNION_ALL(" UNION ALL ");

    private String _typeStr;

    private Type(String typeStr) {
      _typeStr = typeStr;
    }

    @Override
    public String toString() { return _typeStr; }
  }

  private Type _type;
  private SqlObjectList<SelectQuery> _queries;
  private SqlObjectList<SqlObject> _ordering = SqlObjectList.create();
  
  public UnionQuery(Type type) {
    this(type, (SelectQuery[])null);
  }

  public UnionQuery(Type type, SelectQuery... queries) {
    _type = type;
    _queries = SqlObjectList.create(_type.toString());
    _queries.addObjects(queries);
  }


  /** Adds the given queries to the list of queries. */
  public UnionQuery addQueries(SelectQuery... queries) {
    _queries.addObjects(queries);
    return this;
  }


  /**
   * Adds the given column with the given direction to the "ORDER BY"
   * clause
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomColumnSqlObject(Object)}.
   */
  public UnionQuery addCustomOrdering(Object columnStr,
                                      OrderObject.Dir dir) {
    return addCustomOrderings(
        new OrderObject(dir, Converter.toCustomColumnSqlObject(columnStr)));
  }

  /**
   * Adds the given columns to the "ORDER BY" clause
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   */
  public UnionQuery addCustomOrderings(Object... columnStrs) {
    _ordering.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    return this;
  }

  /** Adds the given column with the given direction to the "ORDER BY"
      clause */
  public UnionQuery addOrdering(Column column, OrderObject.Dir dir) {
    return addCustomOrdering(column, dir);
  }

  /** Adds the given columns to the "ORDER BY" clause */
  public UnionQuery addOrderings(Column... columns) {
    return addCustomOrderings((Object[])columns);
  }
  
  /** Adds the given column index with the given direction to the "ORDER BY"
      clause */
  public UnionQuery addIndexedOrdering(Integer columnIdx,
                                        OrderObject.Dir dir) {
    return addCustomOrdering(columnIdx, dir);
  }

  /** Adds the given column index to the "ORDER BY" clause */
  public UnionQuery addIndexedOrderings(Integer... columnIdxs) {
    return addCustomOrderings((Object[])columnIdxs);
  }

  @Override
  public void validate(ValidationContext vContext)
    throws ValidationException
  {
    // check super
    super.validate(vContext);

    // now, validate each of our sub-query's select queries, and check numbers
    // of args if possible
    int currentCount = -1;
    boolean ignoreColumnCount = false;
    for(SelectQuery selectQuery : _queries) {

      // check the column count against the other queries
      if(!ignoreColumnCount) {

        if(selectQuery.hasAllColumns()) {

          // can't validate if using the "*" syntax
          ignoreColumnCount = true;
          
        } else {
          
          if(currentCount < 0) {
            
            // get expected column count
            currentCount = selectQuery.getColumns().size();
            
          } else {
            
            // validate current query against expected count
            if(currentCount != selectQuery.getColumns().size()) {
              throw new ValidationException(
                  "mismatched number of columns in union statement");
            }
          }
        }
      }

      // sub-selects may not have ordering clauses
      if(!selectQuery.getOrdering().isEmpty()) {
        throw new ValidationException(
            "Union selects may not have ordering clause");
      }
    }

    SelectQuery.validateOrdering(currentCount, _ordering, ignoreColumnCount);
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);

    if(!vContext.isLocalOnly()) {
      // treat each select query as a separate subquery
      for(SelectQuery selectQuery : _queries) {
        selectQuery.collectSchemaObjects(new ValidationContext(vContext));
      }
    }
  }
  
  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    // this will only really apply to the ordering not to the sub-queries, as
    // they may use a different value internally
    newContext.setUseTableAliases(false);
    
    app.append(_queries);

    if(!_ordering.isEmpty()) {
      // append ordering clause
      app.append(" ORDER BY ").append(_ordering);
    }
  }
  
  /**
   * Convenience method to create a UNION query.
   */
  public static UnionQuery union() {
    return new UnionQuery(Type.UNION);
  }

  /**
   * Convenience method to create a UNION query.
   */
  public static UnionQuery union(SelectQuery... queries) {
    return new UnionQuery(Type.UNION, queries);
  }
  
  /**
   * Convenience method to create a UNION ALL query.
   */
  public static UnionQuery unionAll() {
    return new UnionQuery(Type.UNION_ALL);
  }

  /**
   * Convenience method to create a UNION ALL query.
   */
  public static UnionQuery unionAll(SelectQuery... queries) {
    return new UnionQuery(Type.UNION_ALL, queries);
  }
  
}
