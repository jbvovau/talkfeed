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
package talkfeed.command;

import java.util.Date;
import java.util.Map;

import javax.jdo.PersistenceManager;

import talkfeed.BlogManager;
import talkfeed.cache.SubscriptionCache;
import talkfeed.data.Blog;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.xmpp.TalkService;

/**
 * Command for adding a web source to user. Check is website exists, and add subscription
 * @author JBVovau
 *
 */
@CommandType("add")
public class CommandAddSource implements Command {

	@Override
	public void execute(Map<String, String> args) {
		
		//Retrieve user id
		String id = args.get("id");
		String link = args.get("link");
		
		BlogManager blogManager = BlogManager.getInstance();
		DataManager dataManager = DataManagerFactory.getInstance();
		PersistenceManager pm = dataManager.newPersistenceManager();
		
		//get user
		User user = dataManager.getUserFromId(pm , id);
		
		//check if blog exists
		Blog blog = blogManager.getOrCreateSource(link);
		
		if (blog == null){
			//blog not found or not avaiable
			TalkService.sendMessage(user.getId(),"blog not found ! :(");
			return;
		}
		
		//check subscription
		Subscription sub = dataManager.getSubscription(pm , user, blog);
		
		//create new subscription
		if (sub == null){
			sub = new Subscription();
			sub.setBlogKey(blog.getKey());
			sub.setPriority(0);
			sub.setUserKey(user.getKey());
			sub.setLastProcessDate(new Date());
			sub.setLatestEntryNotifiedDate(new Date());
			
			pm.currentTransaction().begin();
			pm.makePersistent(sub);
			pm.currentTransaction().commit();

			TalkService.sendMessage(user.getId(),"source added ! :)");
			//update cache
			SubscriptionCache.removeUserFromCache(user.getId());
		} else {
			TalkService.sendMessage(user.getId(),"already subscribed");
		}
		
		pm.close();
		pm = null;
		
	}

}
