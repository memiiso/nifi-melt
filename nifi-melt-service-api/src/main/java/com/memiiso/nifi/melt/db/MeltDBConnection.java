
/*
 * Copyright (c) 2018. Memiiso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.memiiso.nifi.melt.db;

import com.memiiso.nifi.melt.db.objects.Database;
import com.memiiso.nifi.melt.db.objects.Database;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

public interface MeltDBConnection extends AutoCloseable {

    Connection getConnection();

    String getName();

    String getDescription();

    String getCTASStatement(String selectStatement, String tableName);

    String validateQuery(String statement);

    ResultSetMetaData getQueryMeta(String statement) throws SQLException;

    String getQueryMetaJson(String statement, String targetTableName) throws SQLException;

    ArrayList<String> getDatabaseSchemas() throws SQLException;

    Database getDatabaseInformation() throws SQLException;

    @Override
    void close() throws SQLException;
}