/*
 Copyright 2010 - Jean-Baptiste Vovau

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package talkfeed.url;

import talkfeed.cache.CacheService;

public abstract class CachedUrlShorten implements UrlShorten {

	/**
	 * Return available data in cache
	 */
	@Override
	public final String shorten(String url) {
		//
		Object shortUrl = CacheService.get(url);
		
		if (shortUrl != null) return shortUrl.toString();
		
		shortUrl = this.directShorten(url);
		
		CacheService.put(url, shortUrl);
		
		return shortUrl.toString();
	}
	
	/** direct call */
	public abstract String directShorten(String url);
	

}
