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

/**
 * Outputs combination expressions joined by a given mathematical operation (+,
 * -, *, /) or string operation (||) <code>"(&lt;expr1&gt; &lt;exprOp&gt; &lt;expr2&gt; &lt;exprOp&gt; &lt;expr3&gt; ...)"</code>.  Only outputs non-empty
 * expressions in the expression list.
 *
 * @author James Ahlborn
 */
public class ComboExpression extends Expression
{
  /**
   * Enum representing the combo mathematical operations supported in a SQL
   * expression, e.g. <code>"(&lt;expr1&gt; &lt;exprOp&gt; &lt;expr2&gt; &lt;exprOp&gt; &lt;expr3&gt; ...)"</code>.
   */
  public enum Op
  {
    ADD(" + "),
    SUBTRACT(" - "),
    MULTIPLY(" * "),
    DIVIDE(" / "),
    CONCATENATE(" || ");

    private final String _opStr;

    private Op(String opStr) {
      _opStr = opStr;
    }

    @Override
    public String toString() { return _opStr; }
  }  
  
  
  private SqlObjectList<Expression> _expressions;
  
  public ComboExpression(Op comboOp) {
    this(comboOp, (Object[])null);
  }

  /**
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public ComboExpression(Op comboOp, Object... expressions) {
    this((Object)comboOp, expressions);
  }
    
  /**
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public ComboExpression(Object comboOpStr, Object... expressions) {
    _expressions = SqlObjectList.create(comboOpStr.toString());
    _expressions.addObjects(Converter.CUSTOM_TO_EXPRESSION, expressions);
  }
    
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _expressions.collectSchemaObjects(vContext);
  }

  @Override
  public boolean isEmpty() {
    return areEmpty(_expressions);
  }

  @Override
  public boolean hasParens() {
    return hasParens(_expressions);
  }
  
  /**
   * Adds the given expression to the list of expression (wrapped
   * appropriately).
   * <p>
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public ComboExpression addExpression(Object expr) {
    return addExpressions(expr);
  }

  /**
   * Adds the given expressions to the list of expressions (wrapped
   * appropriately).
   * <p>
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public ComboExpression addExpressions(Object... exprs) {
    _expressions.addObjects(Converter.CUSTOM_TO_EXPRESSION, exprs);
    return this;
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException
  {
    appendNestedClauses(app, _expressions);
  }


  /**
   * Convenience method for generating a ComboExpression for joining
   * expressions using '+'.
   */
  public static ComboExpression add() {
    return new ComboExpression(Op.ADD);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * the given expressions using '+'.
   *
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public static ComboExpression add(Object... expressions) {
    return new ComboExpression(Op.ADD, expressions);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * expressions using '-'.
   */
  public static ComboExpression subtract() {
    return new ComboExpression(Op.SUBTRACT);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * the given expressions using '-'.
   *
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public static ComboExpression subtract(Object... expressions) {
    return new ComboExpression(Op.SUBTRACT, expressions);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * expressions using '*'.
   */
  public static ComboExpression multiply() {
    return new ComboExpression(Op.MULTIPLY);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * the given expressions using '*'.
   *
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public static ComboExpression multiply(Object... expressions) {
    return new ComboExpression(Op.MULTIPLY, expressions);
  }
  
  /**
   * Convenience method for generating a ComboExpression for joining
   * expressions using '/'.
   */
  public static ComboExpression divide() {
    return new ComboExpression(Op.DIVIDE);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * the given expressions using '/'.
   *
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public static ComboExpression divide(Object... expressions) {
    return new ComboExpression(Op.DIVIDE, expressions);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * expressions using '||'.
   */
  public static ComboExpression concatenate() {
    return new ComboExpression(Op.CONCATENATE);
  }

  /**
   * Convenience method for generating a ComboExpression for joining
   * the given expressions using '||'.
   *
   * {@code Object} -&gt; {@code Expression} conversions handled by
   * {@link Converter#CUSTOM_TO_EXPRESSION}.
   */
  public static ComboExpression concatenate(Object... expressions) {
    return new ComboExpression(Op.CONCATENATE, expressions);
  }
  
}
