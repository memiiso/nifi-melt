/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cumpel.nifi.processors.melt.db;

import com.cumpel.nifi.processors.melt.db.MeltDatabaseAdapter;
import com.cumpel.nifi.processors.melt.db.impl.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.nifi.dbcp.DBCPService;

/**
 * Interface for RDBMS/JDBC-specific code.
 */
public class MeltDatabaseFactory {
	private static MeltDatabaseAdapter myMeltDatabaseAdapter = null;
	
	public static MeltDatabaseAdapter getInstance(Connection con) throws SQLException  {
		if (myMeltDatabaseAdapter == null) {
			DatabaseMetaData metaData = con.getMetaData();
			String dbProductName = metaData.getDatabaseProductName();
			String dbVersion = metaData.getDatabaseProductVersion();
	
			if (dbProductName.equalsIgnoreCase("MySQL")) {
				myMeltDatabaseAdapter= new MeltMSSQLDatabaseAdapter();
			} else if (dbProductName.equalsIgnoreCase("PostgreSQL")) {
				myMeltDatabaseAdapter= new MeltGenericDatabaseAdapter();
			} else if (dbProductName.equalsIgnoreCase("Apache Derby")) {
				myMeltDatabaseAdapter= new MeltDerbyDatabaseAdapter();
			} else if (dbProductName.equalsIgnoreCase("Microsoft SQL Server")) {
				myMeltDatabaseAdapter= new MeltMSSQLDatabaseAdapter();
			} else {
				myMeltDatabaseAdapter= new MeltGenericDatabaseAdapter();
			}
		}
		return myMeltDatabaseAdapter;
	}

}
