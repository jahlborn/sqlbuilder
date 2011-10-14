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
 * Outputs a unary based condition
 * <code>"(&lt;column&gt; &lt;unaryOp&gt;)"</code> or
 * <code>"(&lt;unaryOp&gt; &lt;column&gt;)"</code>.
 *
 * @author James Ahlborn
 */
public class UnaryCondition extends Condition
{
  /**
   * Interface which can be implemented to provide a custom unary operation.
   */
  public interface CustomUnaryOp
  {
    /** Returns {@code true} if the operator comes before the value, {@code
        false} otherwise. */
    public boolean isPrefixOp();

    /** Returns the Converter which handles the {@code Object} -&gt; {@code
        SqlObject} conversion for the operation value. */
    public Converter<Object,? extends SqlObject> getConverter();
  }
  
  /**
   * Enum representing the unary operations supported in a SQL condition,
   * e.g. <code>"(&lt;column&gt; &lt;unaryOp&gt;)"</code> or
   * <code>"(&lt;unaryOp&gt; &lt;column&gt;)"</code>.
   */
  public enum Op implements CustomUnaryOp
  {
    /** {@code Object} -&gt; {@code SqlObject} conversions handled by
        {@link Converter#COLUMN_VALUE_TO_OBJ}. */
    IS_NULL(" IS NULL", false, Converter.COLUMN_VALUE_TO_OBJ),
    /** {@code Object} -&gt; {@code SqlObject} conversions handled by
        {@link Converter#COLUMN_VALUE_TO_OBJ}. */
    IS_NOT_NULL(" IS NOT NULL", false, Converter.COLUMN_VALUE_TO_OBJ),
    /** {@code Object} -&gt; {@code SqlObject} conversions handled by
        {@link Converter#CUSTOM_TO_SUBQUERY}. */
    EXISTS("EXISTS ", true, Converter.CUSTOM_TO_SUBQUERY),
    /** {@code Object} -&gt; {@code SqlObject} conversions handled by
        {@link Converter#CUSTOM_TO_SUBQUERY}. */
    UNIQUE("UNIQUE ", true, Converter.CUSTOM_TO_SUBQUERY);

    private final String _opStr;
    private final boolean _isPrefixOp;
    private final Converter<Object,? extends SqlObject> _converter;

    private Op(String opStr, boolean isPrefixOp, 
               Converter<Object,? extends SqlObject> converter) {
      _opStr = opStr;
      _isPrefixOp = isPrefixOp;
      _converter = converter;
      
    }

    public boolean isPrefixOp() { return _isPrefixOp; }
    
    public Converter<Object,? extends SqlObject> getConverter() {
      return _converter; 
    }

    @Override
    public String toString() { return _opStr; }
  }

  
  private CustomUnaryOp _unaryOp;
  private SqlObject _value;

  public UnaryCondition(Op unaryOp, SqlObject obj)
  {
    this(unaryOp, (Object)obj);
  }
    
  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Op#getConverter}.
   */
  public UnaryCondition(Op unaryOp, Object value)
  {
    this((CustomUnaryOp)unaryOp, value);
  }
        
  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link CustomUnaryOp#getConverter}.
   */
  public UnaryCondition(CustomUnaryOp unaryOp, Object value)
  {
    _unaryOp = unaryOp;
    _value = _unaryOp.getConverter().convert(value);
  }
        
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _value.collectSchemaObjects(vContext);
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException
  {
    openParen(app);
    if(_unaryOp.isPrefixOp()) {
      app.append(_unaryOp).append(_value);
    } else {
      app.append(_value).append(_unaryOp);
    }
    closeParen(app);
  }

  /**
   * Convenience method for generating a Condition for testing if a column
   * is {@code NULL}.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#COLUMN_VALUE_TO_OBJ}.
   */
  public static UnaryCondition isNull(Object value) {
    return new UnaryCondition(Op.IS_NULL, value);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * is not {@code NULL}.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#COLUMN_VALUE_TO_OBJ}.
   */
  public static UnaryCondition isNotNull(Object value) {
    return new UnaryCondition(Op.IS_NOT_NULL, value);
  }
    
  /**
   * Convenience method for generating a Condition for testing whether a
   * subquery returns any rows.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_TO_SUBQUERY}.
   */
  public static UnaryCondition exists(Object query) {
    return new UnaryCondition(Op.EXISTS, query);
  }
    
  /**
   * Convenience method for generating a Condition for testing whether a
   * subquery returns exactly one row.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_TO_SUBQUERY}.
   */
  public static UnaryCondition unique(Object query) {
    return new UnaryCondition(Op.UNIQUE, query);
  }
    
}
