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

package com.healthmarketscience.sqlbuilder.dbspec;

/**
 * Maintains information about a database function for use with the sqlbuilder
 * utilities.
 *
 * @author James Ahlborn
 */
public interface Function {

  /** @return a string which represents an absolute (fully qualified)
      reference to this function, suitable for sql statements */
  public String getFunctionNameSQL();

}
