package service;

import com.memiiso.nifi.melt.service.MeltDBCPConnectionPool;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.processor.exception.ProcessException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeltDBCPConnectionPoolTest {

    private com.memiiso.nifi.melt.service.MeltDBCPConnectionPool meltDBCPConnectionPool;
    private BasicDataSource basicDataSource;

    @Before
    public void setup() throws Exception {
        basicDataSource = mock(BasicDataSource.class);
        initPool();
    }

    private void initPool() throws Exception {
        meltDBCPConnectionPool = new com.memiiso.nifi.melt.service.MeltDBCPConnectionPool();

        Field dataSourceField = MeltDBCPConnectionPool.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(meltDBCPConnectionPool, basicDataSource);

        Field componentLogField = AbstractControllerService.class.getDeclaredField("logger");
        componentLogField.setAccessible(true);
    }

    @Test(expected = ProcessException.class)
    public void testGetConnectionSqlException() throws SQLException {
        SQLException sqlException = new SQLException("bad sql");
        when(basicDataSource.getConnection()).thenThrow(sqlException);
        try {
            meltDBCPConnectionPool.getMeltDBConnection().getConnection();
        } catch (ProcessException e) {
            assertEquals(sqlException, e.getCause());
            throw e;
        }
    }

}