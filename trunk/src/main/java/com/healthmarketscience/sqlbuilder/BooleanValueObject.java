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
 * By default, outputs a boolean value as a number literal, where {@code true
 * == 1} and {@code false == 0}.
 * <p>
 * Note, however, that the default values are <i>not</i> SQL 92 compliant
 * boolean values (although they are preferred by many databases).  The values
 * output by this class can be made SQL 92 compliant by setting the system
 * property {@value #USE_BOOLEAN_LITERALS_PROPERTY} to {@code true}.  When
 * this feature is enabled, the values output by this class will be {@code
 * true == TRUE} and {@code false == FALSE}.
 *
 * @author James Ahlborn
 */
public class BooleanValueObject extends Expression
{
  public static final String USE_BOOLEAN_LITERALS_PROPERTY =
    "com.healthmarketscience.sqlbuilder.useBooleanLiterals";

  private static final boolean USE_LITERAL_VALUES =
    Boolean.getBoolean(USE_BOOLEAN_LITERALS_PROPERTY);

  private static final Object TRUE_VALUE = (USE_LITERAL_VALUES ? "TRUE" : 1);
  private static final Object FALSE_VALUE = (USE_LITERAL_VALUES ? "FALSE" : 0);

  /** BooleanValueObject representing "true" */
  public static final BooleanValueObject TRUE = new BooleanValueObject(true);
  /** BooleanValueObject representing "false" */
  public static final BooleanValueObject FALSE = new BooleanValueObject(false);


  private Boolean _value;

  public BooleanValueObject(Object value) {
    this((Boolean)value);
  }

  public BooleanValueObject(Boolean value) {
    _value = value;
  }

  /**
   * Returns a BooleanValueObject for the given Boolean value.
   */
  public static BooleanValueObject valueOf(Boolean value) {
    return value ? TRUE : FALSE;
  }

  /**
   * Returns a BooleanValueObject for the given boolean value.
   */
  public static BooleanValueObject valueOf(boolean value) {
    return value ? TRUE : FALSE;
  }

  @Override
  public boolean hasParens() { return false; }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(toSqlValue(_value));
  }

  private static Object toSqlValue(Boolean b) {
    return (b.booleanValue() ? TRUE_VALUE : FALSE_VALUE);
  }
}
