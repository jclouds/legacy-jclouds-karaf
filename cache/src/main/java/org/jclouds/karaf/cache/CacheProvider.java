/*
 * Copyright (C) 2011, the original authors
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.karaf.cache;

import java.util.Map;

import com.google.common.collect.Multimap;

public interface CacheProvider {

    /**.
     * Returns a {@link Map} of cached values per provider for the specified type.
     * An example that demonstrates the structure:
     *
     * {
     *  "images": {
     *      "aws-ec2": {
     *          ami-3xxxxx,
     *          ami-4xxxxx
     *      },
     *      "cloudservers-us": {
     *          103,
     *          104
     *      }
     *  },
     *  "locations": {
     *    "aws-ec2": {
     *          eu-west1,
     *          us-east-1
     *      }
     *  }
     * }
     *
     * @param provider
     * @return
     */

   Multimap<String,String> getProviderCacheForType(String provider);
}
