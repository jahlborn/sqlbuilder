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

import com.healthmarketscience.common.util.AppendableExt;

/**
 * Object which maintains context for the sqlbuilder classes when a SQL
 * statement is being generated.  The object is passed from one SqlObject to
 * another via the {@link com.healthmarketscience.common.util.AppendableExt#setContext}
 * and {@link com.healthmarketscience.common.util.AppendableExt#getContext} methods.
 * This class enables the various SqlObject SQL generation methods to behave
 * differently depending on where they are being used in a query.
 *
 * Note, users of the sqlbuilder classes may extend this context to pass
 * through more information as any copying of SqlContexts is done via cloning.
 * The custom context may be introduced to the current SQL generation context
 * via the {@link SqlObject#toString(int,SqlContext)} method call.
 *
 * @author James Ahlborn
 */
public class SqlContext implements Cloneable
{

  /** Previous context replaced by this context, if any */
  private SqlContext _parent;

  /** flag indicating whether or not table aliases should be used in the
      current SQL generation context */
  private boolean _useTableAliases = true;

  /** flag indicating whether constraints apply to a column or a table */
  private boolean _useTableConstraints = true;

  /** handle to the immediate wrapping query */
  private Query<?> _query;
  
  public SqlContext() {
  }

  /**
   * Gets the SqlContext which was in effect before this SqlContext was
   * "pushed" onto the context stack.
   */
  public SqlContext getParent() {
    return _parent;
  }

  /**
   * Sets the SqlContext which was in effect before this SqlContext was
   * "pushed" onto the context stack.  Used by {@link #pushContext}.
   */
  private void setParent(SqlContext newParentContext) {
    _parent = newParentContext;
  }

  /**
   * @return the flag indicating whether or not table aliases should be used
   *         in the current SQL generation context.
   */
  public boolean getUseTableAliases() {
    return _useTableAliases;
  }

  /**
   * Sets flag indicating whether or not table aliases should be used in the
   * current SQL generation context.
   */
  public void setUseTableAliases(boolean newUseTableAliases) {
    _useTableAliases = newUseTableAliases;
  }

  /**
   * @return the flag indicating whether or not table constraints should be
   *         used in the current SQL generation context.
   */
  public boolean getUseTableConstraints() {
    return _useTableConstraints;
  }

  /**
   * Sets flag indicating whether or not table constraints should be used in
   * the current SQL generation context.
   */
  public void setUseTableConstraints(boolean newUseTableConstraints) {
    _useTableConstraints = newUseTableConstraints;
  }

  /**
   * Gets the handle to the immediate wrapping query
   */
  public Query<?> getQuery() {
    return _query;
  }

  /**
   * Sets the handle to the immediate wrapping query
   */
  public void setQuery(Query<?> newQuery) {
    _query = newQuery;
  }
  
  @Override
  public SqlContext clone() {
    try {
      return (SqlContext)super.clone();
    } catch(CloneNotSupportedException e) {
      throw new RuntimeException("should never get here", e);
    }
  }

  /**
   * Gets the current SqlContext from the given AppendableExt, creating one if
   * necessary.  Should be used by any subclasses of SqlObject wishing to
   * retrieve the current context.
   */
  public static SqlContext getContext(AppendableExt app)
  {
    SqlContext context = (SqlContext)app.getContext();
    if(context == null) {
      context = new SqlContext();
      app.setContext(context);
    }
    return context;
  }

  /**
   * Creates a new SqlContext (cloning current one if available), replaces the
   * current SqlContext with the new SqlContext, and returns the new
   * SqlContext.  All <code>pushContext</code> calls should have a
   * corresponding {@link #popContext} call.
   */
  public static final SqlContext pushContext(AppendableExt app)
  {
    SqlContext parentContext = (SqlContext)app.getContext();
    SqlContext context = null;
    if(parentContext != null) {
      context = parentContext.clone();
      context.setParent(parentContext);
    } else {
      context = new SqlContext();
    }
    app.setContext(context);
    return context;
  }

  /**
   * Replaces the current SqlContext (checking it against the given
   * SqlContext) with the parent SqlContext (stored within the new one).  All
   * <code>popContext</code> calls should come after a corresponding
   * {@link #pushContext} call.
   */
  public static void popContext(AppendableExt app, SqlContext context)
  {
    if(app.getContext() != context) {
      throw new IllegalStateException("Mismatched push/pop SqlContext");
    }
    app.setContext((context != null) ? context.getParent() : null);
  }

  
}
