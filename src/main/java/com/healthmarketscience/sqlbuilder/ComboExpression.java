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
