/*
Copyright (c) 2011 James Ahlborn

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
*/

package com.healthmarketscience.sqlbuilder;

import java.io.IOException;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;

/**
 * Outputs the beginning of a constraint clause, either
 * <code>CONSTRAINT &lt;name&gt; </code> or an empty string if the constraint
 * is unnamed.
 *
 * @author James Ahlborn
 */
class ConstraintObject extends SqlObject
{
  protected Constraint _constraint;

  protected ConstraintObject(Constraint constraint) {
    _constraint = constraint;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    String name = _constraint.getConstraintNameSQL();
    if(name != null) {
      app.append("CONSTRAINT ").append(name).append(" ");
    }
  }
}
