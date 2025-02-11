```java
package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.FeedIdentityDto;
import com.sismics.reader.core.dao.jpa.dto.FeedUrlDto;
import com.sismics.util.jpa.ResultMapper;

/**
 * @author jtremeaux
 */
public class FeedMapper extends ResultMapper<FeedIdentityDto> {
    @Override
    public FeedIdentityDto map(Object[] o) {
        int i = 0;
        return new FeedIdentityDto(stringValue(o[i++]));
    }
}

public class FeedUrlMapper extends ResultMapper<FeedUrlDto> {
    @Override
    public FeedUrlDto map(Object[] o) {
        int i = 0;
        return new FeedUrlDto(stringValue(o[i]));
    }
}
```