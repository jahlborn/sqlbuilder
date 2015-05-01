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
import com.healthmarketscience.sqlbuilder.dbspec.Index;
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import com.healthmarketscience.sqlbuilder.custom.CustomSyntax;
import com.healthmarketscience.sqlbuilder.custom.HookType;
import com.healthmarketscience.sqlbuilder.custom.HookAnchor;
import com.healthmarketscience.sqlbuilder.custom.oracle.OraTableSpaceClause;

/**
 * Query which generates a CREATE INDEX statement.
 * <p/>
 * Note that this query supports custom SQL syntax, see {@link Hook} for more
 * details.
 * 
 * @author Tim McCune
 */
public class CreateIndexQuery extends BaseCreateQuery<CreateIndexQuery>
{
  /**
   * The HookAnchors supported for CREATE INDEX queries.  See {@link com.healthmarketscience.sqlbuilder.custom}
   * for more details on custom SQL syntax.
   */
  public enum Hook implements HookAnchor {
    /** Anchor for the beginning of the query, only supports {@link
        HookType#BEFORE} */
    HEADER, 
    /** Anchor for the "INDEX " part of the "CREATE INDEX " clause */
    INDEX, 
    /** Anchor for the end of the query, only supports {@link
        HookType#BEFORE} */
    TRAILER;
  }
  
  /**
   * Enum which defines the optional index type information.
   */
  public enum IndexType
  {
    UNIQUE("UNIQUE ");

    private final String _typeClause;

    private IndexType(String typeClause) {
      _typeClause = typeClause;
    }
    
    @Override
    public String toString() { return _typeClause; }
  }

  private IndexType _indexType;
  protected SqlObject _table;

  public CreateIndexQuery(Index index) {
    this((Object)index.getTable(), (Object)index);

    // add all the columns for this table
    _columns.addObjects(Converter.COLUMN_TO_OBJ, index.getColumns());
  }
  
  public CreateIndexQuery(Table table, String indexName) {
    this((Object)table, (Object)indexName);
  }
  
  public CreateIndexQuery(Table table, Object index) {
    this((Object)table, index);
  }
  
  /**
   * Index {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomIndexSqlObject(Object)}.
   * Table {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public CreateIndexQuery(Object tableStr, Object index) {
    super(Converter.toCustomIndexSqlObject(index));
    _table = Converter.toCustomTableSqlObject(tableStr);
  }

  /**
   * Sets the type of index to be created.
   */
  public CreateIndexQuery setIndexType(IndexType indexType) {
    _indexType = indexType;
    return this;
  }

  /**
   * Sets the name of the table which is being indexed.
   */
  public CreateIndexQuery setTableName(String nameName) {
    return setCustomTableName(nameName);
  }

  /**
   * Sets the name of the table which is being indexed.
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toCustomTableSqlObject(Object)}.
   */
  public CreateIndexQuery setCustomTableName(Object name) {
    _table = Converter.toCustomTableSqlObject(name);
    return this;
  }

  @Override
  public DropQuery getDropQuery() {
    return new DropQuery(DropQuery.Type.INDEX, _object);
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#CUSTOM_COLUMN_TO_OBJ}.
   */
  @Override
  public CreateIndexQuery addCustomColumns(Object... columnStrs) {
    _columns.addObjects(Converter.CUSTOM_COLUMN_TO_OBJ, columnStrs);
    return this;
  }

  /** Sets a specific tablespace for the table to be created in by appending
   * <code>TABLESPACE &lt;tableSpace&gt;</code> to the end of the CREATE
   * query.
   *  <p>
   *  <em>WARNING, this is not ANSI SQL compliant.</em>
   *
   * @see OraTableSpaceClause
   *  
   * @deprecated Use {@code addCustomization(new OraTableSpaceClause(tableSpace))}
   *             instead.
   */
  @Deprecated
  public CreateIndexQuery setTableSpace(String tableSpace) {
    return addCustomization(new OraTableSpaceClause(tableSpace));
  }
  
  /**
   * Adds custom SQL to this query.  See {@link com.healthmarketscience.sqlbuilder.custom} for more details on
   * custom SQL syntax.
   * @param hook the part of the query being customized
   * @param type the type of customization
   * @param obj the custom sql.  The {@code Object} -&gt; {@code SqlObject}
   *            conversions handled by {@link Converter#toCustomSqlObject}.
   */
  public CreateIndexQuery addCustomization(Hook hook, HookType type, Object obj) {
    super.addCustomization(hook, type, obj);
    return this;
  }
  
  /**
   * Adds custom SQL to this query.  See {@link com.healthmarketscience.sqlbuilder.custom} for more details on
   * custom SQL syntax.
   * @param obj the custom sql syntax on which the 
   *            {@link CustomSyntax#apply(CreateIndexQuery)} method will be
   *            invoked (may be {@code null}).
   */
  public CreateIndexQuery addCustomization(CustomSyntax obj) {
    if(obj != null) {
      obj.apply(this);
    }
    return this;
  }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    super.collectSchemaObjects(vContext);
    _table.collectSchemaObjects(vContext);
  }

  @Override
  protected void appendTo(AppendableExt app, SqlContext newContext)
    throws IOException
  {
    newContext.setUseTableAliases(false);
    
    customAppendTo(app, Hook.HEADER);

    app.append("CREATE ");
    if(_indexType != null) {
      app.append(_indexType);
    }
    customAppendTo(app, Hook.INDEX, "INDEX ")
      .append(_object).append(" ON ").append(_table)
      .append(" (").append(_columns).append(")");

    customAppendTo(app, Hook.TRAILER);
  }
   
}
