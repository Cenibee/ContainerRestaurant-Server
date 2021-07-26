package container.restaurant.server.domain.comment;

import container.restaurant.server.domain.feed.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from TB_COMMENT c join fetch c.replies where c.feed.id=:feedId")
    List<Comment> findFeedComments(Long feedId);

    List<Comment> findAllByFeed(Feed feed);
    List<Comment> findAllByUpperReplyId(Long id);
}
