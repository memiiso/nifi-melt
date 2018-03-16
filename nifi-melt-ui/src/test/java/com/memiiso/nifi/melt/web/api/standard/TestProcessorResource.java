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

import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.nifi.web.*;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class TestProcessorResource extends JerseyTest {

    public static final ServletContext servletContext = mock(ServletContext.class);
    public static final HttpServletRequest requestContext = mock(HttpServletRequest.class);

    @Override
    protected Application configure() {
        final ResourceConfig config = new ResourceConfig();
        config.register(StandardMeltProcessor.class);
        config.register(JacksonFeature.class);
        config.register(new AbstractBinder(){
            @Override
            public void configure() {
                bindFactory(MockRequestContext.class).to(HttpServletRequest.class);
            }
        });
        config.register(new AbstractBinder(){
            @Override
            public void configure() {
                bindFactory(MockServletContext.class).to(ServletContext.class);
            }
        });
        return config;
    }

    @Override
    public TestContainerFactory getTestContainerFactory() {
        return new InMemoryTestContainerFactory();
    }


    @Test
    public void testSetProperties() {

        final NiFiWebConfigurationContext niFiWebConfigurationContext = mock(NiFiWebConfigurationContext.class);
        final Map<String,String> properties = new HashMap<>();
        properties.put("jolt-transform","jolt-transform-chain");
        final ComponentDetails componentDetails = new ComponentDetails.Builder().properties(properties).build();

        Mockito.when(servletContext.getAttribute(Mockito.anyString())).thenReturn(niFiWebConfigurationContext);
        Mockito.when(niFiWebConfigurationContext.updateComponent(any(NiFiWebConfigurationRequestContext.class),any(String.class),any(Map.class))).thenReturn(componentDetails);

        Response response = client().target(getBaseUri())
                .path("/standard/standard/properties")
                .queryParam("processorId","1")
                .queryParam("clientId","1")
                .queryParam("revisionId","1")
                .request()
                .put(Entity.json(JsonUtils.toJsonString(properties)));

        assertNotNull(response);
        JsonNode jsonNode = response.readEntity(JsonNode.class);
        assertNotNull(jsonNode);
        assertTrue(jsonNode.get("properties").get("jolt-transform").asText().equals("jolt-transform-chain"));
    }


    @Test
    public void testGetProcessorDetails() {
        final NiFiWebConfigurationContext niFiWebConfigurationContext = mock(NiFiWebConfigurationContext.class);
        final Map<String,String> allowableValues = new HashMap<>();
        final ComponentDescriptor descriptor = new ComponentDescriptor.Builder().name("test-name").allowableValues(allowableValues).build();
        final Map<String,ComponentDescriptor> descriptors = new HashMap<>();
        descriptors.put("jolt-transform",descriptor);
        final ComponentDetails componentDetails = new ComponentDetails.Builder().name("mytransform").type("org.apache.nifi.processors.standard.JoltTransformJSON")
                .descriptors(descriptors)
                .build();

        Mockito.when(servletContext.getAttribute(Mockito.anyString())).thenReturn(niFiWebConfigurationContext);
        Mockito.when(niFiWebConfigurationContext.getComponentDetails(any(NiFiWebRequestContext.class))).thenReturn(componentDetails);

        JsonNode value = client().target(getBaseUri())
                .path("/standard/standard/details")
                .queryParam("processorId","1")
                .request()
                .get(JsonNode.class);

        assertNotNull(value);

        try{
            assertTrue(value.get("name").asText().equals("mytransform"));
        } catch (Exception e){
            fail("Failed due to: " + e.toString());
        }

    }

    public static class MockRequestContext implements Factory<HttpServletRequest> {
        @Override
        public HttpServletRequest provide() {
            return requestContext;
        }

        @Override
        public void dispose(HttpServletRequest t) {
        }
    }

    public static class MockServletContext implements Factory<ServletContext> {
        @Override
        public ServletContext provide() {
            return servletContext;
        }

        @Override
        public void dispose(ServletContext t) {
        }
    }

}
