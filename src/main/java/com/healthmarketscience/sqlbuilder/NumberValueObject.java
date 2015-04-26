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
import java.math.BigDecimal;
import java.math.BigInteger;

import com.healthmarketscience.common.util.AppendableExt;


/**
 * Outputs a number literal <code>&lt;value&gt;</code>.
 *
 * @author James Ahlborn
 */
public class NumberValueObject extends Expression
{
  private Number _value;

  public NumberValueObject(Object value) {
    this((Number)value);
  }

  public NumberValueObject(Number value) {
    _value = value;
  }

  @Override
  public boolean hasParens() { return false; }

  /**
   * @return {@code true} if this number value is an integral value in the
   *         given range (inclusive), {@code false} otherwise.
   */
  public boolean isIntegralInRange(long min, long max) {
    if(isFloatingPoint()) {
      return false;
    }

    long value = _value.longValue();
    return ((min <= value) && (value <= max));
  }

  /**
   * @return <code>true</code> if the given number is a floating point value
   */
  private boolean isFloatingPoint() {
    if(_value instanceof BigInteger) {
      return false;
    }

    if((_value instanceof Float) || (_value instanceof Double)) {
      return true;
    }

    BigDecimal dec = ((_value instanceof BigDecimal) ? 
                      (BigDecimal)_value : new BigDecimal(_value.doubleValue()));

    return(dec.scale() > 0);
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
  }
  
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(_value);
  }
}
