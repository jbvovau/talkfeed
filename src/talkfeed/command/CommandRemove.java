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

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.UserManager;
import talkfeed.cache.SubscriptionCache;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.xmpp.TalkService;

import com.google.appengine.api.datastore.Key;

/**
 * Remove last subscription
 * @author Jean-Baptiste Vovau
 *
 */
@CommandType("remove")
public class CommandRemove implements Command {

	@Override
	public void execute(Map<String, String> args) {
		String jid = args.get("id");
		String number = args.get("arg2");
		
		if (jid != null){
			DataManager dm = DataManagerFactory.getInstance();
			PersistenceManager pm = dm.newPersistenceManager();
			
			User u = dm.getUserFromId(pm,jid);
			
			Key subToRemove = null;
			
			//no number : remove last subscription
			if (number == null) subToRemove = u.getLastSubscriptionKey();
			else {
				int nb = 0;
				try {
					nb = Integer.valueOf(number);
				} catch (Exception ex){
					//bad number
					return;
				}
				
				//find sub
				Query q = pm.newQuery(Subscription.class);
				q.setFilter("userKey == k");
				q.declareParameters("com.google.appengine.api.datastore.Key k");
				
				@SuppressWarnings("unchecked")
				List<Subscription> subs = (List<Subscription>) q.execute(u.getKey());
				int i = 1;
				for(Subscription sub : subs){
					if (nb == i) {
						subToRemove = sub.getKey();
						break;
					}
					i++;
				}
			}
			
			if (subToRemove != null){
				UserManager serv = new UserManager();
				serv.removeUserSubscription(subToRemove.getId());
				
				TalkService.sendMessage(jid, "subscription is removed");
				//update cache
				SubscriptionCache.removeUserFromCache(jid);
			}
			pm.close();
		}
		
	}

}
