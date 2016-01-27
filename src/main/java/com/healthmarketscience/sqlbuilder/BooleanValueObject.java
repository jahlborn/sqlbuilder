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
 * Outputs a boolean value as a number literal, where
 * {@code true} == {@code 1} and
 * {@code false} == {@code 0}.
 *
 * @author James Ahlborn
 */
public class BooleanValueObject extends Expression
{
  private static final Integer TRUE_NUMBER = 1;
  private static final Integer FALSE_NUMBER = 0;

  private Boolean _value;

  public BooleanValueObject(Object value) {
    this((Boolean)value);
  }

  public BooleanValueObject(Boolean value) {
    _value = value;
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


  private static Object toSqlValue(Boolean b)
  {
    return (b.booleanValue() ? TRUE_NUMBER : FALSE_NUMBER);
  }


}
