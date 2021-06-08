package container.restaurant.server.domain.user;

import container.restaurant.server.domain.feed.picture.Image;
import container.restaurant.server.domain.push.PushToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;


class UserTest {

    @Test
    @DisplayName("빌더 테스트")
    void testBuilder() {
        //given
        String authId = "testId";
        AuthProvider provider = AuthProvider.KAKAO;
        String email = "test@test.com";
        String nickname = "testNickname";
        Image profile = new Image("profilePath");
        PushToken pushToken = new PushToken("testToken");

        //when
        User user = User.builder()
                .authId(authId)
                .authProvider(provider)
                .email(email)
                .nickname(nickname)
                .profile(profile)
                .pushToken(pushToken)
                .build();

        //then
        assertThat(user.getAuthId()).isEqualTo(authId);
        assertThat(user.getAuthProvider()).isEqualTo(provider);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getProfile().getUrl()).isEqualTo(profile.getUrl());
        assertThat(user.getLevel()).isEqualTo(0);
        assertThat(user.getLevelFeedCount()).isEqualTo(0);
        assertThat(user.getFeedCount()).isEqualTo(0);
        assertThat(user.getBanned()).isFalse();
        assertThat(user.getPushToken()).isEqualTo(pushToken);
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("레벨링 테스트")
    void testLeveling(int before, int count, boolean up, int expectedLevel) throws NoSuchFieldException, IllegalAccessException {
        //given
        User user = new User();
        Field f = User.class.getDeclaredField("levelFeedCount");
        f.setAccessible(true);
        f.set(user, before);

        //when
        if (up) user.levelFeedUp(count);
        else user.levelFeedDown(count);

        //then
        assertThat(user.getLevel()).isEqualTo(expectedLevel);
    }

    static Stream<Arguments> testLeveling() {
        return Stream.of(
                arguments(3, 3, false, 0),
                arguments(3, 7, true, 3),
                arguments(25, 6, false, 3),
                arguments(0, 5, true, 2)
        );
    }

}