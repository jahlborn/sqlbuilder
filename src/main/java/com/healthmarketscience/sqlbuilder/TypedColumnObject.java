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
import com.healthmarketscience.sqlbuilder.dbspec.Column;


/**
 * Outputs the name of a column, its type information and any constraints
 * <code>"&lt;column&gt; &lt;type&gt; [ &lt;constraint&gt; ... ]"</code> (for
 * CREATE statements).
 *
 * @author James Ahlborn
 */
class TypedColumnObject extends ColumnObject
{
  private SqlObjectList<SqlObject> _constraints = SqlObjectList.create(" ");
  private SqlObject _defaultValue;

  TypedColumnObject(Column column) {
    super(column);

    _constraints.addObjects(Converter.CUSTOM_TO_CONSTRAINTCLAUSE,
                            column.getConstraints());
    Object defVal = column.getDefaultValue();
    if(defVal != null) {
      _defaultValue = Converter.toValueSqlObject(defVal);
    }
  }

  /**
   * Adds the given object as a column constraint.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} constraint conversions handled by
   * {@link Converter#toCustomConstraintClause}.
   */
  void addConstraint(Object obj) {
    _constraints.addObject(Converter.toCustomConstraintClause(obj));
  }

  /**
   * Sets the given value as the column default value.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} value conversions handled by
   * {@link Converter#toValueSqlObject}.
   */
  void setDefaultValue(Object val) {
    _defaultValue = Converter.toValueSqlObject(val);
  }
   
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _constraints.collectSchemaObjects(vContext);
    if(_defaultValue != null) {
      _defaultValue.collectSchemaObjects(vContext);
    }
  }
 
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(_column.getColumnNameSQL()).append(" ")
      .append(_column.getTypeNameSQL());
    Integer colFieldLength = _column.getTypeLength();
    if(colFieldLength != null) {
      app.append("(").append(colFieldLength).append(")");
    }

    if(_defaultValue != null) {
      app.append(" DEFAULT ").append(_defaultValue);
    }

    if(!_constraints.isEmpty()) {

      SqlContext context = SqlContext.pushContext(app);
      // generate constraint clauses in their "column" format
      context.setUseTableConstraints(false);
    
      app.append(" ").append(_constraints);

      SqlContext.popContext(app, context);
    }

  }
  
}
