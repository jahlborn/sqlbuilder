/*
Copyright (c) 2016 James Ahlborn

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

package com.healthmarketscience.sqlbuilder.custom.postgresql;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Converter;

/**
 * Condition which supports PostgreSQL specific binary operators.
 *
 * @author James Ahlborn
 */
public class PgBinaryCondition extends BinaryCondition 
{
  public enum PgOp
  {
    ILIKE(" ILIKE "),
    NOT_ILIKE(" NOT ILIKE "),
    SIMILAR_TO(" SIMILAR TO "),
    NOT_SIMILAR_TO(" NOT SIMILAR TO "),
    MATCHES_RE(" ~ "),
    NOT_MATCHES_RE(" !~ "),
    IMATCHES_RE(" ~* "),
    NOT_IMATCHES_RE(" !~* ");

    private final String _opStr;

    private PgOp(String opStr) {
      _opStr = opStr;
    }

    @Override
    public String toString() { return _opStr; }
  }

  public PgBinaryCondition(PgOp binaryOp, Object leftValue, Object rightValue)
  {
    super(binaryOp, leftValue, rightValue);
  }

  @Override
  protected boolean supportsEscape(Object binaryOp) {
    return ((binaryOp == PgOp.ILIKE) || (binaryOp == PgOp.NOT_ILIKE) ||
            (binaryOp == PgOp.SIMILAR_TO) || (binaryOp == PgOp.NOT_SIMILAR_TO));
  }


  /**
   * Convenience method for generating a Condition for testing if a column is
   * "ilike" (case-insensitive like) a given value (sql pattern matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static PgBinaryCondition iLike(Object value1, Object value2) {
    return new PgBinaryCondition(PgOp.ILIKE, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column is
   * not "ilike" (case-insensitive like) a given value (sql pattern matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static PgBinaryCondition notILike(Object value1, Object value2) {
    return new PgBinaryCondition(PgOp.NOT_ILIKE, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column is
   * "similar to" a given value (sql regex matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static PgBinaryCondition similarTo(Object value1, Object value2) {
    return new PgBinaryCondition(PgOp.SIMILAR_TO, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column is
   * not "similar to" a given value (sql regex matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static PgBinaryCondition notSimilarTo(Object value1, Object value2) {
    return new PgBinaryCondition(PgOp.NOT_SIMILAR_TO, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * "regex matches" a given value (posix regex matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static PgBinaryCondition matchesRe(Object value1, Object value2) {
    return new PgBinaryCondition(PgOp.MATCHES_RE, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * does not "regex match" a given value (posix regex matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static PgBinaryCondition notMatchesRe(Object value1, Object value2) {
    return new PgBinaryCondition(PgOp.NOT_MATCHES_RE, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * "regex matches" (case-insensitive) a given value (posix regex matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static PgBinaryCondition iMatchesRe(Object value1, Object value2) {
    return new PgBinaryCondition(PgOp.IMATCHES_RE, value1, value2);
  }
    
  /**
   * Convenience method for generating a Condition for testing if a column
   * does not "regex match" (case-insensitive) a given value (posix regex
   * matching).
   * <p>
   * {@code Object} -&gt; {@code SqlObject} conversions handled by
   * {@link Converter#toColumnSqlObject(Object)}.
   */
  public static PgBinaryCondition notIMatchesRe(Object value1, Object value2) {
    return new PgBinaryCondition(PgOp.NOT_IMATCHES_RE, value1, value2);
  }
    
}
