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
import com.memiiso.nifi.melt.db.connection.MeltPostgresqlConnection;
import com.memiiso.nifi.melt.db.objects.Database;
import com.memiiso.nifi.melt.db.objects.Database;
import org.apache.nifi.processor.exception.ProcessException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.distribution.Version;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6;

public class MeltPostgresqlConnectionTest {

    private static final String createPersons = "CREATE TABLE PERSONS (id integer primary key, name varchar(100), code integer)";
    private static final String createTeacher = "CREATE TABLE stg.teachers (id integer primary key, name varchar(100), code integer)";
    private static final EmbeddedPostgres postgres = new EmbeddedPostgres(Version.Main.V9_6);
    private static MeltDBConnection da;

    @After
    public  void tearDown() throws Exception {
        postgres.getProcess().ifPresent(PostgresProcess::stop);
    }

    @Before
    public void init() {
        try {
            final String url = postgres.start();
            da = new MeltPostgresqlConnection(DriverManager.getConnection(url));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            Statement stmt = da.getConnection().createStatement();
            stmt.execute("create schema stg ");
            stmt.executeUpdate(createPersons);
            stmt.executeUpdate(createTeacher);
        } catch ( ProcessException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateSelect() throws SQLException {
            assertEquals("VALID", da.validateQuery("SELECT * FROM PERSONS"));
            assertNotEquals("VALID", da.validateQuery("SELECT * FROM xyz.PERSONS"));
    }

    @Test
    public void testGetSelectMeta() throws  SQLException {
            Assert.assertEquals(5, da.getQueryMeta("SELECT id AS personiddd,name, 123 as coll, 321 as col2, 'dfhdf' col3 FROM PERSONS").getColumnCount());
            //assertEquals("id", da.getQueryMeta(conn,"SELECT id AS personiddd,name, 123 as coll, 321 as col2, 'dfhdf' col3 FROM PERSONS").getColumnName(1).toLowerCase());
            Assert.assertEquals("personiddd", da.getQueryMeta("SELECT id AS personiddd,name, 123 as coll, 321 as col2, 'dfhdf' col3 FROM PERSONS").getColumnLabel(1).toLowerCase());
            //Assert.assertEquals("teachers", da.getQueryMeta(conn,"SELECT s.id AS personiddd,s.name, 123 as coll, 321 as col2, 'dfhdf' col3 FROM PERSONS s " +
            //        "left join teachers p2 on s.id=p2.id" ).getTableName(2).toLowerCase());
            // assertEquals("id", da.getQueryMeta(conn,"SELECT id AS personiddd,name, id-123 as coll, 321 as col2, 'dfhdf' col3 FROM PERSONS").getTableName(3).toLowerCase());

            ResultSetMetaData a = da.getQueryMeta("SELECT s.id AS personiddd, p2.name AS teac_name,p2.name||s.name AS teac_name33, s.name, 123 as coll, 321 as col2, 'dfhdf' col3 FROM PERSONS s " +
                    "left join stg.teachers p2 on s.id=p2.id");

            ResultSetMetaData rsMetaData = a;
            int numberOfColumns = rsMetaData.getColumnCount();
            System.out.println("resultSet MetaData column Count=" + numberOfColumns);
            for (int i = 1; i <= numberOfColumns; i++) {
                //System.out.println("column MetaData ");
                System.out.print(i + " - ");
                // gets the designated column's suggested title
                // for use in printouts and displays.
                System.out.print(rsMetaData.getColumnLabel(i) + "-");
                // get the designated column's name.
                System.out.print(rsMetaData.getColumnName(i) + "-");
                // get the designated column's table name.
                System.out.print(rsMetaData.getTableName(i) + "-");
                // Gets the designated column's table's catalog name.
                System.out.print(rsMetaData.getCatalogName(i) + "-");
                System.out.print(rsMetaData.getCatalogName(i) + "-");
                System.out.print(rsMetaData.getSchemaName(i) + "-");
                // Gets the designated column's table's schema name.
                System.out.println(rsMetaData.getSchemaName(i));
            }
    }

    @Test
    public void testGetDatabaseSchemas() throws  SQLException {
            ArrayList<String> databaseSchemas = da.getDatabaseSchemas();
            assertEquals(true, databaseSchemas.contains("stg"));
    }

    @Test
    public void testGetDatabaseInformation() throws  SQLException {
            System.out.println(da.getName());
            Database dbmeta = da.getDatabaseInformation();
            System.out.println(dbmeta.getSchemaList().keySet().toString());
            assertEquals(true, dbmeta.getSchemaList().containsKey("stg"));
            assertEquals(true, dbmeta.getSchemaList().get("stg").getTableList().containsKey("teachers"));
            assertEquals(true, dbmeta.getSchemaList().get("stg").getTableList().get("teachers").getColumnList().containsKey("name"));
            assertEquals(true, dbmeta.getSchemaList().get("stg").getTableList().get("persons").getColumnList().containsKey("id"));
        }

}