// Copyright (c) 2008 Health Market Science, Inc.

package com.healthmarketscience.sqlbuilder;

/**
 * Interface for SqlObjects wishing to provide verifiablity.  In general, all
 * checking in SqlObject implementations is deferred until an object is fully
 * constructed.  As a post process, a user may choose to validate the sql
 * construct after it is fully constructed by calling the validate method.
 *
 * @author James Ahlborn
 */
public interface Verifiable<ThisType extends Verifiable> {

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
