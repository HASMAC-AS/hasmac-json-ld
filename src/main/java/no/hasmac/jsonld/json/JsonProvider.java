/*
 * Copyright 2020 APICATALOG and HASMAC.
 *
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 */

package no.hasmac.jsonld.json;

/**
 * Instead of using the Jakarta Json static class to create builders, it
 * is much faster to keep track of the provider in a static instance.  This
 * avoids the overhead of doing a service lookup on each call.  This improves
 * performance dramatically (about 200 to 300% for most calls).
 */
public class JsonProvider {

	private static jakarta.json.spi.JsonProvider provider;

	private JsonProvider() {}

	public static jakarta.json.spi.JsonProvider instance() {
		if (provider == null) {
			provider = jakarta.json.spi.JsonProvider.provider();
		}
		return provider;
	}
}
