/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.karaf.services;

import com.google.common.base.Optional;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.karaf.services.internal.ComputeServiceFactory;
import org.jclouds.providers.ProviderMetadata;
import org.junit.Test;

import java.util.Properties;

import static org.easymock.EasyMock.*;

public class ServiceFactoryValidationTest {

    @Test
    public void testProviderValidationWithDefaults() throws InvalidConfigurationException {
        ApiMetadata apiMetadata = createMock(ApiMetadata.class);
        ProviderMetadata providerMetadata = createMock(ProviderMetadata.class);
        expect(apiMetadata.getDefaultIdentity()).andReturn(Optional.of("defaultIdentity")).anyTimes();
        expect(apiMetadata.getDefaultCredential()).andReturn(Optional.of("defaultCredential")).anyTimes();
        expect(apiMetadata.getDefaultEndpoint()).andReturn(Optional.of("defaultEndpoint")).anyTimes();
        expect(providerMetadata.getApiMetadata()).andReturn(apiMetadata).anyTimes();
        replay(providerMetadata);
        replay(apiMetadata);
        ComputeServiceFactory.validate(providerMetadata, new Properties());
    }

    @Test
    public void testApiValidationWithDefaults() throws InvalidConfigurationException {
        ApiMetadata apiMetadata = createMock(ApiMetadata.class);
        expect(apiMetadata.getDefaultIdentity()).andReturn(Optional.of("defaultIdentity")).anyTimes();
        expect(apiMetadata.getDefaultCredential()).andReturn(Optional.of("defaultCredential")).anyTimes();
        expect(apiMetadata.getDefaultEndpoint()).andReturn(Optional.of("defaultEndpoint")).anyTimes();
        replay(apiMetadata);
        ComputeServiceFactory.validate(apiMetadata, new Properties());
    }


    @Test(expected = InvalidConfigurationException.class)
    public void testProviderValidationWithNoIdentity() throws InvalidConfigurationException {
        ApiMetadata apiMetadata = createMock(ApiMetadata.class);
        ProviderMetadata providerMetadata = createMock(ProviderMetadata.class);
        expect(apiMetadata.getDefaultIdentity()).andReturn(Optional.<String>absent()).anyTimes();
        expect(apiMetadata.getDefaultCredential()).andReturn(Optional.of("defaultCredential")).anyTimes();
        expect(apiMetadata.getDefaultEndpoint()).andReturn(Optional.of("defaultEndpoint")).anyTimes();
        expect(providerMetadata.getApiMetadata()).andReturn(apiMetadata).anyTimes();
        replay(providerMetadata);
        replay(apiMetadata);
        ComputeServiceFactory.validate(providerMetadata, new Properties());
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testApiValidationWithNoIdentity() throws InvalidConfigurationException {
        ApiMetadata apiMetadata = createMock(ApiMetadata.class);
        expect(apiMetadata.getDefaultIdentity()).andReturn(Optional.<String>absent()).anyTimes();
        expect(apiMetadata.getDefaultCredential()).andReturn(Optional.of("defaultCredential")).anyTimes();
        expect(apiMetadata.getDefaultEndpoint()).andReturn(Optional.of("defaultEndpoint")).anyTimes();
        replay(apiMetadata);
        ComputeServiceFactory.validate(apiMetadata, new Properties());
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testProviderValidationWithNoCredential() throws InvalidConfigurationException {
        ApiMetadata apiMetadata = createMock(ApiMetadata.class);
        ProviderMetadata providerMetadata = createMock(ProviderMetadata.class);
        expect(apiMetadata.getDefaultIdentity()).andReturn(Optional.of("defaultIdentity")).anyTimes();
        expect(apiMetadata.getDefaultCredential()).andReturn(Optional.<String>absent()).anyTimes();
        expect(apiMetadata.getDefaultEndpoint()).andReturn(Optional.of("defaultEndpoint")).anyTimes();
        expect(providerMetadata.getApiMetadata()).andReturn(apiMetadata).anyTimes();
        replay(providerMetadata);
        replay(apiMetadata);
        ComputeServiceFactory.validate(providerMetadata, new Properties());
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testApiValidationWithNoCredential() throws InvalidConfigurationException {
        ApiMetadata apiMetadata = createMock(ApiMetadata.class);
        expect(apiMetadata.getDefaultIdentity()).andReturn(Optional.of("defaultIdentity")).anyTimes();
        expect(apiMetadata.getDefaultCredential()).andReturn(Optional.<String>absent()).anyTimes();
        expect(apiMetadata.getDefaultEndpoint()).andReturn(Optional.of("defaultEndpoint")).anyTimes();
        replay(apiMetadata);
        ComputeServiceFactory.validate(apiMetadata, new Properties());
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testApiValidationWithNoEndpoint() throws InvalidConfigurationException {
        ApiMetadata apiMetadata = createMock(ApiMetadata.class);
        expect(apiMetadata.getDefaultIdentity()).andReturn(Optional.of("defaultIdentity")).anyTimes();
        expect(apiMetadata.getDefaultCredential()).andReturn(Optional.of("defaultCredential")).anyTimes();
        expect(apiMetadata.getDefaultEndpoint()).andReturn(Optional.<String>absent()).anyTimes();
        replay(apiMetadata);
        ComputeServiceFactory.validate(apiMetadata, new Properties());
    }
}
