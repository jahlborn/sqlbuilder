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

  /** @return the number value held by this NumberValueObject */
  Number getValue() {
    return _value;
  }
  
  /**
   * @return <code>true</code> if the given number is a floating point value
   */
  boolean isFloatingPoint()
  {
    if(_value instanceof BigInteger) {
      return false;
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
