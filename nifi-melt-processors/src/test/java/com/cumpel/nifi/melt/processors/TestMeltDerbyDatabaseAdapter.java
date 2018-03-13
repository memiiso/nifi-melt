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
package com.cumpel.nifi.melt.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.cumpel.nifi.melt.processors.db.MeltDatabaseAdapter;
import com.cumpel.nifi.melt.processors.db.MeltDatabaseFactory;
import com.cumpel.nifi.melt.processors.db.impl.MeltDerbyDatabaseAdapter;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.processor.exception.ProcessException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;


public class TestMeltDerbyDatabaseAdapter {
    private static final String createPersons = "CREATE TABLE PERSONS (id integer primary key, name varchar(100), code integer)";

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    /**
     * Setting up Connection pooling is expensive operation.
     * So let's do this only once and reuse MockDBCPService in each test.
     */
    static protected DBCPService service;

    @Before
    public void init() {
        try {
            System.setProperty("derby.stream.error.file", "target/derby.log");
            final File tempDir = folder.getRoot();
            final File dbDir = new File(tempDir, "db");
            service = new MockDBCPService(dbDir.getAbsolutePath());
            try (final Connection conn = service.getConnection()) {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(createPersons);
                }
            }
		} catch ( ProcessException | SQLException e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void testValidateSelect() throws SQLException {
        try (final Connection conn = service.getConnection()) {
            MeltDerbyDatabaseAdapter da = new MeltDerbyDatabaseAdapter();
            try (final Statement stmt = conn.createStatement()) {
                assertEquals("VALID", da.validateSelect(conn,"SELECT * FROM PERSONS"));
                assertNotEquals("VALID", da.validateSelect(conn,"SELECT * FROM xyz.PERSONS"));
            }
        }
    }
    
    @Test
    public void testGetSelectMeta() throws  SQLException {
        try (final Connection conn = service.getConnection()) {
            //MeltDatabaseAdapter da = new MeltDatabaseFactory().getInstance(conn);
            MeltDerbyDatabaseAdapter da = new MeltDerbyDatabaseAdapter();
            try (final Statement stmt = conn.createStatement()) {     
                Assert.assertEquals(5, da.getSelectMeta(conn,"SELECT id AS personiddd,name, 123 as coll, 321 as col2, 'dfhdf' col3 FROM PERSONS").getColumnCount());
	            //assertEquals("id", da.getSelectMeta(conn,"SELECT id AS personiddd,name, 123 as coll, 321 as col2, 'dfhdf' col3 FROM PERSONS").getColumnName(1).toLowerCase());
                Assert.assertEquals("personiddd", da.getSelectMeta(conn,"SELECT id AS personiddd,name, 123 as coll, 321 as col2, 'dfhdf' col3 FROM PERSONS").getColumnLabel(1).toLowerCase());
                Assert.assertEquals("persons", da.getSelectMeta(conn,"SELECT id AS personiddd,name, 123 as coll, 321 as col2, 'dfhdf' col3 FROM PERSONS").getTableName(1).toLowerCase());
                //assertEquals("id", da.getSelectMeta(conn,"SELECT id AS personiddd,name, id-123 as coll, 321 as col2, 'dfhdf' col3 FROM PERSONS").getTableName(3).toLowerCase());
            }
        }
    }

    @Test
    public void testGetDatabaseSchemas() throws  SQLException {
        try (final Connection conn = service.getConnection()) {
            this.recreateTable("PERSONS",createPersons);
            MeltDatabaseAdapter da = new MeltDatabaseFactory().getInstance(conn);
            ArrayList<String> databaseSchemas = da.getDatabaseSchemas(conn);
            assertEquals(true, databaseSchemas.contains("STG"));
        }
    }

    /**
     * Simple implementation only for testing purposes
     */
    public static class MockDBCPService extends AbstractControllerService implements DBCPService {
        private final String dbLocation;

        public MockDBCPService(final String dbLocation) {
            this.dbLocation = dbLocation;
        }

        @Override
        public String getIdentifier() {
            return "dbcp";
        }

        @Override
        public Connection getConnection() throws ProcessException {
            try {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbLocation + ";create=true");
                return conn;
            } catch (final Exception e) {
                e.printStackTrace();
                throw new ProcessException("getConnection failed: " + e);
            }
        }
    }
    
    private void recreateTable(String tableName, String createSQL) throws ProcessException, SQLException {
        try (final Connection conn = service.getConnection()) {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("create schema stg ");
                stmt.executeUpdate("drop table " + tableName);
                stmt.executeUpdate(createSQL);
            }
        }
    }

}
