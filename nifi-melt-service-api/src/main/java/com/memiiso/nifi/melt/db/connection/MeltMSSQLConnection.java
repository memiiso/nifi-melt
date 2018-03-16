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


import java.sql.Connection;

/**
 * A objects adapter that generates MS SQL Compatible SQL.
 */
public class MeltMSSQLConnection extends MeltGenericConnection {

    public MeltMSSQLConnection(Connection c) {
        super(c);
    }

    @Override
    public String getName() {
        return "MS SQL 2012+";
    }

    @Override
    public String getDescription() {
        return "Generates MS SQL Compatible SQL, for version 2012 or greater";
    }

}
