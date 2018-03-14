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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class TestCTAS {
    private static final String createPersons = "CREATE TABLE PERSONS (id integer primary key, name varchar(100), code integer)";

    private TestRunner testRunner;

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    /**
     * Setting up Connection pooling is expensive operation.
     * So let's do this only once and reuse MockDBCPService in each test.
     */
    static protected DBCPService service;

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(CTAS.class);
        testRunner.setValidateExpressionUsage(false);
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

			testRunner.addControllerService("dbcp", service);
	        testRunner.enableControllerService(service);
	        testRunner.setProperty(CTAS.CONNECTION_POOL, "dbcp");
	        //testRunner.setProperty(CTAS.SQL_CTAS_TABLE, "stg.stg_testctastable");
	        //testRunner.setProperty(CTAS.SQL_CTAS_QUERY, "select current_date as currdatecoll, 123 as coll2 from dual");
	        testRunner.setProperty(CTAS.QUERY_TIMEOUT, "0 seconds");

	        recreateTable("PERSONS", createPersons);
	        
		} catch (InitializationException | ProcessException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Test
    public void testSimpleCTASStatement() throws InitializationException, ProcessException, SQLException, IOException {
        testRunner.setProperty(CTAS.CTAS_SQL_EXPRESSION, "select 'Mark' as stringcol, current_date as current_datecoll, '123' as integercoll from PERSONS ");
        testRunner.setProperty(CTAS.SQL_CTAS_TABLE, " stg.stg_testctastable ");
        //testRunner.enqueue("INSERT INTO PERSONS (ID, NAME, CODE) VALUES (1, 'Mark', 84)".getBytes());
	    	testRunner.run();
	    	testRunner.assertTransferCount(CTAS.REL_SUCCESS, 1);
        try (final Connection conn = service.getConnection()) {
            try (final Statement stmt = conn.createStatement()) {
                final ResultSet rs = stmt.executeQuery("SELECT * FROM stg.stg_testctastable");
                assertEquals("stringcol", rs.getMetaData().getColumnName(1).toLowerCase());
                assertEquals("current_datecoll", rs.getMetaData().getColumnName(2).toLowerCase());
            }
        }
    }
    /*
    @Test
    public void testInvalidStatement() throws InitializationException, ProcessException, SQLException, IOException {

        final String sql = "INSERT INTO PERSONS (ID, NAME, CODE) VALUES (?, ?, ?); " +
                "UPDATE SOME_RANDOM_TABLE NAME='George' WHERE ID=?; ";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("sql.args.1.type", String.valueOf(Types.INTEGER));
        attributes.put("sql.args.1.value", "1");

        attributes.put("sql.args.2.type", String.valueOf(Types.VARCHAR));
        attributes.put("sql.args.2.value", "Mark");

        attributes.put("sql.args.3.type", String.valueOf(Types.INTEGER));
        attributes.put("sql.args.3.value", "84");

        attributes.put("sql.args.4.type", String.valueOf(Types.INTEGER));
        attributes.put("sql.args.4.value", "1");

        testRunner.enqueue(sql.getBytes(), attributes);
        testRunner.run();

        // should fail because of the semicolon
        testRunner.assertAllFlowFilesTransferred(CTAS.REL_FAILURE, 1);

        try (final Connection conn = service.getConnection()) {
            try (final Statement stmt = conn.createStatement()) {
                final ResultSet rs = stmt.executeQuery("SELECT * FROM PERSONS");
                assertFalse(rs.next());
            }
        }
    }

    @Test
    public void testStatementsFromProperty() throws InitializationException, ProcessException, SQLException, IOException {

    		testRunner.enqueue("This statement should be ignored".getBytes(), new HashMap<String,String>() {{
            put("row.id", "1");
        }});
        testRunner.run();

        testRunner.assertAllFlowFilesTransferred(CTAS.REL_SUCCESS, 1);

        try (final Connection conn = service.getConnection()) {
            try (final Statement stmt = conn.createStatement()) {
                final ResultSet rs = stmt.executeQuery("SELECT * FROM PERSONS");
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
                assertEquals("Mark", rs.getString(2));
                assertEquals(84, rs.getInt(3));
                assertFalse(rs.next());
            }
        }
    }
    
*/

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
