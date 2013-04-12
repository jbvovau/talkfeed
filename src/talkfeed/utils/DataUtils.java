/*
 Copyright 2010-2012 - Jean-Baptiste Vovau

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
package talkfeed.utils;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.data.Blog;
import talkfeed.data.User;

/**
 * 
 * @author JBVovau
 *
 */
public class DataUtils {

	private DataUtils(){}
	
	/**
	 * Get a stored Blog from his link (could be direct url or RSS url)
	 */
	@SuppressWarnings("unchecked")
	public static Blog getBlogFromLink(PersistenceManager pm , String link){
		
		Blog blog = null;
		
		//try direct link
		Query q = pm.newQuery(Blog.class);
		q.setFilter("link == '" + link +"'");
		
		List<Blog> blogs = (List<Blog>) q.execute();
		
		if (blogs.size() > 0){
			blog = blogs.get(0);
		}
		
		//if blog not found, fetch blog by rss
		if (blog == null){
			q = pm.newQuery(Blog.class);
			q.setFilter("rss == '" + link +"'");
			
			blogs = (List<Blog>) q.execute();
			if (blogs.size() > 0){
				blog = blogs.get(0);
			}
		}
		q.closeAll();
		
		return blog;
	}
	
	
	public static User getUserFromId(PersistenceManager pm , String id){
		User user = null;

		Query q = pm.newQuery(User.class);
		q.setFilter("id == jid");
		q.declareParameters("java.lang.String jid");
		
		@SuppressWarnings("unchecked")
		List<User> list = (List<User>) q.execute(id);
		
		if (list.size() > 0){
			user = list.get(0);
		}
		
		q.closeAll();

		return user;
	}
	
}
