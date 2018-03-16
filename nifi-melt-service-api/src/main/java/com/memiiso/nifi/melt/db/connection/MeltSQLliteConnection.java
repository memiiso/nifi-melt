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

import com.memiiso.nifi.melt.db.objects.Column;
import com.memiiso.nifi.melt.db.objects.Database;
import com.memiiso.nifi.melt.db.objects.Schema;
import com.memiiso.nifi.melt.db.objects.Table;
import com.memiiso.nifi.melt.db.objects.Database;
import com.memiiso.nifi.melt.db.objects.Schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MeltSQLliteConnection extends MeltGenericConnection {

    public MeltSQLliteConnection(Connection c) {
        super(c);
    }

    public String getName() {
        return "SQLlite";
    }

    public String getDescription() {
        return "Generates SQLlite SQL";
    }

    public Database getDatabaseInformation() throws SQLException {

        Database databaseDTO = new Database();
        DatabaseMetaData metaData = this.getConnection().getMetaData();

        Schema schema = new Schema( "defaultSchema");
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
        schema.setSchemaName("defaultSchema2");
        databaseDTO.addSchema(schema);

        return databaseDTO;
    }

}
