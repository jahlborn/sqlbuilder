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

/**
 * SqlBuilder supports two types of custom SQL:
 * <ul>
 * <li>Custom values/expressions within existing queries - this type of
 *     customization can be achieved using the custom SqlObject variants:
 *     {@link com.healthmarketscience.sqlbuilder.CustomSql}, {@link
 *     com.healthmarketscience.sqlbuilder.CustomExpression}, and {@link
 *     com.healthmarketscience.sqlbuilder.CustomCondition}.</li>
 * <li>Custom clauses added to existing queries - the classes in this package
 *     (and subpackages) enable this type of customization, read on for
 *     details.</li>
 * </ul>
 * <p/>
 * <h2>Custom SQL Syntax</h2>
 * <p/>
 * Inserting custom SQL syntax into queries involves a few different pieces
 * all working together.
 * <ul>
 * <li>A query (or other existing SqlObject type) which has enabled custom SQL
 *     syntax support (e.g. {@link com.healthmarketscience.sqlbuilder.SelectQuery}).  This is done via a
 *     nested {@code Hook} enum which defines the "hook anchors" for that type
 *     (e.g. {@link com.healthmarketscience.sqlbuilder.SelectQuery.Hook}).</li>
 * <li>A "hook anchor" (an enum which implements the {@link
 *     com.healthmarketscience.sqlbuilder.custom.HookAnchor} marker interface)
 *     defines locations within the related query in which custom SQL syntax
 *     can be inserted.  Custom SQL syntax generally falls into one of two
 *     categories: additional keywords which enhance an existing clause or
 *     additional sub-clauses within a query.  In both cases, the custom
 *     syntax needs to be inserted into the correct location within the query
 *     and the "hook anchor" provides the first of two important bits of
 *     location information.</li>
 * <li>The {@link com.healthmarketscience.sqlbuilder.custom.HookType} is the
 *     second bit of important information which ties the custom SQL syntax to
 *     the correct location in the query.  It defines how the custom syntax
 *     should be inserted relative to the "hook anchor".</li>
 * <li>The actual custom SQL syntax object.  For convenience, the custom
 *     syntax implementation may extend {@link com.healthmarketscience.sqlbuilder.custom.CustomSyntax}, but that is
 *     not a requirement.  {@code CustomSyntax} based implementations know how
 *     to insert themselves into the correct location in relevant queries so

 *     they can easily be added to a query using the simplified {@code
 *     addCustomization()} method (e.g. {@link com.healthmarketscience.sqlbuilder.SelectQuery#addCustomization(CustomSyntax)}).
 *     However, any ad hoc custom syntax can be added to customizable queries
 *     by specifying the HookType and "hook anchor" when adding (e.g. {@link com.healthmarketscience.sqlbuilder.SelectQuery#addCustomization(Hook,HookType,Object)})</li>
 * </ul>
 * <p/>
 * <h3>Supported Customizations</h3>
 * <p/>
 * SqlBuilder currently has existing constructs for few common database
 * specific customizations:
 * <ul>
 * <li>Oracle {@link com.healthmarketscience.sqlbuilder.custom.oracle}</li>
 * <li>MySQL {@link com.healthmarketscience.sqlbuilder.custom.mysql}</li>
 * <li>PostreSQL {@link com.healthmarketscience.sqlbuilder.custom.postgresql}</li>
 * <li>SQLServer {@link com.healthmarketscience.sqlbuilder.custom.sqlserver}</li>
 * </ul>
 * <p/>
 * <h3>Customizable Queries</h3>
 * <p/>
 * Only a few SqlBuilder queryies are currently customizable:
 * <ul>
 * <li>{@link com.healthmarketscience.sqlbuilder.SelectQuery}</li>
 * <li>{@link com.healthmarketscience.sqlbuilder.CreateTableQuery}</li>
 * <li>{@link com.healthmarketscience.sqlbuilder.CreateIndexQuery}</li>
 * </ul>
 */
package com.healthmarketscience.sqlbuilder.custom;

