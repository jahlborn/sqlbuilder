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
