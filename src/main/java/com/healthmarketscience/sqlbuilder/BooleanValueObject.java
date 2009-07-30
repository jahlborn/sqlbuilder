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

  private Object _value;

  public BooleanValueObject(Object value) {
    this((Boolean)value);
  }

  public BooleanValueObject(Boolean value) {
    _value = toValue(value);
  }

  @Override
  public boolean hasParens() { return false; }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
  }
  
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(_value);
  }


  private static Object toValue(Boolean b)
  {
    return (b.booleanValue() ? TRUE_NUMBER : FALSE_NUMBER);
  }


}
