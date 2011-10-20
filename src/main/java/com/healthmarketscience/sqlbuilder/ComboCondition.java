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
 * Outputs combination conditions joined by a given string (AND, OR)
 * <code>"(&lt;cond1&gt; &lt;comboOp&gt; &lt;cond2&gt; &lt;comboOp&gt; &lt;cond3&gt; ...)"</code>.  Only outputs
 * non-empty conditions in the condition list.
 *
 * @author James Ahlborn
 */
public class ComboCondition extends Condition
{
  /**
   * Enum representing the combo operations supported in a SQL
   * condition, e.g. <code>"(&lt;cond1&gt; &lt;comboOp&gt; &lt;cond2&gt; &lt;comboOp&gt; &lt;cond3&gt; ...)"</code>.
   */
  public enum Op
  {
    AND(" AND "),
    OR(" OR ");

    private final String _opStr;

    private Op(String opStr) {
      _opStr = opStr;
    }

    @Override
    public String toString() { return _opStr; }
  }  

  
  private SqlObjectList<Condition> _conditions;

  public ComboCondition(Op comboOp) {
    this(comboOp, (Object[])null);
  }

  public ComboCondition(Op comboOp, Condition... conditions)
  {
    this(comboOp, (Object[])conditions);
  }

  /**
   * {@code Object} -&gt; {@code Condition} conversions handled by
   * {@link Converter#CUSTOM_TO_CONDITION}.
   */
  public ComboCondition(Op comboOp, Object... conditions)
  {
    this((Object)comboOp, conditions);
  }
    
  /**
   * {@code Object} -&gt; {@code Condition} conversions handled by
   * {@link Converter#CUSTOM_TO_CONDITION}.
   */
  public ComboCondition(Object comboOpStr, Object... conditions)
  {
    _conditions = SqlObjectList.create(comboOpStr.toString());
    _conditions.addObjects(Converter.CUSTOM_TO_CONDITION, conditions);
  }
    
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _conditions.collectSchemaObjects(vContext);
  }

  @Override
  public boolean isEmpty() {
    return areEmpty(_conditions);
  }

  @Override
  public boolean hasParens() {
    return hasParens(_conditions);
  }

  /** Adds the given condition to the list of conditions. */
  public ComboCondition addCondition(Condition condition) {
    return addCustomConditions(condition);
  }

  /** Adds the given conditions to the list of conditions. */
  public ComboCondition addConditions(Condition... conditions) {
    return addCustomConditions((Object[])conditions);
  }

  /**
   * Adds the given custom condition to the list of conditions.
   * <p>
   * {@code Object} -&gt; {@code Condition} conversions handled by
   * {@link Converter#CUSTOM_TO_CONDITION}.
   */
  public ComboCondition addCustomCondition(Object condition) {
    return addCustomConditions(condition);
  }

  /**
   * Adds the given custom conditions to the list of conditions.
   * <p>
   * {@code Object} -&gt; {@code Condition} conversions handled by
   * {@link Converter#CUSTOM_TO_CONDITION}.
   */
  public ComboCondition addCustomConditions(Object... conditions) {
    _conditions.addObjects(Converter.CUSTOM_TO_CONDITION, conditions);
    return this;
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException
  {
    appendNestedClauses(app, _conditions);
  }

  
  /**
   * Convenience method for generating a ComboCondition for joining
   * conditions using AND.
   */
  public static ComboCondition and() {
    return new ComboCondition(Op.AND);
  }

  /**
   * Convenience method for generating a ComboCondition for joining
   * the given conditions using AND.
   */
  public static ComboCondition and(Condition... conditions) {
    return and((Object[])conditions);
  }

  /**
   * Convenience method for generating a ComboCondition for joining
   * the given custom conditions using AND.
   * <p>
   * {@code Object} -&gt; {@code Condition} conversions handled by
   * {@link Converter#CUSTOM_TO_CONDITION}.
   */
  public static ComboCondition and(Object... conditions) {
    return new ComboCondition(Op.AND, conditions);
  }

  /**
   * Convenience method for generating a ComboCondition for joining
   * conditions using OR.
   */
  public static ComboCondition or() {
    return new ComboCondition(Op.OR);
  }

  /**
   * Convenience method for generating a ComboCondition for joining
   * the given conditions using OR.
   */
  public static ComboCondition or(Condition... conditions) {
    return or((Object[])conditions);
  }
  
  /**
   * Convenience method for generating a ComboCondition for joining
   * the given custom conditions using OR.
   * <p>
   * {@code Object} -&gt; {@code Condition} conversions handled by
   * {@link Converter#CUSTOM_TO_CONDITION}.
   */
  public static ComboCondition or(Object... conditions) {
    return new ComboCondition(Op.OR, conditions);
  }
}
