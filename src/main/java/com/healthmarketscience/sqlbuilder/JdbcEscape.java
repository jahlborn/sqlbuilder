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
import java.util.Date;

import com.healthmarketscience.common.util.AppendableExt;

/**
 * Outputs SQL and a prefix enclosed within JDBC escape syntax
 * <code>"{&lt;prefix&gt; &lt;sql&gt;}"</code>.
 *
 * @author James Ahlborn
 */
public class JdbcEscape extends Expression
{
  
  /**
   * Enum which defines the escape types supported.
   */
  public enum Type
  {
    SCALAR_FUNCTION("fn "),
    DATE("d "),
    TIME("t "),
    TIMESTAMP("ts "),
    OUTER_JOIN("oj "),
    STORED_PROCEDURE("call "),
    STORED_PROCEDURE_WITH_RETURN("?= call "),
    ESCAPE("escape ");

    private final String _prefixStr;

    private Type(String prefixStr) {
      _prefixStr = prefixStr;
    }
    
    @Override
    public String toString() { return _prefixStr; }
  }
  
  private Type _type;
  private SqlObject _val;
  
  public JdbcEscape(Type type, SqlObject val) {
    _type = type;
    _val = val;
  }

  @Override
  public boolean hasParens() { return false; }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
    _val.collectSchemaObjects(vContext);
  }
    
  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append("{").append(_type).append(_val).append("}");
  }

  /** @return a JDBC escaped value with the date portion of the given Date */
  public static JdbcEscape date(Date d) {
    java.sql.Date sqlDate = ((d instanceof java.sql.Date) ?
                             (java.sql.Date)d :
                             new java.sql.Date(d.getTime()));
    return new JdbcEscape(Type.DATE, new ValueObject(sqlDate));
  }
  
  /** @return a JDBC escaped value with the time portion of the given Date */
  public static JdbcEscape time(Date d) {
    java.sql.Time sqlTime = ((d instanceof java.sql.Time) ?
                             (java.sql.Time)d :
                             new java.sql.Time(d.getTime()));
    return new JdbcEscape(Type.TIME, new ValueObject(sqlTime));
  }
  
  /** @return a JDBC escaped value with the date-time portion of the given
              Date (including milliseconds) */
  public static JdbcEscape timestamp(Date d) {
    java.sql.Timestamp sqlTimestamp = ((d instanceof java.sql.Timestamp) ?
                                       (java.sql.Timestamp)d :
                                       new java.sql.Timestamp(d.getTime()));
    return new JdbcEscape(Type.TIMESTAMP, new ValueObject(sqlTimestamp));
  }
  
}
