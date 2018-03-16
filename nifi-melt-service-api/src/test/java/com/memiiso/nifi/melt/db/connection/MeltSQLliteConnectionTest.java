/*
 * Copyright (c) 2018. Memiiso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.memiiso.nifi.melt.db.connection;

import com.memiiso.nifi.melt.db.MeltDBConnection;
import com.memiiso.nifi.melt.db.objects.Database;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memiiso.nifi.melt.db.objects.Database;
import org.apache.nifi.processor.exception.ProcessException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.sql.*;

import static org.junit.Assert.*;

public class MeltSQLliteConnectionTest {
    private static final String createPersons = "CREATE TABLE if not exists PERSONS (id integer primary key, name varchar(100), code integer)";
    private static final String createPersonsStg = "CREATE TABLE if not exists stg_PERSONS (id integer primary key, name varchar(100), code integer)";

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();
    static MeltDBConnection da ;

    @Before
    public void init() throws SQLException, ClassNotFoundException {
        try {
            // create a database connection
            Connection conn = DriverManager.getConnection("jdbc:sqlite:/opt/databases/sqlite/chinook.db");
            Statement statement = conn.createStatement();
            da = new MeltSQLliteConnection(conn);

            final Statement stmt = conn.createStatement();
            stmt.executeUpdate(createPersons);
            stmt.executeUpdate(createPersonsStg);
        } catch ( ProcessException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void printInfo() throws SQLException {
        System.out.println(this.da.getConnection().getMetaData().getDatabaseProductName());

        DatabaseMetaData metaData = this.da.getConnection().getMetaData();
        ResultSet schemas = metaData.getSchemas();
        ResultSet catalogs = metaData.getCatalogs();
        if (!schemas.isBeforeFirst() ) {
            System.out.println("No schemas data");
        }
        if (!catalogs.isBeforeFirst() ) {
            System.out.println("No catalogs data");
        }
        String[] types = { "TABLE", "VIEW" };
        ResultSet tables = metaData.getTables("defaultttt", null, "%", types);
        System.out.println(tables.getString(3).toString());
        if (!tables.isBeforeFirst() ) {
            System.out.println("No tables data");
        }

    }

    @Test
    public void testGetDatabaseInformation() throws SQLException, JsonProcessingException {
        System.out.println(da.getName());
        Database dbmeta = da.getDatabaseInformation();
        System.out.println(new ObjectMapper().writeValueAsString(dbmeta));

        assertEquals(true, dbmeta.getSchemaList().containsKey("NoSchemaFound"));
        assertEquals(true, dbmeta.getSchemaList().get("NoSchemaFound").getTableList().containsKey("PERSONS"));
        assertEquals(true, dbmeta.getSchemaList().get("NoSchemaFound").getTableList().get("PERSONS").getColumnList().containsKey("name"));
        assertEquals(true, dbmeta.getSchemaList().get("NoSchemaFound").getTableList().get("stg_PERSONS").getColumnList().containsKey("id"));
    }

    @Test
    public void testGetQueryMetaJson() throws SQLException, JsonProcessingException {
        String querymetadata = da.getQueryMetaJson("select id as col_id, id, t.* from PERSONS as t","test");
        System.out.println(querymetadata);
        }

/*
    @Test
    public void testGetUpdateQueryMetaJson() throws SQLException, JsonProcessingException {

        ResultSetMetaData s = da.getQueryMeta("update PERSONS set name = 'new_name' where id = 5 ");
        System.out.println(s.toString());
        System.out.println(s.getColumnCount());
    }

    @Test
    public void testGetInsertQueryMetaJson() throws SQLException, JsonProcessingException {

        //ResultSetMetaData s = da.getQueryMeta("insert into PERSONS (id) values (1234) ");
        ResultSetMetaData s = da.getConnection().(statement).getMetaData();

        System.out.println(s.toString());
        System.out.println(s.getColumnCount());
    }
    */

}