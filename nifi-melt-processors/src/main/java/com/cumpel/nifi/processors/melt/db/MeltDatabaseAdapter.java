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
package com.cumpel.nifi.processors.melt.db;

import java.sql.*;
import java.util.ArrayList;


/**
 * Interface for RDBMS/JDBC-specific code.
 */
public interface MeltDatabaseAdapter {

    String getName();

    @FunctionalInterface
    public interface Extractor<T> {
        T extract(ResultSet rs) throws SQLException;
    }

        public default String getCTASStatement (String selectStatement, String tableName){
            return "CREATE TABLE " + tableName + " AS " + System.lineSeparator() + selectStatement;
        }

        public default String validateSelect (Connection conn, String selectStatement){
            try {
                Statement stmt = conn.createStatement();
                stmt.setFetchSize(1);
                stmt.setMaxRows(1);
                ResultSet rs = stmt.executeQuery(selectStatement + System.lineSeparator() + " LIMIT 0 ");
                return "VALID";
            } catch (Exception e) {
                return e.getMessage();
            }
        }

        public default ResultSetMetaData getSelectMeta (Connection conn, String selectStatement) throws SQLException {

            Statement stmt = conn.createStatement();
            stmt.setFetchSize(1);
            stmt.setMaxRows(1);
            ResultSet rs = stmt.executeQuery(selectStatement + System.lineSeparator());
            ResultSetMetaData m = rs.getMetaData();
            return m;
        }

        public default ArrayList<String> getDatabaseSchemas (Connection conn) throws SQLException {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet schemas = meta.getSchemas();
            ArrayList<String> dbschemas = new ArrayList<String>();
            while (schemas.next()) {
                dbschemas.add(schemas.getString("TABLE_SCHEM"));
            }
            return dbschemas;
        }

    }