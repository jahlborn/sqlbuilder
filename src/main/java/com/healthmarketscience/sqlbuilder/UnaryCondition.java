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
import java.util.Collection;
import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.Table;



/**
 * Outputs a unary based condition
 * <code>"(&lt;column&gt; &lt;unaryOp&gt;)"</code> or
 * <code>"(&lt;unaryOp&gt; &lt;column&gt;)"</code>.
 *
 * @author James Ahlborn
 */
public class UnaryCondition extends Condition
{
  /**
   * Enum representing the unary operations supported in a SQL condition,
   * e.g. <code>"(&lt;column&gt; &lt;unaryOp&gt;)"</code> or
   * <code>"(&lt;unaryOp&gt; &lt;column&gt;)"</code>.
   */
  public enum Op
  {
    IS_NULL(" IS NULL", false),
    IS_NOT_NULL(" IS NOT NULL", false);

    private String _opStr;
    private boolean _isPrefixOp;

    private Op(String opStr, boolean isPrefixOp) {
      _opStr = opStr;
      _isPrefixOp = isPrefixOp;
    }

    public boolean isPrefixOp() { return _isPrefixOp; }
    
    @Override
    public String toString() { return _opStr; }
  }

  
  private Op _unaryOp;
  private SqlObject _value;

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public UnaryCondition(Op unaryOp, Object obj)
  {
    this(unaryOp, Converter.toColumnSqlObject(obj));
  }
    
  public UnaryCondition(Op unaryOp, SqlObject value)
  {
    _unaryOp = unaryOp;
    _value = value;
  }
        
  @Override
  protected void collectSchemaObjects(Collection<Table> tables,
                                  Collection<Column> columns) {
    _value.collectSchemaObjects(tables, columns);
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException
  {
    app.append("(");
    if(_unaryOp.isPrefixOp()) {
      app.append(_unaryOp).append(_value);
    } else {
      app.append(_value).append(_unaryOp);
    }
    app.append(")");
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is {@code NULL}.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static UnaryCondition isNull(Object value) {
    return new UnaryCondition(Op.IS_NULL, value);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is not {@code NULL}.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static UnaryCondition isNotNull(Object value) {
    return new UnaryCondition(Op.IS_NOT_NULL, value);
  }
    
}
