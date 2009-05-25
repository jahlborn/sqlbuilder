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
 * Base query for queries which generate a series of SELECT queries joined by
 * one or more "set operations", such as UNION [ALL], EXCEPT [ALL], and
 * INSERSECT [ALL].
 *
 * @author James Ahlborn
 */
public class SetOperationQuery<ThisType extends SetOperationQuery<ThisType>> 
  extends Query<ThisType> 
{

  /** Enumeration representing the type of union to use */
  public enum Type
  {
    UNION(" UNION "),
    UNION_ALL(" UNION ALL "),
    EXCEPT(" EXCEPT "),
    EXCEPT_ALL(" EXCEPT ALL "),
    INTERSECT(" INTERSECT "),
    INTERSECT_ALL(" INTERSECT ALL ");

    private final String _typeStr;

    private Type(String typeStr) {
      _typeStr = typeStr;
    }

    @Override
    public String toString() { return _typeStr; }
  }

  private Type _defaultType;
  private SqlObjectList<RelateTo> _queries = SqlObjectList.create("");
  private SqlObjectList<SqlObject> _ordering = SqlObjectList.create();
  
  public SetOperationQuery(Type type) {
    this(type, (Object[])null);
  }

  public SetOperationQuery(Type type, Object... queries) {
    _defaultType = type;
    addQueriesImpl(_defaultType, queries);
  }

  /** Actual implementation which adds the given queries to the list of
      queries with the given type. */
  private void addQueriesImpl(final Type type, Object[] queries)
  {
    _queries.addObjects(new Converter<Object,RelateTo>() {
                          @Override
                          public RelateTo convert(Object src) {
                            return (_queries.isEmpty() ?
                                    new RelateTo(null, src) :
                                    new RelateTo(type, src));
                          }
                        }, queries);
  }
  
  /** Adds the given queries to the list of queries with the default set
      operation type (the one configured in the constructor). */
  public ThisType addQueries(SelectQuery... queries) {
    return addQueries((Object[])queries);
  }

  /** Adds the given queries to the list of queries with the given set
      operation type. */
  public ThisType addQueries(Type type, SelectQuery... queries) {
    return addQueries(type, (Object[])queries);
  }

  /** Adds the given queries to the list of queries with the default set
      operation type (the one configured in the constructor). */
  public ThisType addQueries(Object... queries) {
    return addQueries(_defaultType, queries);
  }

  /** Adds the given queries to the list of queries with the given set
      operation type. */
  public ThisType addQueries(Type type, Object... queries) {
    addQueriesImpl(type, queries);
    return getThisType();
  }


  /**
   * Adds the given column with the given direction to the "ORDER BY"
   * clause
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomColumnSqlObject(Object)}.
   */
  public ThisType addCustomOrdering(Object columnStr,
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
  public ThisType addCustomOrderings(Object... columnStrs) {
    _ordering.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    return getThisType();
  }

  /** Adds the given column with the given direction to the "ORDER BY"
      clause */
  public ThisType addOrdering(Column column, OrderObject.Dir dir) {
    return addCustomOrdering(column, dir);
  }

  /** Adds the given columns to the "ORDER BY" clause */
  public ThisType addOrderings(Column... columns) {
    return addCustomOrderings((Object[])columns);
  }
  
  /** Adds the given column index with the given direction to the "ORDER BY"
      clause */
  public ThisType addIndexedOrdering(Integer columnIdx,
                                     OrderObject.Dir dir) {
    return addCustomOrdering(columnIdx, dir);
  }

  /** Adds the given column index to the "ORDER BY" clause */
  public ThisType addIndexedOrderings(Integer... columnIdxs) {
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
    for(RelateTo relateTo : _queries) {

      Object queryObj = relateTo.getQuery();
      if(!(queryObj instanceof SelectQuery)) {
        continue;
      }
      SelectQuery selectQuery = (SelectQuery)queryObj;

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

    _queries.collectSchemaObjects(vContext);
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

  /**
   * Convenience method to create a EXCEPT query.
   */
  public static ExceptQuery except() {
    return new ExceptQuery(Type.EXCEPT);
  }

  /**
   * Convenience method to create a EXCEPT query.
   */
  public static ExceptQuery except(SelectQuery... queries) {
    return new ExceptQuery(Type.EXCEPT, queries);
  }
  
  /**
   * Convenience method to create a EXCEPT ALL query.
   */
  public static ExceptQuery exceptAll() {
    return new ExceptQuery(Type.EXCEPT_ALL);
  }

  /**
   * Convenience method to create a EXCEPT ALL query.
   */
  public static ExceptQuery exceptAll(SelectQuery... queries) {
    return new ExceptQuery(Type.EXCEPT_ALL, queries);
  }

  /**
   * Convenience method to create a INTERSECT query.
   */
  public static IntersectQuery intersect() {
    return new IntersectQuery(Type.INTERSECT);
  }

  /**
   * Convenience method to create a INTERSECT query.
   */
  public static IntersectQuery intersect(SelectQuery... queries) {
    return new IntersectQuery(Type.INTERSECT, queries);
  }
  
  /**
   * Convenience method to create a INTERSECT ALL query.
   */
  public static IntersectQuery intersectAll() {
    return new IntersectQuery(Type.INTERSECT_ALL);
  }

  /**
   * Convenience method to create a INTERSECT ALL query.
   */
  public static IntersectQuery intersectAll(SelectQuery... queries) {
    return new IntersectQuery(Type.INTERSECT_ALL, queries);
  }

  /**
   * Outputs the set operator type (if non-{@code null}) and the query
   * <code>"&lt;type&gt; &lt;query&gt;"</code>.
   */
  private static class RelateTo extends Subquery
  {
    private Type _type;

    private RelateTo(Type type, Object query)
    {
      super(query);
      _type = type;
    }

    private Object getQuery() {
      return _query;
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      // type is null for the first query
      if(_type != null) {
        app.append(_type);
      }
      app.append(getQuery());
    }    
  }

}
