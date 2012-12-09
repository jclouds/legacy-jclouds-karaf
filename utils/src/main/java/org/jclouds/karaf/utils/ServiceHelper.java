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

package org.jclouds.karaf.utils;

import com.google.common.base.Strings;
import org.jclouds.Context;
import org.jclouds.View;
import org.jclouds.compute.ComputeService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class ServiceHelper {

    private ServiceHelper() {
        //Utility Class
    }

    /**
     * Returns the cache keys for a given Service.
     * @param service
     * @return
     */
    public static <T> List<String> findCacheKeysForService(T service) {
        List<String> keys = new LinkedList<String>();
        String contextName = toName(service);
        String providerOrApi = toId(service);
        if (contextName != null) {
            keys.add(contextName);
        }
        if (providerOrApi != null) {
            keys.add(providerOrApi);
        }
        return keys;
    }

    /**
     * Chooses a {@link ComputeService} that matches the specified a service id or a provider / api.
     * @param id
     * @param providerOrApi
     * @param services
     * @return
     */
    public static <T> T getService(String id, String providerOrApi, List<T> services) {
        if (!Strings.isNullOrEmpty(id)) {
            T service = null;
            for (T svc : services) {
                if (id.equals(toName(svc))) {
                    service = svc;
                    break;
                }
            }
            if (service == null) {
                throw new IllegalArgumentException("No service with id" + id + " found.");
            }
            return service;
        }

        if (!Strings.isNullOrEmpty(providerOrApi)) {
            T service = null;
            for (T svc : services) {
                if (providerOrApi.equals(toId(svc))) {
                    service = svc;
                    break;
                }
            }
            if (service == null) {
                throw new IllegalArgumentException("No Provider or Api named " + providerOrApi + " found.");
            }
            return service;
        } else {
            if (services.size() == 0) {
                throw new IllegalArgumentException("No providers are present.  Note: It takes a couple of seconds for the provider to initialize.");
            }
            else if (services.size() != 1) {
                StringBuilder sb = new StringBuilder();
                for (T svc : services) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(toId(svc));
                }
                throw new IllegalArgumentException("Multiple providers/apis are present, please select one using the --provider/--api argument in the following values: " + sb.toString());
            } else {
                return services.get(0);
            }
        }
    }

    /**
     * Extracts the Id of the Service.
     * @param service
     * @param <T>
     * @return
     */
    public static <T> String toId(T service) {
        String id = null;
        Context context = toContext(service);
        if (View.class.isAssignableFrom(context.getClass())) {
            id = ((View)context).unwrap().getId();
        } else {
            id = context.getId();
        }
        return id;
    }

    /**
     * Extracts the Name of the Service.
     * @param service
     * @param <T>
     * @return
     */
    public static <T> String toName(T service) {
        String id = null;
        Context context = toContext(service);
        id = context.getName();
        return id;
    }

    /**
     * Extracts the {@link Context}.
     * @param service
     * @param <T>
     * @return
     */
    public static <T> Context toContext(T service) {
        Context ctx = null;
        Class c = service.getClass();
        try {
            //Ugly way to get the Context, but there doesn't seem to be a better one.
            Method m = c.getMethod("getContext");
            Object obj = m.invoke(service);
            if (Context.class.isAssignableFrom(obj.getClass())) {
                ctx = (Context) obj;
            } else if (View.class.isAssignableFrom(obj.getClass())) {
                ctx = ((View)obj).unwrap();
            } else {
                throw new IllegalArgumentException("Service doesn't have a context.");
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        return ctx;
    }
}
