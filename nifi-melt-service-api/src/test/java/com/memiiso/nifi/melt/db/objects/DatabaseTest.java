/*
 * Copyright (c) 2018. Memiiso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.memiiso.nifi.melt.db.objects;

import com.bazaarvoice.jolt.JsonUtils;
import com.memiiso.nifi.melt.db.MeltDBConnection;
import com.memiiso.nifi.melt.db.connection.MeltDerbyConnection;
import com.memiiso.nifi.melt.db.objects.Database;
import org.apache.nifi.processor.exception.ProcessException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertNotNull;

public class DatabaseTest {
    private static final String createPersons = "CREATE TABLE PERSONS (id integer primary key, name varchar(100), code integer)";
    private static final String createPersonsStg = "CREATE TABLE stg.PERSONS (id integer primary key, name varchar(100), code integer)";

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();
    static MeltDBConnection da ;

    @Before
    public void init() throws SQLException, ClassNotFoundException {
        try {
            System.setProperty("derby.stream.error.file", "target/derby.log");
            final File tempDir = folder.getRoot();
            final File dbDir = new File(tempDir, "com/memiiso/nifi/melt/db");
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbDir + ";create=true");
            da = new MeltDerbyConnection(conn);

            final Statement stmt = conn.createStatement();
            stmt.executeUpdate(createPersons);
            stmt.executeUpdate("create schema stg ");
            stmt.executeUpdate(createPersonsStg);
        } catch ( ProcessException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetDatabaseInformation() throws  SQLException {
            Database dbmeta = da.getDatabaseInformation();
            assertNotNull(dbmeta);
            System.out.println(JsonUtils.toPrettyJsonString(dbmeta));
            //System.out.println(dbmeta.getSchemaList().keySet().toString());
            //assertEquals(true, dbmeta.getSchemaList().containsKey("STG"));
            //assertEquals(true, dbmeta.getSchemaList().get("STG").getTableList().containsKey("PERSONS"));
            //assertEquals(true, dbmeta.getSchemaList().get("STG").getTableList().get("PERSONS").getColumnList().containsKey("NAME"));
            //assertEquals(true, dbmeta.getSchemaList().get("STG").getTableList().get("PERSONS").getColumnList().containsKey("ID"));
    }

}
