package com.cumpel.nifi.melt.processors.db.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.cumpel.nifi.melt.processors.db.MeltDatabaseAdapter;

public class MeltDerbyDatabaseAdapter implements MeltDatabaseAdapter {

	@Override
	public String getName() {
		return "Derby";
	}

	@Override
	public String getCTASStatement(String selectStatement, String tableName) {
		return "CREATE TABLE " + tableName + " AS " + System.lineSeparator() + selectStatement + " WITH NO DATA "
				;
				//+ System.lineSeparator() + "INSERT INTO " + tableName + " " + System.lineSeparator() + selectStatement;
	}
	
	@Override
	public String validateSelect(Connection conn, String selectStatement) {
		try {
			Statement stmt = conn.createStatement();
			stmt.setFetchSize(1);
			stmt.setMaxRows(1);
			ResultSet rs = stmt.executeQuery(selectStatement+ System.lineSeparator() + " FETCH FIRST ROW ONLY " );
			return "VALID";
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
