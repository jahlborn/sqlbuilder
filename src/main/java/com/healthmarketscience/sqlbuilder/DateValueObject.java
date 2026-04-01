/*
Copyright (c) 2026 Health Market Science, Inc.

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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import com.healthmarketscience.common.util.AppendableExt;

/**
 * Outputs a date literal <code>DATE/TIME/TIMESTAMP '&lt;value&gt;'</code>.
 *
 * @author James Ahlborn
 */
public class DateValueObject extends Expression
{
  public enum Type {
    DATE("DATE "),
    TIME("TIME "),
    TIMESTAMP("TIMESTAMP ");

    private final String _str;

    private Type(String str) {
      _str = str;
    }

    @Override
    public String toString() { return _str; }
  }

  private Type _type;
  private Date _value;

  public DateValueObject(Type type, Date value) {
    _type = type;
    _value = value;
  }

  /** @return a date value literal of the appropriate type for the given
              object.  Object must be some subclass of Date or
              TemporalAccessor */
  public static DateValueObject of(Object obj) {
    if(obj instanceof Timestamp) {
      return timestamp((Timestamp)obj);
    }
    if(obj instanceof LocalDateTime) {
      return timestamp((LocalDateTime)obj);
    }
    if(obj instanceof Instant) {
      return timestamp((Instant)obj);
    }
    if(obj instanceof java.sql.Date) {
      return date((java.sql.Date)obj);
    }
    if(obj instanceof java.sql.Time) {
      return time((java.sql.Time)obj);
    }
    if(obj instanceof LocalDate) {
      return date((LocalDate)obj);
    }
    if(obj instanceof LocalTime) {
      return time((LocalTime)obj);
    }
    if(obj instanceof Date) {
      return timestamp((Date)obj);
    }
    return timestamp((TemporalAccessor)obj);
  }

  @Override
  public boolean hasParens() { return false; }

  @Override
  protected void collectSchemaObjects(ValidationContext vContext) {
  }

  @Override
  public void appendTo(AppendableExt app) throws IOException {
    app.append(_type).append("'").append(_value).append("'");
  }

  /** @return a TIMESTAMP value literal of the for the given value */
  public static DateValueObject timestamp(Timestamp ts) {
    return new DateValueObject(Type.TIMESTAMP, ts);
  }

  /** @return a TIMESTAMP value literal of the for the given value */
  public static DateValueObject timestamp(Date d) {
    return timestamp(new Timestamp(d.getTime()));
  }

  /** @return a TIMESTAMP value literal of the for the given value */
  public static DateValueObject timestamp(LocalDateTime ldt) {
    return timestamp(Timestamp.valueOf(ldt));
  }

  /** @return a TIMESTAMP value literal of the for the given value */
  public static DateValueObject timestamp(Instant i) {
    return timestamp(Timestamp.from(i));
  }

  /** @return a TIMESTAMP value literal of the for the given value */
  public static DateValueObject timestamp(TemporalAccessor ta) {
    return timestamp(LocalDateTime.from(ta));
  }

  /** @return a DATE value literal of the for the given value */
  public static DateValueObject date(java.sql.Date d) {
    return new DateValueObject(Type.DATE, d);
  }

  /** @return a DATE value literal of the for the given value */
  public static DateValueObject date(LocalDate ld) {
    return date(java.sql.Date.valueOf(ld));
  }

  /** @return a DATE value literal of the for the given value */
  public static DateValueObject date(TemporalAccessor ta) {
    return date(LocalDate.from(ta));
  }

  /** @return a TIME value literal of the for the given value */
  public static DateValueObject time(java.sql.Time t) {
    return new DateValueObject(Type.TIME, t);
  }

  /** @return a TIME value literal of the for the given value */
  public static DateValueObject time(LocalTime ld) {
    return time(java.sql.Time.valueOf(ld));
  }

  /** @return a TIME value literal of the for the given value */
  public static DateValueObject time(TemporalAccessor ta) {
    return time(LocalTime.from(ta));
  }
}
