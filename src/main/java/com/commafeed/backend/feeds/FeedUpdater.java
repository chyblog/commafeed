package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedService;
import com.commafeed.backend.model.Feed;

@Stateless
public class FeedUpdater {

	private static Logger log = LoggerFactory.getLogger(FeedTimer.class);

	@Inject
	FeedFetcher fetcher;

	@Inject
	FeedService feedService;

	@Inject
	FeedEntryService feedEntryService;

	@Asynchronous
	@Lock(LockType.READ)
	@AccessTimeout(value = 15, unit = TimeUnit.SECONDS)
	public void update(Feed feed) {

		try {
			Feed fetchedFeed = fetcher.fetch(feed.getUrl());
			feedEntryService.updateEntries(feed.getUrl(),
					fetchedFeed.getEntries());
		} catch (Exception e) {
			log.info("Unable to refresh feed " + feed.getUrl() + " : "
					+ e.getMessage());
			feed.setLastUpdated(Calendar.getInstance().getTime());
			feed.setMessage("Unable to refresh feed: " + e.getMessage());
			feedService.update(feed);
		}
	}
}
