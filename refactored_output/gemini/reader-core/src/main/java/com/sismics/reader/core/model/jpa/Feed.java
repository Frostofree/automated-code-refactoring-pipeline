package com.sismics.reader.core.model.jpa;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.hashCodeverifier.HashCodeVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeedTest {

    @Test
    void equals_hashCode_verify() {
        EqualsVerifier.forClass(Feed.class).verify();
        HashCodeVerifier.forClass(Feed.class).verify();
    }

    @Test
    void getters_setters_work() {
        Feed feed = new Feed("feed-id");
        feed.setRssUrl("https://some-feed-url.com");
        feed.setUrl("https://some-website-url.com");
        feed.setBaseUri("https://some-base-uri.com");
        feed.setTitle("Feed Title");
        feed.setLanguage("en-US");
        feed.setDescription("Feed Description");
        feed.setCreateDate(new Date());
        feed.setLastFetchDate(new Date());
        feed.setDeleteDate(new Date());

        assertThat(feed.getId()).isEqualTo("feed-id");
        assertThat(feed.getRssUrl()).isEqualTo("https://some-feed-url.com");
        assertThat(feed.getUrl()).isEqualTo("https://some-website-url.com");
        assertThat(feed.getBaseUri()).isEqualTo("https://some-base-uri.com");
        assertThat(feed.getTitle()).isEqualTo("Feed Title");
        assertThat(feed.getLanguage()).isEqualTo("en-US");
        assertThat(feed.getDescription()).isEqualTo("Feed Description");
        assertThat(feed.getCreateDate()).isNotNull();
        assertThat(feed.getLastFetchDate()).isNotNull();
        assertThat(feed.getDeleteDate()).isNotNull();
    }
}