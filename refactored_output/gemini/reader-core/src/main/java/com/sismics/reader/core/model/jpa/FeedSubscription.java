```java
import com.google.common.base.Objects;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity representing a feed subscription from a user to a feed.
 */
@Entity
@Table(name = "T_FEED_SUBSCRIPTION")
public class FeedSubscription {

    public static final int ID_LENGTH = 36;

    /**
     * Subscription ID.
     */
    @Id
    @Column(name = "FES_ID_C", length = ID_LENGTH)
    private String id;

    /**
     * User ID.
     */
    @Column(name = "FES_IDUSER_C", nullable = false, length = ID_LENGTH)
    private String userId;

    /**
     * Feed ID.
     */
    @Column(name = "FES_IDFEED_C", nullable = false, length = ID_LENGTH)
    private String feedId;

    /**
     * Category ID.
     */
    @Column(name = "FES_IDCATEGORY_C", nullable = false, length = ID_LENGTH)
    private String categoryId;

    /**
     * Display order of this feed in the category.
     */
    @Column(name = "FES_ORDER_N", nullable = false)
    private Integer order;

    /**
     * Number of unread articles in this subscription.
     */
    @Column(name = "FES_UNREADCOUNT_N", nullable = false)
    private Integer unreadCount;

    /**
     * Creation date.
     */
    @Column(name = "FES_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "FES_DELETEDATE_D")
    private Date deleteDate;

    // Getters and setters omitted for brevity

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("feedId", feedId)
                .toString();
    }
}
```