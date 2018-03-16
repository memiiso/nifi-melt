/*
 * Copyright (c) 2018. Memiiso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.memiiso.nifi.melt.web.api.standard;


import org.apache.nifi.web.ComponentDetails;
import org.apache.nifi.web.HttpServletConfigurationRequestContext;
import org.apache.nifi.web.HttpServletRequestContext;
import org.apache.nifi.web.NiFiWebConfigurationContext;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class TestProcessorWebUtils extends AbstractStandardResource {

    @Test
    public void testGetComponentDetailsForProcessor(){
        HttpServletRequest request = mock(HttpServletRequest.class);
        NiFiWebConfigurationContext configurationContext = mock(NiFiWebConfigurationContext.class);
        when(configurationContext.getComponentDetails(any(HttpServletRequestContext.class))).thenReturn(new ComponentDetails.Builder().build());
        ComponentDetails componentDetails = this.getComponentDetails(configurationContext,"1",request);
        assertNotNull(componentDetails);

    }

    @Test
    public void testGetComponentDetailsForProcessorWithSpecificClientRevision(){
        NiFiWebConfigurationContext configurationContext = mock(NiFiWebConfigurationContext.class);
        when(configurationContext.getComponentDetails(any(HttpServletConfigurationRequestContext.class))).thenReturn(new ComponentDetails.Builder().build());
        ComponentDetails componentDetails = this.getComponentDetails(configurationContext,"1",1L, "client1",mock(HttpServletRequest.class));
        assertNotNull(componentDetails);
    }

    @Test
    public void testApplyCacheControl(){
        Response.ResponseBuilder response = mock(Response.ResponseBuilder.class);
        this.applyCacheControl(response);
        verify(response).cacheControl(any(CacheControl.class));
    }

    /*
    @Test
    @SuppressWarnings("unchecked")
    public void testGetRequestContextForProcessor() throws NoSuchMethodException, IOException,InvocationTargetException, IllegalAccessException{
        Method method = this.getDeclaredMethod("getRequestContext", String.class, HttpServletRequest.class);
        method.setAccessible(true);
        NiFiWebRequestContext requestContext = (NiFiWebRequestContext) method.invoke(null,"1",mock(HttpServletRequest.class));
        assertTrue(requestContext instanceof HttpServletRequestContext);
        assertTrue(requestContext.getId().equals("1"));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetRequestContextForProcessorWithSpecificClientRevision() throws NoSuchMethodException, IOException,InvocationTargetException, IllegalAccessException{
        Method method = this.getDeclaredMethod("getRequestContext", String.class, Long.class, String.class, HttpServletRequest.class);
        method.setAccessible(true);
        NiFiWebRequestContext requestContext = (NiFiWebRequestContext) method.invoke(null,"1",1L, "client1",mock(HttpServletRequest.class));
        assertTrue(requestContext instanceof HttpServletConfigurationRequestContext);
        assertTrue(requestContext.getId().equals("1"));
        assertTrue(((HttpServletConfigurationRequestContext)requestContext).getRevision().getClientId().equals("client1"));
        assertTrue(((HttpServletConfigurationRequestContext)requestContext).getRevision().getVersion().equals(1L));
    }
*/

}
