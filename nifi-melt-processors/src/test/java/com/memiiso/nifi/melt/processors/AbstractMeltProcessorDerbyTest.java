package com.memiiso.nifi.melt.processors;

import com.memiiso.nifi.melt.db.MeltDBConnection;
import com.memiiso.nifi.melt.db.connection.MeltDerbyConnection;
import com.memiiso.nifi.melt.dbcp.MeltDBCPService;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractMeltProcessorDerbyTest {

    private static final String createPersons = "CREATE TABLE tempschema.PERSONS (id integer primary key, name varchar(100), code integer)";
    private static final String createTeachers = "CREATE TABLE stg.TEACHERS (id integer primary key, name varchar(100), code integer)";
    public TestRunner testRunner;
    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();
    static protected MeltDBCPService serviceMeltDBCP;

    @Before
    public void setUp() throws Exception {
        testRunner.setValidateExpressionUsage(false);
        try {
            System.setProperty("derby.stream.error.file", "target/derby.log");
            final File tempDir = folder.getRoot();
            final File dbDir = new File(tempDir, "com/memiiso/nifi/melt/db");
            serviceMeltDBCP = new MockDBCPService(dbDir.getAbsolutePath());

            testRunner.addControllerService("dbcp", serviceMeltDBCP);
            testRunner.enableControllerService(serviceMeltDBCP);

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