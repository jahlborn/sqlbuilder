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
