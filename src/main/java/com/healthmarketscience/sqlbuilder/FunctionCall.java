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
import com.healthmarketscience.sqlbuilder.dbspec.Function;


/**
 * Outputs a function call
 * <code>"&lt;name&gt;([&lt;param1&gt;, ... &lt;paramN&gt;])"</code>.
 *
 * @author James Ahlborn
 */
public class FunctionCall extends Expression
{
  private boolean _isDistinct;
  private SqlObject _functionName;
  private SqlObjectList<SqlObject> _params = SqlObjectList.create();

  public FunctionCall(Function function) {
    this((Object)function);
  }

  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomFunctionSqlObject(Object)}.
   */
  public FunctionCall(Object functionNameStr) {
    _functionName = Converter.toCustomFunctionSqlObject(functionNameStr);
  }

  @Override
  public boolean hasParens() { return false; }

  /** Iff isDistinct is <code>true</code>, adds the DISTINCT keyword to the
      parameter clause */
  public FunctionCall setIsDistinct(boolean isDistinct) {
    _isDistinct = isDistinct;
    return this;
  }
    
  /**
   * Adds custom parameters to the function call.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#COLUMN_VALUE_TO_OBJ}.
   */
  public FunctionCall addCustomParams(Object... params) {
    _params.addObjects(Converter.COLUMN_VALUE_TO_OBJ, params);
    return this;
  }
    
  /** Adds column parameters to the function call as
      <code>"&lt;alias&gt;.&lt;column&gt;"</code>. */
  public FunctionCall addColumnParams(Column... columns) {
    return addCustomParams((Object[])columns);
  }
    
  /**
   * Adds a numeric value parameter to the function call.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#COLUMN_VALUE_TO_OBJ}.
   */
  public FunctionCall addNumericValueParam(Object obj) {
    return addCustomParams(obj);
  }
    
  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _functionName.collectSchemaObjects(vContext);
    _params.collectSchemaObjects(vContext);
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(_functionName).append("(");
    if(_isDistinct) {
      app.append("DISTINCT ");
    }
    app.append(_params).append(")");
  }

  /**
   * Convenience method for generating a FunctionCall using the standard AVG
   * function.
   */
  public static FunctionCall avg() {
    return new FunctionCall(new CustomSql("AVG"));
  }
  
  /**
   * Convenience method for generating a FunctionCall using the standard MIN
   * function.
   */
  public static FunctionCall min() {
    return new FunctionCall(new CustomSql("MIN"));
  }
  
  /**
   * Convenience method for generating a FunctionCall using the standard MAX
   * function.
   */
  public static FunctionCall max() {
    return new FunctionCall(new CustomSql("MAX"));
  }
  
  /**
   * Convenience method for generating a FunctionCall using the standard SUM
   * function.
   */
  public static FunctionCall sum() {
    return new FunctionCall(new CustomSql("SUM"));
  }
  
  /**
   * Convenience method for generating a FunctionCall using the standard COUNT
   * function.
   */
  public static FunctionCall count() {
    return new FunctionCall(new CustomSql("COUNT"));
  }
  
  /**
   * Convenience method for generating a FunctionCall using the standard COUNT
   * function with the single parameter '*'.
   */
  public static FunctionCall countAll() {
    return (new FunctionCall(new CustomSql("COUNT")))
      .addCustomParams(ALL_SYMBOL);
  }
  
}
