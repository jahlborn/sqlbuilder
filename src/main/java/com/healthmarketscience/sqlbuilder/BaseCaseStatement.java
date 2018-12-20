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
import java.util.ListIterator;

import com.healthmarketscience.common.util.AppendableExt;

/**
 * Common base class for <code>"CASE ... END"</code> clauses
 *
 * @author James Ahlborn
 */
public abstract class BaseCaseStatement<ThisType extends BaseCaseStatement<ThisType>>
  extends Expression implements Verifiable<ThisType>
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
  public final ThisType validate()
    throws ValidationException
  {
    doValidate();
    return getThisType();
  }

  @Override
  public void validate(ValidationContext vContext)
    throws ValidationException
  {
    if(_whens.size() > 1) {
      // make sure that at most one else clause exists and that it is at the
      // end
      int okayIdx = _whens.size() - 1;
      for(ListIterator<BaseWhenObject> iter = _whens.listIterator();
          iter.hasNext(); ) {
        int idx = iter.nextIndex();
        BaseWhenObject obj = iter.next();
        if((obj instanceof ElseObject) && (idx != okayIdx)) {
          throw new ValidationException("Else clause at invalid index " + idx);
        }
      }
      
    }
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    vContext.addVerifiable(this);
    if(_operand != null) {
      _operand.collectSchemaObjects(vContext);
    }
    _whens.collectSchemaObjects(vContext);
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
  @SuppressWarnings("unchecked")
  protected final ThisType getThisType() {
    return (ThisType)this;
  }

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
    protected void collectSchemaObjects(ValidationContext vContext) {
      _result.collectSchemaObjects(vContext);
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
    protected void collectSchemaObjects(ValidationContext vContext) {
      super.collectSchemaObjects(vContext);
      _test.collectSchemaObjects(vContext);
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
