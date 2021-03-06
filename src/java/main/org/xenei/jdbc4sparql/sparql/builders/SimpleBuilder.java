/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xenei.jdbc4sparql.sparql.builders;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Resource;

import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xenei.jdbc4sparql.iface.TableDef;
import org.xenei.jdbc4sparql.sparql.SparqlCatalog;
import org.xenei.jdbc4sparql.sparql.SparqlColumnDef;
import org.xenei.jdbc4sparql.sparql.SparqlTableDef;

public class SimpleBuilder implements SchemaBuilder
{
	public static final String BUILDER_NAME = "Simple_Builder";
	public static final String DESCRIPTION = "A simple schema builder that builds tables based on RDFS Class names";

	// Params: namespace.
	private static final String TABLE_QUERY = " prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
			+ " SELECT ?tName WHERE { ?tName a rdfs:Class ; " + " }";

	// Params: class resource, namespace
	private static final String COLUMN_QUERY = "SELECT DISTINCT ?cName "
			+ " WHERE { " + " ?instance a <%s> ; " + " ?cName [] ; }";

	private static final String TABLE_SEGMENT = "%1$s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> %2$s";
	private static final String COLUMN_SEGMENT = "%1$s %3$s %2$s";

	public SimpleBuilder()
	{
	}

	private void addColumnDefs( final SparqlCatalog catalog,
			final SparqlTableDef tableDef, final Resource tName )
	{
		final List<QuerySolution> solns = catalog.executeQuery(String.format(
				SimpleBuilder.COLUMN_QUERY, tName));
		for (final QuerySolution soln : solns)
		{
			final Resource cName = soln.getResource("cName");
			final SparqlColumnDef colDef = new SparqlColumnDef(
					cName.getNameSpace(), cName.getLocalName(), Types.VARCHAR,
					SimpleBuilder.COLUMN_SEGMENT);
			colDef.setNullable(DatabaseMetaData.columnNullable);
			tableDef.add(colDef);
		}
	}

	@Override
	public Set<TableDef> getTableDefs( final SparqlCatalog catalog )
	{
		final HashSet<TableDef> retval = new HashSet<TableDef>();
		final List<QuerySolution> solns = catalog
				.executeQuery(SimpleBuilder.TABLE_QUERY);
		for (final QuerySolution soln : solns)
		{
			final Resource tName = soln.getResource("tName");
			final SparqlTableDef tableDef = new SparqlTableDef(
					tName.getNameSpace(), tName.getLocalName(),
					SimpleBuilder.TABLE_SEGMENT);
			addColumnDefs(catalog, tableDef, tName);
			retval.add(tableDef);
		}
		return retval;
	}

}
