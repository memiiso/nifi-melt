/*
 * Copyright (c) 2018. Memiiso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.memiiso.nifi.melt.web.api.processors;

import com.memiiso.nifi.melt.web.api.standard.TestProcessorResource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;

import static org.mockito.Mockito.mock;

public class GetDatabaseInformationTest extends JerseyTest {

    public static final ServletContext servletContext = mock(ServletContext.class);

    @Override
    protected Application configure() {
        final ResourceConfig config = new ResourceConfig();
        config.register(CTAS.class);
        config.register(JacksonFeature.class);
        config.register(new AbstractBinder(){
            @Override
            public void configure() {
                bindFactory(TestProcessorResource.MockServletContext.class).to(ServletContext.class);
            }
        });
        return config;
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new InMemoryTestContainerFactory();
    }
/*
    @Test
    public void testGetDatabaseInformation() throws SQLException {

        NiFiWebConfigurationContext webConfigurationContext = (NiFiWebConfigurationContext) servletContext.getAttribute("nifi-web-configuration-context");
        Object a = webConfigurationContext.getControllerService();
        MeltDBConnection dbc = a.getMeltDBConnection();
        Database databaseDTO = dbc.getDatabaseInformation();
        assertNotNull(databaseDTO);
        System.out.println(JsonUtils.toPrettyJsonString(databaseDTO));
    }*/

}