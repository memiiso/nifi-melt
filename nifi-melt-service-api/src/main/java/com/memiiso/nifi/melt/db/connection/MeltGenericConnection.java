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
import com.memiiso.nifi.melt.db.objects.Column;
import com.memiiso.nifi.melt.db.objects.Database;
import com.memiiso.nifi.melt.db.objects.Schema;
import com.memiiso.nifi.melt.db.objects.Table;
import com.memiiso.nifi.melt.db.MeltDBConnection;
import com.memiiso.nifi.melt.db.objects.Database;
import com.memiiso.nifi.melt.db.objects.Schema;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;

public class MeltGenericConnection implements MeltDBConnection {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public MeltGenericConnection(Connection c) {
        this.connection = c;
    }

    public String getName() {
        return "Generic";
    }

    public String getDescription() {
        return "Generates ANSI SQL";
    }

    public  String getCTASStatement (String selectStatement, String tableName){
        return "CREATE TABLE " + tableName + " AS " + System.lineSeparator() + selectStatement;
    }

    public  String validateQuery(String statement){
        try {
            ResultSetMetaData metaData = this.connection.prepareStatement(statement).getMetaData();
        } catch (SQLException e) {
            return e.getErrorCode() + " : " +  e.getMessage();
        }
        return "VALID";
    }

    public  ResultSetMetaData getQueryMeta(String statement) throws SQLException {
        ResultSetMetaData metaData = this.connection.prepareStatement(statement).getMetaData();
        return metaData;
    }

    // TODO metadata
    //---- target schema,table,column @TODO
    //    ---- source schema,table,column
    public  String getQueryMetaJson(String statement, String targetTableName) throws SQLException {

        ResultSetMetaData metaData = this.getQueryMeta(statement);
        JSONArray json = new JSONArray();
        int numColumns = metaData.getColumnCount();
        for (int i=1; i<numColumns+1; i++) {
            JSONObject obj = new JSONObject();
            obj.appendField("columnIndex",i);
            obj.appendField("target_TableSchemaName",targetTableName);
            obj.appendField("target_TableName","");
            obj.appendField("target_columnName","");

            obj.appendField("source_columnLabel",metaData.getColumnLabel(i));
            obj.appendField("source_columnName",metaData.getColumnName(i));
            obj.appendField("source_columnSchemaName",metaData.getSchemaName(i));
            obj.appendField("source_columnTableName",metaData.getTableName(i));
            json.add(obj);
        }
        return json.toJSONString();
    }

    public ArrayList<String> getDatabaseSchemas () throws SQLException {
        DatabaseMetaData meta = this.connection.getMetaData();
        ResultSet schemas = meta.getSchemas();
        ArrayList<String> dbschemas = new ArrayList<String>();
        while (schemas.next()) {
            dbschemas.add(schemas.getString("TABLE_SCHEM"));
        }
        return dbschemas;
    }

    public Database getDatabaseInformation() throws SQLException {

        //System.out.println("-------");
        Database databaseDTO = new Database();
        DatabaseMetaData metaData = this.connection.getMetaData();

        ResultSet schemas = metaData.getSchemas();
        //ResultSet catalogs = metaData.getCatalogs();

        while (schemas.next()) {
            //System.out.println(catalogs.getString("TABLE_CAT"));
            Schema schema = new Schema( schemas.getString("TABLE_SCHEM"));
            String[] types = { "TABLE", "VIEW" };
            ResultSet tables = metaData.getTables(schema.getSchemaName(), null, "%", types);
            while (tables.next()){
                //System.out.println(tables.getString(3));
                Table table = new Table(tables.getString(3));
                ResultSet columns = metaData.getColumns(schema.getSchemaName(), null, table.getTableName(), "%");
                while (columns.next()){
                    //System.out.println(columns.getString("COLUMN_NAME"));
                    //System.out.println(columns.getString("TYPE_NAME"));
                    Column column = new Column(columns.getString("COLUMN_NAME"),columns.getString("TYPE_NAME"));
                    table.addColumn(column);
                }
                schema.addTable(table);
                //     columns.close();
            }
            databaseDTO.addSchema(schema);
            // tables.close();
        }
        // catalogs.close();
        return databaseDTO;
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
    }

}
