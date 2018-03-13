package com.cumpel.nifi.melt.web.api.processor;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import org.apache.nifi.web.ComponentDetails;
import org.apache.nifi.web.HttpServletConfigurationRequestContext;
import org.apache.nifi.web.HttpServletRequestContext;
import org.apache.nifi.web.NiFiWebConfigurationContext;
import org.apache.nifi.web.NiFiWebRequestContext;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TestProcessorWebUtils {

    @Test
    public void testGetComponentDetailsForProcessor(){
        HttpServletRequest request = mock(HttpServletRequest.class);
        NiFiWebConfigurationContext configurationContext = mock(NiFiWebConfigurationContext.class);
        when(configurationContext.getComponentDetails(any(HttpServletRequestContext.class))).thenReturn(new ComponentDetails.Builder().build());
        ComponentDetails componentDetails = ProcessorWebUtils.getComponentDetails(configurationContext,"1",request);
        assertNotNull(componentDetails);

    }

    @Test
    public void testGetComponentDetailsForProcessorWithSpecificClientRevision(){
        NiFiWebConfigurationContext configurationContext = mock(NiFiWebConfigurationContext.class);
        when(configurationContext.getComponentDetails(any(HttpServletConfigurationRequestContext.class))).thenReturn(new ComponentDetails.Builder().build());
        ComponentDetails componentDetails = ProcessorWebUtils.getComponentDetails(configurationContext,"1",1L, "client1",mock(HttpServletRequest.class));
        assertNotNull(componentDetails);
    }

    @Test
    public void testApplyCacheControl(){
        Response.ResponseBuilder response = mock(Response.ResponseBuilder.class);
        ProcessorWebUtils.applyCacheControl(response);
        verify(response).cacheControl(any(CacheControl.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetRequestContextForProcessor() throws NoSuchMethodException, IOException,InvocationTargetException, IllegalAccessException{
        Method method = ProcessorWebUtils.class.getDeclaredMethod("getRequestContext", String.class, HttpServletRequest.class);
        method.setAccessible(true);
        NiFiWebRequestContext requestContext = (NiFiWebRequestContext) method.invoke(null,"1",mock(HttpServletRequest.class));
        assertTrue(requestContext instanceof HttpServletRequestContext);
        assertTrue(requestContext.getId().equals("1"));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetRequestContextForProcessorWithSpecificClientRevision() throws NoSuchMethodException, IOException,InvocationTargetException, IllegalAccessException{
        Method method = ProcessorWebUtils.class.getDeclaredMethod("getRequestContext", String.class, Long.class, String.class, HttpServletRequest.class);
        method.setAccessible(true);
        NiFiWebRequestContext requestContext = (NiFiWebRequestContext) method.invoke(null,"1",1L, "client1",mock(HttpServletRequest.class));
        assertTrue(requestContext instanceof HttpServletConfigurationRequestContext);
        assertTrue(requestContext.getId().equals("1"));
        assertTrue(((HttpServletConfigurationRequestContext)requestContext).getRevision().getClientId().equals("client1"));
        assertTrue(((HttpServletConfigurationRequestContext)requestContext).getRevision().getVersion().equals(1L));
    }


}
