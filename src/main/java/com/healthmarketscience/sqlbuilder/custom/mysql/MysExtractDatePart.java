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

package com.healthmarketscience.sqlbuilder.custom.mysql;

/**
 * The MySQL defined date parts for the
 * {@link com.healthmarketscience.sqlbuilder.ExtractExpression}.
 *
 * @see "SQL 2003"
 * @author James Ahlborn
 */
public enum MysExtractDatePart 
{
  MICROSECOND,
  SECOND,
  MINUTE,
  HOUR,
  DAY,
  WEEK,
  MONTH,
  QUARTER,
  YEAR,
  SECOND_MICROSECOND,
  MINUTE_MICROSECOND,
  MINUTE_SECOND,
  HOUR_MICROSECOND,
  HOUR_SECOND,
  HOUR_MINUTE,
  DAY_MICROSECOND,
  DAY_SECOND,
  DAY_MINUTE,
  DAY_HOUR,
  YEAR_MONTH;
}
