/*
Copyright (c) 2017 James Ahlborn

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


/**
 * Outputs a window function clause like:
 * <code>([PARTITION BY &lt;cols&gt;] [ORDER BY &lt;cols&gt;] [&lt;frameClause&gt;])</code>
 * Can be used inline on a {@link FunctionCall} or via a named reference in a
 * {@link SelectQuery}.
 *
 * @see "SQL 2003" 
 * @author James Ahlborn
 */
public class WindowDefinitionClause extends SqlObject
{
  /**
   * Outputs the units for the window frame clause.
   */
  public enum FrameUnits 
  {
    ROWS,
    RANGE;
  }

  /**
   * Outputs the exclusion for the window frame clause.
   */
  public enum FrameExclusion
  {
    CURRENT_ROW("CURRENT ROW"),
    GROUP("GROUP"),
    TIES("TIES"),
    NO_OTHERS("NO OTHERS");
    
    private final String _str;

    private FrameExclusion(String str) {
      _str = str;
    }

    @Override
    public String toString() {
      return _str;
    }
  }

  private final SqlObjectList<SqlObject> _columns = SqlObjectList.create();
  private final SqlObjectList<SqlObject> _ordering = SqlObjectList.create();
  private Object _frameUnits;
  private SqlObject _frameStart;
  private SqlObject _frameEnd;
  private Object _frameExclusion;

  public WindowDefinitionClause() {
  }
  
  /**
   * Adds the given columns to the PARTITION BY column list.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   */
  public WindowDefinitionClause addPartitionColumns(Object... columnStrs) {
    _columns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    return this;
  }

  /**
   * Adds the given column with the given direction to the "ORDER BY"
   * clause
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomColumnSqlObject(Object)}.
   */
  public WindowDefinitionClause addOrdering(Object columnStr,
                                            OrderObject.Dir dir) {
    return addOrderings(
        new OrderObject(dir, Converter.toCustomColumnSqlObject(columnStr)));
  }

  /**
   * Adds the given columns to the "ORDER BY" clause
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   */
  public WindowDefinitionClause addOrderings(Object... columnStrs) {
    _ordering.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    return this;
  }

  /**
   * Sets the window's "frame" clause to the given units and starting bound.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject}.
   * @see FrameUnits
   * @see FrameBound
   */
  public WindowDefinitionClause setFrame(Object frameUnits, Object frameStart) {
    return setFrame(frameUnits, frameStart, null);
  }

  /**
   * Sets the window's "frame" clause to the given units, starting bound, and
   * exclusion.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject}.
   * @see FrameUnits
   * @see FrameBound
   * @see FrameExclusion
   */
  public WindowDefinitionClause setFrame(
      Object frameUnits, Object frameStart, Object frameExclusion) {
    return setFrameImpl(frameUnits, frameStart, null, frameExclusion);
  }

  /**
   * Sets the window's "frame" clause to the given units and bound range.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject}.
   * @see FrameUnits
   * @see FrameBound
   */
  public WindowDefinitionClause setFrameBetween(
      Object frameUnits, Object frameStart, Object frameEnd) {
    return setFrameBetween(frameUnits, frameStart, frameEnd, null);
  }

  /**
   * Sets the window's "frame" clause to the given units, bound range, and
   * exclusion.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomSqlObject}.
   * @see FrameUnits
   * @see FrameBound
   * @see FrameExclusion
   */
  public WindowDefinitionClause setFrameBetween(
      Object frameUnits, Object frameStart, Object frameEnd,
      Object frameExclusion) 
  {
    return setFrameImpl(frameUnits, frameStart, frameEnd, frameExclusion);
  }

  private WindowDefinitionClause setFrameImpl(
      Object frameUnits, Object frameStart, Object frameEnd,
      Object frameExclusion) 
  {
    _frameUnits = frameUnits;
    _frameStart = Converter.toCustomSqlObject(frameStart);
    if(frameEnd != null) {
      _frameEnd = Converter.toCustomSqlObject(frameEnd);
    }
    _frameExclusion = frameExclusion;
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _columns.collectSchemaObjects(vContext);
    _ordering.collectSchemaObjects(vContext);
    collectSchemaObjects(_frameStart, vContext);
    collectSchemaObjects(_frameEnd, vContext);
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {

    app.append("(");
    
    boolean hasPreceding = false;
    if(!_columns.isEmpty()) {
      app.append("PARTITION BY ").append(_columns);
      hasPreceding = true;
    }

    if(!_ordering.isEmpty()) {
      if(hasPreceding) {
        app.append(" ");
      }
      app.append("ORDER BY ").append(_ordering);
      hasPreceding = true;
    }

    if((_frameUnits != null) && (_frameStart != null)) {
      if(hasPreceding) {
        app.append(" ");
      }
      app.append(_frameUnits).append(" ");
      if(_frameEnd == null) {
        app.append(_frameStart);
      } else {
        app.append("BETWEEN ").append(_frameStart)
          .append(" AND ").append(_frameEnd);
      }

      if(_frameExclusion != null) {
        app.append(" EXCLUDE ").append(_frameExclusion);
      }
    }

    app.append(")");
  }  


  /**
   * Outputs a bound for the window frame clause.
   * <code>"[&lt;boundValue&gt;] &lt;boundScope&gt;"</code>
   */
  public static class FrameBound extends SqlObject
  {
    public static final FrameBound UNBOUNDED_PRECEDING = 
      new FrameBound("UNBOUNDED", "PRECEDING");
    public static final FrameBound UNBOUNDED_FOLLOWING = 
      new FrameBound("UNBOUNDED", "FOLLOWING");
    public static final FrameBound CURRENT_ROW = 
      new FrameBound(null, "CURRENT ROW");

    private final Object _value;
    private final Object _scope;

    public FrameBound(Object value, Object scope) {
      _value = value;
      _scope = scope;
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
      if(_value != null) {
        app.append(_value).append(" ");
      }
      app.append(_scope);
    }

    public static FrameBound boundedPreceding(int value) {
      return new FrameBound(value, "PRECEDING");
    }

    public static FrameBound boundedFollowing(int value) {
      return new FrameBound(value, "FOLLOWING");
    }
  }
}
