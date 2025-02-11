```java
package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.util.jpa.EntityMapper;

public class FeedSubscriptionMapper implements EntityMapper<FeedSubscriptionDto> {

    @Override
    public FeedSubscriptionDto map(Object[] o) {
        FeedSubscriptionDto dto = new FeedSubscriptionDto();
        dto.setId(stringValue(o[0]));
        dto.setUnreadUserArticleCount(intValue(o[1]));
        dto.setCreateDate(dateValue(o[2]));
        dto.setUserId(stringValue(o[3]));
        dto.setFeedId(stringValue(o[4]));
        dto.setFeedRssUrl(stringValue(o[5]));
        dto.setFeedUrl(stringValue(o[6]));
        dto.setFeedDescription(stringValue(o[7]));
        dto.setCategoryId(stringValue(o[8]));
        dto.setSynchronizationFailCount(((Number) o[9]).intValue());

        return dto;
    }
}
```