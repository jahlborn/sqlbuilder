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

/**
 * Interface for SqlObjects wishing to provide verifiablity.  In general, all
 * checking in SqlObject implementations is deferred until an object is fully
 * constructed.  As a post process, a user may choose to validate the sql
 * construct after it is fully constructed by calling the validate method.
 *
 * @author James Ahlborn
 */
public interface Verifiable<ThisType extends Verifiable<ThisType>> {

  /**
   * Runs validation on this verifiable object.
   * 
   * @return a handle to this instance
   */
  public ThisType validate() throws ValidationException;
  
  /**
   * Runs validation on this verifiable object using a previously collected
   * ValidationContext.
   * <p>
   * In general, this method will only be called internally, not by users.
   *
   * @param vContext handle to the current, filled-in validation context
   */
  public void validate(ValidationContext vContext)
    throws ValidationException;
  
}
