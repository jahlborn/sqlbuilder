/*
Copyright (c) 2015 James Ahlborn

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

package com.healthmarketscience.sqlbuilder.custom;

/**
 * Enum defining the type of customization relative to the HookAnchor
 * (which is typically a sub-clause within a query).
 * <p/>
 * The {@code PREFIX}, {@code REPLACEMENT}, and {@code SUFFIX} types are all
 * dependent on the anchor clause itself.  These customizations will only be
 * inserted if the anchor clause itself is included.  Note that if any {@code
 * REPLACEMENT} type is used, the anchor text itself will <i>not</i> be
 * inserted.
 * <p/>
 * The {@code BEFORE} type, while similar in location to the {@code PREFIX}
 * type, is included regardless of whether or not the anchor clause itself is
 * included (it is more related to the surrounding clause than the anchor
 * clause itself).
 * <p>
 * Note that customizable queries support multiple instances of each type for
 * a given anchor.  Multiple customizations of the same type will be inserted
 * in the order they were added to the query.
 *
 * @author James Ahlborn
 */
public enum HookType 
{
  /**
   * Customization which is inserted before the anchor clause.  Unlike the
   * {@code PREFIX} type, this type of customization will <i>always</i> be
   * inserted regardless of whether or not the related anchor clause itself is
   * included in the query.
   */
  BEFORE, 
  /**
   * Customization which is inserted before the anchor clause, but only if the
   * anchor clause itself is included in the query.
   */
  PREFIX, 
  /**
   * Customization which <i>replaces</i> the anchor clause text (the related
   * anchor clause text will not be inserted if this type of hook is added),
   * but only if the anchor clause itself is included in the query.
   */
  REPLACEMENT, 
  /**
   * Customization which is inserted after the anchor clause, but only if the
   * anchor clause itself is included in the query.
   */
  SUFFIX;
}
