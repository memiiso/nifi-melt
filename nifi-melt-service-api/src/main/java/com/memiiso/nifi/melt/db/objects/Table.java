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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class Table {

    @JsonProperty("name")
    private String tableName;

    private ConcurrentHashMap<String,Column> columnList;

    public Table(String tableName) {
        this.tableName = tableName;
        this.columnList = new ConcurrentHashMap<String,Column>();
    }

    public String getTableName() {
        return tableName;
    }

    public ConcurrentHashMap <String,Column> getColumnList(){return columnList;}

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void addColumn(Column column) {
        this.columnList.put(column.getColumnName(),column);
    }

    @JsonGetter("columns")
    public ArrayList getColumnsAsList() {
        return new ArrayList<>(columnList.keySet());
    }
}
