/*
 * Copyright (c) 2018. Memiiso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.memiiso.nifi.melt.processors;

import com.memiiso.nifi.melt.dbcp.MeltDBCPService;
import com.memiiso.nifi.melt.db.MeltDBConnection;
import com.memiiso.nifi.melt.db.connection.MeltDerbyConnection;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.sql.*;

import static org.junit.Assert.assertEquals;

public class CTASTest {

    private static final String createPersons = "CREATE TABLE tempschema.PERSONS (id integer primary key, name varchar(100), code integer)";
    private static final String createTeachers = "CREATE TABLE stg.TEACHERS (id integer primary key, name varchar(100), code integer)";
    private TestRunner testRunner;
    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();
    static protected MeltDBCPService serviceMeltDBCP;

    @Before
    public void setUp() throws Exception {
        testRunner = TestRunners.newTestRunner(CTAS.class);
        testRunner.setValidateExpressionUsage(false);
        try {
            System.setProperty("derby.stream.error.file", "target/derby.log");
            final File tempDir = folder.getRoot();
            final File dbDir = new File(tempDir, "com/memiiso/nifi/melt/db");
            serviceMeltDBCP = new MockDBCPService(dbDir.getAbsolutePath());

            testRunner.addControllerService("dbcp", serviceMeltDBCP);
            testRunner.enableControllerService(serviceMeltDBCP);
            testRunner.setProperty(CTAS.MELT_DBCP_SERVICE, "dbcp");
            testRunner.setProperty(CTAS.MELT_QUERY_TIMEOUT, "0 seconds");
            testRunner.setProperty(CTAS.MELT_TEMPLATE, "CREATE TABLE ${target_table} AS ${source_statement} WITH NO DATA");

            try (final Connection conn = serviceMeltDBCP.getMeltDBConnection().getConnection()) {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.execute("create schema tempschema ");
                    stmt.execute("create schema stg ");
                    stmt.execute(createPersons);
                    stmt.execute(createTeachers);
                }catch (Exception e){

                }
            }

        } catch (InitializationException | ProcessException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSimpleCTASStatement() throws InitializationException, ProcessException, SQLException, IOException {
        try (final Connection conn = serviceMeltDBCP.getMeltDBConnection().getConnection()) {
            try (final Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeQuery(" drop table stg.stg_testctastable; " );
                }catch (Exception e){
                }
                testRunner.setProperty(CTAS.MELT_SOURCE, "select 'Mark' as stringcol, current_date as current_datecoll, '123' as integercoll from tempschema.PERSONS ");
                testRunner.setProperty(CTAS.MELT_TARGET_TABLE, " stg.stg_testctastable ");
                testRunner.setProperty(CTAS.MELT_ELT_STATEMENT, "create table stg.stg_testctastable as select 'Mark' as stringcol, current_date as current_datecoll, '123' as integercoll from tempschema.PERSONS WITH NO DATA");
                testRunner.run();
                testRunner.assertTransferCount(CTAS.REL_SUCCESS, 1);
                final ResultSet rs = stmt.executeQuery("SELECT * FROM stg.stg_testctastable");
                assertEquals("stringcol", rs.getMetaData().getColumnName(1).toLowerCase());
                assertEquals("current_datecoll", rs.getMetaData().getColumnName(2).toLowerCase());
            }
        }
    }

    @Test
    public void testInvalidStatement() throws InitializationException, ProcessException, SQLException, IOException {

        final String sql = "INSERT INTO PERSONS (ID, NAME, CODE) VALUES (?, ?, ?); " +
                "UPDATE SOME_RANDOM_TABLE NAME='George' WHERE ID=?; ";

        testRunner.setProperty(CTAS.MELT_SOURCE, sql);
        testRunner.setProperty(CTAS.MELT_TARGET_TABLE, " stg.stg_testctastable2");
        testRunner.run();

        // should fail because of the semicolon
        testRunner.assertAllFlowFilesTransferred(CTAS.REL_FAILURE, 1);
    }

    @Test
    public void testSimpleCTASStatement2() throws ClassNotFoundException, SQLException, InitializationException, IOException {
        // load test data to database
        final Connection con = ((MeltDBCPService) testRunner.getControllerService("dbcp")).getMeltDBConnection().getConnection();
        Statement stmt = con.createStatement();
        try {
            stmt.execute("drop table STG.TEST_QUERY_DB_TABLE");
        } catch (final SQLException sqle) {
            // Ignore this error, probably a "table does not exist" since Derby doesn't yet support DROP IF EXISTS [DERBY-4842]
        }
        testRunner.setIncomingConnection(true);
        testRunner.setProperty(CTAS.MELT_TARGET_TABLE, "STG.TEST_QUERY_DB_TABLE");
        testRunner.setProperty(CTAS.MELT_SOURCE, " SELECT s.id AS personiddd, p2.name AS teac_name,p2.name AS teac_name33, s.name, 123 as coll, 321 as col2, 'dfhdf' col3 " +
                "FROM tempschema.PERSONS s " +
                "left join stg.teachers p2 on s.id=p2.id");
        testRunner.run();
        testRunner.assertAllFlowFilesTransferred(CTAS.REL_SUCCESS, 1);
        testRunner.clearTransferState();
    }


    /**
     * Simple implementation only for testing purposes
     */
    private class MockDBCPService extends AbstractControllerService implements MeltDBCPService {
        private final String dbLocation;

        public MockDBCPService(final String dbLocation) {
            this.dbLocation = dbLocation;
        }

        @Override
        public String getIdentifier() {
            return "dbcp";
        }

        @Override
        public MeltDBConnection getMeltDBConnection() throws ProcessException {
            return new MeltDerbyConnection(this.getConnection());
        }

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

}