package container.restaurant.server.domain.comment;

import container.restaurant.server.domain.feed.Category;
import container.restaurant.server.domain.feed.Feed;
import container.restaurant.server.domain.feed.container.Container;
import container.restaurant.server.domain.restaurant.Restaurant;
import container.restaurant.server.domain.restaurant.menu.Menu;
import container.restaurant.server.domain.user.AuthProvider;
import container.restaurant.server.domain.user.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@DataJpaTest
public class CommentRepositoryTest2 {

    @Autowired CommentRepository commentRepository;
    @Autowired
    EntityManagerFactory emf;

    @Autowired TestEntityManager em;

    @Test
    void test() {
        //given
        User owner = User.builder()
                .authId("authID").authProvider(AuthProvider.KAKAO)
                .nickname("nickname").email("e@mail.com").build();
        em.persist(owner);

        Restaurant restaurant = Restaurant.builder()
                .name("restaurant").addr("address")
                .lat(36.5).lon(36.5).build();
        em.persist(restaurant);

        Feed feed = Feed.builder()
                .owner(owner).restaurant(restaurant).difficulty(3)
                .category(Category.KOREAN).build();
        em.persist(feed);

//        emContainer.of(feed, Menu.mainOf(restaurant, "menu"), "desc");

        Comment comment = Comment.builder()
                .owner(owner).feed(feed).content("test").build();
        em.persist(comment);

        Comment reply = Comment.builder()
                .owner(owner).feed(feed)
                .content("testReply").build();
        reply.isBelongTo(comment);
        System.out.println("reply.getId()1 = " + reply.getId());
        commentRepository.findById(null);

        em.flush();
        em.clear();
        System.out.println("===플러쉬===");

        Comment find = em.find(Comment.class, comment.getId());
        Comment reply2 = Comment.builder()
                .owner(owner).feed(feed)
                .content("testReply2").build();
        reply2.isBelongTo(find);
        System.out.println(emf.getPersistenceUnitUtil().isLoaded(find.getReplies()));
        System.out.println("find.getReplies() = " + find.getReplies());
        System.out.println(emf.getPersistenceUnitUtil().isLoaded(find.getReplies()));

        em.flush();
        System.out.println("===여기까지===");


        System.out.println("reply.getId()2 = " + reply.getId());

        Object count = em.getEntityManager().createQuery("select count(c) from TB_COMMENT c").getSingleResult();
        System.out.println("count = " + count);
        Comment c = em.find(Comment.class, comment.getId());
        List<Comment> list = commentRepository.findAll();
        list.forEach(e -> {
            System.out.println("e.getId() = " + e.getId());
            System.out.println("e.getUpperReply() = " + e.getUpperReply());
        });

    }


    @Disabled
    @Test
    @DisplayName("피드 ID 로 조회하기")
    void findAllByFeedId() {
        //given
        User owner = User.builder()
                .authId("authID").authProvider(AuthProvider.KAKAO)
                .nickname("nickname").email("e@mail.com").build();
        em.persist(owner);

        Restaurant restaurant = Restaurant.builder()
                .name("restaurant").addr("address")
                .lat(36.5).lon(36.5).build();
        em.persist(restaurant);

        // TODO 피드 안의 용기에 피드가 셋되어있지 않아도 CASCADE 로 저장하면 피드 외래키가 설정되나?
        Feed feed1 = Feed.builder()
                .owner(owner).restaurant(restaurant).difficulty(3)
                .menus(List.of(
                        Container.of(null, Menu.mainOf(restaurant, "menu"), "desc")))
                .category(Category.KOREAN).build();
        em.persist(feed1);

        //when

        //then
    }

}
