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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
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

  /** @return a JDBC escaped value with the given LocalDate */
  public static JdbcEscape date(LocalDate d) {
    return date(java.sql.Date.valueOf(d));
  }

  /** @return a JDBC escaped value with the date portion of the given
              TemporalAccessor */
  public static JdbcEscape date(TemporalAccessor t) {
    return date(LocalDate.from(t));
  }

  /** @return a JDBC escaped value with the time portion of the given Date */
  public static JdbcEscape time(Date d) {
    java.sql.Time sqlTime = ((d instanceof java.sql.Time) ?
                             (java.sql.Time)d :
                             new java.sql.Time(d.getTime()));
    return new JdbcEscape(Type.TIME, new ValueObject(sqlTime));
  }

  /** @return a JDBC escaped value with the given LocalTime */
  public static JdbcEscape time(LocalTime t) {
    return time(java.sql.Time.valueOf(t));
  }

  /** @return a JDBC escaped value with the time portion of the given
              TemporalAccessor */
  public static JdbcEscape time(TemporalAccessor t) {
    return time(LocalTime.from(t));
  }

  /** @return a JDBC escaped value with the date-time portion of the given
              Date (including milliseconds) */
  public static JdbcEscape timestamp(Date d) {
    java.sql.Timestamp sqlTimestamp = ((d instanceof java.sql.Timestamp) ?
                                       (java.sql.Timestamp)d :
                                       new java.sql.Timestamp(d.getTime()));
    return new JdbcEscape(Type.TIMESTAMP, new ValueObject(sqlTimestamp));
  }

  /** @return a JDBC escaped value with the given LocalDate (including
              nanoseconds) */
  public static JdbcEscape timestamp(LocalDateTime d) {
    return timestamp(java.sql.Timestamp.valueOf(d));
  }

  /** @return a JDBC escaped value with the date-time portion of the given
              TemporalAccessor (including nanoseconds)*/
  public static JdbcEscape timestamp(TemporalAccessor t) {
    return timestamp(LocalDateTime.from(t));
  }

}
