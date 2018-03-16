
/*
 * Copyright (c) 2018. Memiiso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.memiiso.nifi.melt.service;

import com.memiiso.nifi.melt.db.MeltDBConnection;
import com.memiiso.nifi.melt.db.connection.*;
import com.memiiso.nifi.melt.dbcp.MeltDBCPService;
import org.apache.nifi.annotation.behavior.DynamicProperty;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.dbcp.DBCPConnectionPool;
import org.apache.nifi.processor.exception.ProcessException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implementation of for Database Connection Pooling Service. Apache DBCP is used for connection pooling functionality.
 *
 */
@Tags({ "dbcp", "jdbc", "database", "connection", "pooling", "store" })
@CapabilityDescription("Provides Database Connection Pooling Service. Connections can be asked from pool and returned after usage.")
@DynamicProperty(name = "JDBC property name", value = "JDBC property value", supportsExpressionLanguage = true,
        description = "Specifies a property name and value to be set on the JDBC connection(s). "
                + "If Expression Language is used, evaluation will be performed upon the controller service being enabled. "
                + "Note that no flow file input (attributes, e.g.) is available for use in Expression Language constructs for these properties.")
public class MeltDBCPConnectionPool extends DBCPConnectionPool implements MeltDBCPService {

    @Override
    public String toString() {
        return "MeltDBCPConnectionPool[id=" + getIdentifier() + "]";
    }

    @Override
    public MeltDBConnection getMeltDBConnection() throws ProcessException {
        try {
            final Connection con = this.getConnection();

            // TODO is this neccessary ?
            if (con == null) {
                throw new ProcessException("Can't load Get Connection!");
            }

            String dbProductName = con.getMetaData().getDatabaseProductName();
            final MeltDBConnection myMeltDatabaseAdapter;

            if (dbProductName.equalsIgnoreCase("MySQL")) {
                myMeltDatabaseAdapter = new MeltGenericConnection(con);
            } else if (dbProductName.equalsIgnoreCase("PostgreSQL")) {
                myMeltDatabaseAdapter = new MeltPostgresqlConnection(con);
            } else if (dbProductName.equalsIgnoreCase("Apache Derby")) {
                myMeltDatabaseAdapter = new MeltDerbyConnection(con);
            } else if (dbProductName.equalsIgnoreCase("Microsoft SQL Server")) {
                myMeltDatabaseAdapter = new MeltMSSQLConnection(con);
            } else if (dbProductName.equalsIgnoreCase("SQLite")){
                myMeltDatabaseAdapter = new MeltSQLliteConnection(con);
            } else {
                myMeltDatabaseAdapter = new MeltGenericConnection(con);
            }
            return myMeltDatabaseAdapter;

        } catch (final SQLException e) {
            throw new ProcessException(e);
        }
    }

}
