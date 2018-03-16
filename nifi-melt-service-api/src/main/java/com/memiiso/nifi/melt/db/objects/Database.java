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

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class Database {
    private ConcurrentHashMap<String, Schema> schemaList;

    public Database() {
        this.schemaList = new ConcurrentHashMap<String, Schema>();
    }

    public ConcurrentHashMap<String, Schema> getSchemaList() {
        return schemaList;
    }

    public void addSchema(Schema schema) {
        this.schemaList.put(schema.getSchemaName(),schema);
    }

    @JsonGetter("schemas")
    public ArrayList getSchemasAsList() {
        return new ArrayList<>(schemaList.keySet());
    }

}
