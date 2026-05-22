/*
Copyright (c) 2020 James Ahlborn

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

import java.util.Collection;

/**
 * Useful static accessors for building conditions.
 *
 * @author James Ahlborn
 */
public class Conditions
{
  private Conditions() {}

  /**
   * Convenience method for generating a Condition for testing if a column
   * is less than another column (inclusive or exclusive).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition lessThan(Object value1, Object value2,
                                         boolean inclusive) {
    return new BinaryCondition((inclusive ?
                                BinaryCondition.Op.LESS_THAN_OR_EQUAL_TO :
                                BinaryCondition.Op.LESS_THAN),
                               value1, value2);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is strictly less than another column.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition lessThan(Object value1, Object value2) {
    return new BinaryCondition(BinaryCondition.Op.LESS_THAN, value1, value2);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is less than or equal to another column.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition lessThanOrEq(Object value1, Object value2) {
    return new BinaryCondition(BinaryCondition.Op.LESS_THAN_OR_EQUAL_TO,
                               value1, value2);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is greater than a given value (inclusive or exclusive).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition greaterThan(Object value1, Object value2,
                                            boolean inclusive) {
    return new BinaryCondition((inclusive ?
                                BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO :
                                BinaryCondition.Op.GREATER_THAN),
                               value1, value2);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is strictly greater than a given value.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition greaterThan(Object value1, Object value2) {
    return new BinaryCondition(BinaryCondition.Op.GREATER_THAN, value1, value2);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is greater than or equal to a given value.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition greaterThanOrEq(Object value1, Object value2) {
    return new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO,
                               value1, value2);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is equal to another column.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition equalTo(Object value1, Object value2) {
    return new BinaryCondition(BinaryCondition.Op.EQUAL_TO, value1, value2);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is not equal to another column.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition notEqualTo(Object value1, Object value2) {
    return new BinaryCondition(BinaryCondition.Op.NOT_EQUAL_TO, value1, value2);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is "like" a given value (sql pattern matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition like(Object value1, Object value2) {
    return new BinaryCondition(BinaryCondition.Op.LIKE, value1, value2);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is not "like" a given value (sql pattern matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static BinaryCondition notLike(Object value1, Object value2) {
    return new BinaryCondition(BinaryCondition.Op.NOT_LIKE, value1, value2);
  }

  /**
   * Convenience method for generating a ComboCondition for joining
   * conditions using AND.
   */
  public static ComboCondition and() {
    return new ComboCondition(ComboCondition.Op.AND);
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
    return new ComboCondition(ComboCondition.Op.AND, conditions);
  }

  /**
   * Convenience method for generating a ComboCondition for joining
   * conditions using OR.
   */
  public static ComboCondition or() {
    return new ComboCondition(ComboCondition.Op.OR);
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
    return new ComboCondition(ComboCondition.Op.OR, conditions);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is {@code NULL}.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#COLUMN_VALUE_TO_OBJ}.
   */
  public static UnaryCondition isNull(Object value) {
    return new UnaryCondition(UnaryCondition.Op.IS_NULL, value);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is not {@code NULL}.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#COLUMN_VALUE_TO_OBJ}.
   */
  public static UnaryCondition isNotNull(Object value) {
    return new UnaryCondition(UnaryCondition.Op.IS_NOT_NULL, value);
  }

  /**
   * Convenience method for generating a Condition for testing whether a
   * subquery returns any rows.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_TO_SUBQUERY}.
   */
  public static UnaryCondition exists(Object query) {
    return new UnaryCondition(UnaryCondition.Op.EXISTS, query);
  }

  /**
   * Convenience method for generating a Condition for testing whether a
   * subquery returns exactly one row.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_TO_SUBQUERY}.
   */
  public static UnaryCondition unique(Object query) {
    return new UnaryCondition(UnaryCondition.Op.UNIQUE, query);
  }

  /**
   * Convenience method for generating a NOT condition..
   * <p>
   * {@code Object} -&gt; {@code Condition} conversions handled by
   * {@link Converter#toConditionObject(Object)}.
   */
  public static NotCondition not(Object condition) {
    return new NotCondition(condition);
  }

  /**
   * Convenience method for generating a IN condition..
   * <p>
   * Column {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject}.
   * Value {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#COLUMN_VALUE_TO_OBJ}.
   */
  public static InCondition in(Object leftObj, Object... rightObjs) {
    return new InCondition(leftObj, rightObjs);
  }

  /**
   * Convenience method for generating a IN condition..
   * <p>
   * Column {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject}.
   * Value {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#COLUMN_VALUE_TO_OBJ}.
   */
  public static InCondition in(Object leftObj, Collection<?> rightObjs) {
    return new InCondition(leftObj, rightObjs);
  }

  /**
   * Convenience method for generating a NOT IN condition..
   * <p>
   * Column {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject}.
   * Value {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#COLUMN_VALUE_TO_OBJ}.
   */
  public static InCondition notIn(Object leftObj, Object... rightObjs) {
    return new InCondition(leftObj, rightObjs).setNegate(true);
  }

  /**
   * Convenience method for generating a NOT IN condition..
   * <p>
   * Column {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject}.
   * Value {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#COLUMN_VALUE_TO_OBJ}.
   */
  public static InCondition notIn(Object leftObj, Collection<?> rightObjs) {
    return new InCondition(leftObj, rightObjs).setNegate(true);
  }

  /**
   * Convenience method for generating a BETWEEN condition..
   * <p>
   * Column {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject}.
   */
  public static BetweenCondition between(Object obj, Object minObj,
                                         Object maxObj) {
    return new BetweenCondition(obj, minObj, maxObj);
  }

  /**
   * Convenience method for generating a NOT BETWEEN condition..
   * <p>
   * Column {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject}.
   */
  public static BetweenCondition notBetween(Object obj, Object minObj,
                                            Object maxObj) {
    return new BetweenCondition(obj, minObj, maxObj).setNegate(true);
  }

  /**
   * Convenience method for generating a custom condition.
   */
  public static CustomCondition customCond(Object condObj) {
    return new CustomCondition(condObj);
  }

  /** a Condition object which will always return <code>true</code> for
      {@link Condition#isEmpty}.  useful for selectively including condition
      blocks */
  public static Condition emptyCond() {
    return Condition.EMPTY;
  }
}
