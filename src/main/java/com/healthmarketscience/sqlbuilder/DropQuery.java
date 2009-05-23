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
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import com.healthmarketscience.sqlbuilder.dbspec.Index;

/**
 * Query which generates a DROP statement.
 * 
 * @author Tim McCune
 */
public class DropQuery extends Query<DropQuery>
{
  /**
   * Enum representing he type of the object being dropped
   */
  public enum Type
  {
    TABLE(" TABLE "),
    INDEX(" INDEX "),
    VIEW(" VIEW ");

    private final String _typeStr;

    private Type(String typeStr) {
      _typeStr = typeStr;
    }
    
    @Override
    public String toString() { return _typeStr; }
  }

  /**
   * Enum representing additional behavior for the drop query, e.g.:
   * <code>"DROP &lt;type&gt; &lt;obj&gt; &lt;behavior&gt;"</code>
   */
  public enum Behavior
  {
    CASCADE(" CASCADE"),
    RESTRICT(" RESTRICT");

    private final String _bvrStr;

    private Behavior(String bvrStr) {
      _bvrStr = bvrStr;
    }

    @Override
    public String toString() { return _bvrStr; }
  }
  
  private Type _type;
  private SqlObject _obj;
  private Behavior _behavior;
  
  /**
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject(Object)}.
   */
  public DropQuery(Type type, Object obj) {
    _type = type;
    _obj = Converter.toCustomSqlObject(obj);
  }

  /** Sets the behavior for the drop query */
  public DropQuery setBehavior(Behavior newBehavior) {
    _behavior = newBehavior;
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _obj.collectSchemaObjects(vContext);
  }

  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    app.append("DROP").append(_type).append(_obj);
    if(_behavior != null) {
      app.append(_behavior);
    }
  }
  
  /**
   * @return a DropQuery for the given table.
   */
  public static DropQuery dropTable(Table table) {
    return dropTable((Object)table);
  }
  
  /**
   * @return a DropQuery for the given table.
   *
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public static DropQuery dropTable(Object tableStr) {
    return new DropQuery(Type.TABLE,
                         Converter.toCustomTableSqlObject(tableStr));
  }
  
  /**
   * @return a DropQuery for the given index.
   */
  public static DropQuery dropIndex(Index index) {
    return dropIndex((Object)index);
  }
  
  /**
   * @return a DropQuery for the given index.
   *
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomIndexSqlObject(Object)}.
   */
  public static DropQuery dropIndex(Object indexStr) {
    return new DropQuery(Type.INDEX,
                         Converter.toCustomIndexSqlObject(indexStr));
  }
  
  /**
   * @return a DropQuery for the given view.
   */
  public static DropQuery dropView(Table table) {
    return dropView((Object)table);
  }
  
  /**
   * @return a DropQuery for the given view.
   *
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public static DropQuery dropView(Object tableStr) {
    return new DropQuery(Type.VIEW,
                         Converter.toCustomTableSqlObject(tableStr));
  }
  
}
