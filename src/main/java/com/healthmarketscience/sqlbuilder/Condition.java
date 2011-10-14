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
 * An object representing a conditional expression.
 *
 * @author James Ahlborn
 */
public abstract class Condition extends NestableClause
{
  /** a Condition object which will always return <code>true</code> for
      {@link Condition#isEmpty}.  useful for selectively including condition
      blocks */
  public static final Condition EMPTY = new Condition() {
      @Override
      public boolean isEmpty() { return true; }
      @Override
      protected void collectSchemaObjects(ValidationContext vContext) {}
      @Override
      public void appendTo(AppendableExt app) throws IOException {
        throw new UnsupportedOperationException("Should never be called");
      }
    };

  
  protected Condition() {}

  @Override
  public Condition setDisableParens(boolean disableParens) {
    super.setDisableParens(disableParens);
    return this;
  }  
}
