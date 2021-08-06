package container.restaurant.server.utils.jwt.jjwt;

import container.restaurant.server.config.auth.user.CustomOAuth2User;
import container.restaurant.server.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JjwtLoginService 단위 테스트")
class JjwtLoginServiceTest {

    JjwtLoginService service = new JjwtLoginService();

    @Test
    @DisplayName("인증 받은 유저의 토큰 처리 테스트")
    void 인증_받은_유저의_토큰_처리_테스트() {
        //given
        Map<String, Object> attrs = Map.of(
                "id", "testId",
                "kakao_account", Map.of(
                        "email", "testEmail"));

        CustomOAuth2User givenUser = CustomOAuth2User.newUser(
                "kakao", "id", attrs);

        //when
        String token = service.tokenize(givenUser);
        System.out.println("token = " + token);
        OAuth2User result = service.parse(token);

        //then
        assertThat(result).isEqualTo(givenUser);
    }

    @Test
    @DisplayName("만료 기한이 지난 토큰 테스트")
    void 만료_기한이_지난_토큰_테스트() {
        //given
        String token = Jwts.builder()
                .setExpiration(new Date(new Date().getTime() - 1000))
                .signWith(JjwtLoginService.KEY)
                .compact();

        //expect
        assertThatThrownBy(() -> service.parse(token))
                .isExactlyInstanceOf(UnauthorizedException.class)
                .hasMessage("만료된 토큰입니다.");
    }

}