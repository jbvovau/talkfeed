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

package talkfeed;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.data.Blog;
import talkfeed.data.BlogEntry;
import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.url.UrlShorten;
import talkfeed.url.UrlShortenFactory;
import talkfeed.utils.CacheService;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Presence;

public class SubscriptionService {

	// caching
	private final Map<Long, Date> blogToDate = new HashMap<Long, Date>();

	private UrlShorten urlShorten;

	/**
	 * Remove a subscription
	 * 
	 * @param id
	 * @return
	 */
	public boolean removeSubscription(long id) {

		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();

		Subscription s = pm.getObjectById(Subscription.class, new Long(id));
		pm.currentTransaction().begin();
		pm.deletePersistent(s);
		pm.currentTransaction().commit();
		pm.flush();

		pm.close();

		return true;
	}

	/**
	 * Remove subscription
	 * 
	 * @param email
	 * @param blogId
	 * @return
	 */
	public boolean removeSubscription(String email, long blogId) {
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();

		Query qUser = pm.newQuery(User.class);
		qUser.setFilter("id == email");
		qUser.declareParameters("String email");
		qUser.setRange(0, 1);
		qUser.setUnique(true);

		User user = (User) qUser.execute(email);

		if (user == null) {
			qUser.closeAll();
			return false;
		}

		Blog blog = pm.getObjectById(Blog.class, new Long(blogId));

		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userKey == uid && blogKey == bid");
		q.declareParameters("com.google.appengine.api.datastore.Key uid, com.google.appengine.api.datastore.Key bid");
		q.setRange(0, 1);

		@SuppressWarnings("unchecked")
		List<Subscription> list = (List<Subscription>) q.execute(user.getKey(),
				blog.getKey());

		if (list.size() > 0) {
			pm.currentTransaction().begin();
			pm.deletePersistent(list.get(0));
			pm.currentTransaction().commit();
			pm.flush();

		}
		q.closeAll();
		pm.close();

		return true;
	}

	/**
	 * Get url shorten
	 * 
	 * @return
	 */
	@Deprecated
	private UrlShorten getUrlShorten() {
		if (urlShorten == null)
			this.urlShorten = UrlShortenFactory.getInstance();
		return this.urlShorten;
	}

	/**
	 * Result of notification
	 * 
	 * @author vovau
	 * 
	 */
	public class NotificationResult {

	}

}
