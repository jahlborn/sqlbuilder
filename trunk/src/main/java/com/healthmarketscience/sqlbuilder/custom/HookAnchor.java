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
 * Marker interface for the custom hook anchors use by specific SqlObjects.
 * The hook anchors typically refer to sub-clauses within the query, but may
 * also be absolute positions.
 * <p>
 * See {@link com.healthmarketscience.sqlbuilder.custom} for more details on
 * custom SQL syntax.
 *
 * @author James Ahlborn
 */
public interface HookAnchor 
{

}
