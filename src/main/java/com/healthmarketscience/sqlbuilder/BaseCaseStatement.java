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
import java.util.Collection;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Table;

/**
 * Common base class for <code>"CASE ... END"</code> clauses
 *
 * @author James Ahlborn
 */
public abstract class BaseCaseStatement<ThisType extends BaseCaseStatement>
  extends Expression
{
  /** SqlObject which can outputs "ELSE NULL" */
  private static final ElseObject NULL_ELSE = new ElseObject(null);

  /** the initial column operand for the CASE statement, if any */
  private SqlObject _operand;
  /** the when clauses for this CASE statement (the last object may be an
      ElseObject) */
  private SqlObjectList<BaseWhenObject> _whens = SqlObjectList.create(" ");
  
  protected BaseCaseStatement(SqlObject operand) {
    _operand = operand;
  }

  @Override
  public boolean isEmpty() {
    return _whens.isEmpty();
  }
  
  /**
   * Adds a "WHEN" clause to the "CASE" statement.
   * <p>
   * All {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   *
   * @param test the custom condition to test for this "WHEN" clause
   * @param result the result to output if this "WHEN" clause is selected
   */
  public ThisType addCustomWhen(Object test, Object result) {
    _whens.addObject(new WhenObject(Converter.toColumnSqlObject(test),
                                    result));
    return getThisType();
  }

  /**
   * Adds an "ELSE" clause to the "CASE" statement.  Should be called at most
   * once after all necessary calls to addWhen have been done.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   *
   * @param result the result to output if no other "WHEN" clause is selected
   */
  public ThisType addElse(Object result) {
    // FIXME, FUTURE: if we ever have validation of subclauses, this would be a great place for it (make sure at most one else clause added at the end of the list)!
    _whens.addObject(new ElseObject(result));
    return getThisType();
  }


  /**
   * Adds an "ELSE NULL" clause to the "CASE" statement.  Should be called at
   * most once after all necessary calls to addWhen have been done.
   */
  public ThisType addElseNull() {
    _whens.addObject(NULL_ELSE);
    return getThisType();
  }
  
  @Override
  protected void collectSchemaObjects(Collection<Table> tables,
                                  Collection<Column> columns) {
    if(_operand != null) {
      _operand.collectSchemaObjects(tables, columns);
    }
    _whens.collectSchemaObjects(tables, columns);
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    if(!isEmpty()) {
      app.append("(CASE ");
      if(_operand != null) {
        app.append(_operand).append(" ");
      }
      app.append(_whens).append(" END)");
    }
  }

  /** @return the handle to this object as the subclass type */
  protected abstract ThisType getThisType();

  /**
   * Utility class to output the result part of a "WHEN" clause for a "CASE"
   * clause.
   */
  private static abstract class BaseWhenObject extends SqlObject
  {
    private SqlObject _result;

    protected BaseWhenObject(Object result) {
      _result = Converter.toColumnSqlObject(result);
    }
    
    @Override
    protected void collectSchemaObjects(Collection<Table> tables,
                                    Collection<Column> columns) {
      _result.collectSchemaObjects(tables, columns);
    }
    
    protected void appendResult(AppendableExt app) throws IOException {
      app.append(_result);
    }
  }
  
  /**
   * Utility class to output the "WHEN" clause for a "CASE" clause.
   */
  private static class WhenObject extends BaseWhenObject
  {
    private SqlObject _test;

    private WhenObject(SqlObject test, Object result) {
      super(result);
      _test = test;
    }
    
    @Override
    protected void collectSchemaObjects(Collection<Table> tables,
                                    Collection<Column> columns) {
      super.collectSchemaObjects(tables, columns);
      _test.collectSchemaObjects(tables, columns);
    }
    
    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append("WHEN ").append(_test).append(" THEN ");
      appendResult(app);
    }
  }
  
  /**
   * Utility class to output the "ELSE" clause for a "CASE" clause.
   */
  private static class ElseObject extends BaseWhenObject
  {
    private ElseObject(Object result) {
      super(result);
    }
    
    @Override
    public void appendTo(AppendableExt app) throws IOException {
      app.append("ELSE ");
      appendResult(app);
    }
  }
  
}
