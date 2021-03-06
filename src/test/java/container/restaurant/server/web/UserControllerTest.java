package container.restaurant.server.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import container.restaurant.server.config.auth.dto.SessionUser;
import container.restaurant.server.domain.exception.ResourceNotFoundException;
import container.restaurant.server.domain.user.User;
import container.restaurant.server.domain.user.UserRepository;
import container.restaurant.server.exceptioin.FailedAuthorizationException;
import container.restaurant.server.web.dto.user.UserUpdateDto;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.ConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockHttpSession session;

    @Autowired
    private ObjectMapper mapper;

    private User myself;
    private User other;

    @BeforeEach
    public void beforeEach() {
        myself = User.builder()
                .email("me@test.com")
                .profile("https://my.profile.path")
                .build();
        myself.setNickname("??????????????????");
        myself = userRepository.save(myself);

        session.setAttribute("user", SessionUser.from(myself));
        other = userRepository.save(User.builder()
                .email("you@test.com")
                .profile("https://your.profile.path")
                .build());
    }

    @AfterEach
    public void afterEach() {
        userRepository.deleteAll();
        session.clearAttributes();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("????????? ?????? ??????")
    void testGetUserSelf() throws Exception {
        mvc.perform(
                get("/api/user/{id}", myself.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").value(myself.getEmail()))
                .andExpect(jsonPath("nickname").value(myself.getNickname()))
                .andExpect(jsonPath("profile").value(myself.getProfile()))
                .andExpect(jsonPath("level").value(myself.getLevel()))
                .andExpect(jsonPath("feedCount").value(myself.getFeedCount()))
                .andExpect(jsonPath("scrapCount").value(myself.getScrapCount()))
                .andExpect(jsonPath("bookmarkedCount").value(myself.getBookmarkedCount()))
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.patch-user.href").exists())
                .andExpect(jsonPath("_links.delete-user.href").exists())
                .andExpect(jsonPath("_links.check-nickname-exists.href").exists())
                .andDo(document("get-user",
                        preprocessResponse(prettyPrint()),
                        links(
                                linkWithRel("self").description("??? ????????? ??????"),
                                linkWithRel("patch-user").description("??? ???????????? ?????? ???????????? ??????," +
                                        "???????????? ???????????? ?????? ????????????."),
                                linkWithRel("delete-user").description("??? ???????????? ?????? ?????? ??????"),
                                linkWithRel("check-nickname-exists").description("????????? ?????? ?????? ??????, " +
                                        "??????????????? ????????????, {nickname}??? ???????????? ????????? ????????????.")
                        ),
                        responseFields(
                                fieldWithPath("email").description("??? ???????????? ????????? ??????"),
                                fieldWithPath("nickname").description("??? ???????????? ?????????"),
                                fieldWithPath("profile").description("??? ???????????? ????????? ??????"),
                                fieldWithPath("level").description("??? ???????????? ??????"),
                                fieldWithPath("feedCount").description("??? ???????????? ?????? ??????"),
                                fieldWithPath("scrapCount").description("??? ???????????? ???????????? ?????? ??????"),
                                fieldWithPath("bookmarkedCount").description("??? ???????????? ???????????? ?????? ??????"),
                                subsectionWithPath("_links").description("??? ???????????? ?????? ????????? ?????? ?????????")
                        )));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("????????? ?????? ?????? - ??? ?????????")
    void testGetUserOther() throws Exception {
        mvc.perform(
                get("/api/user/{id}", other.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").value(other.getEmail()))
                .andExpect(jsonPath("nickname").value(other.getNickname()))
                .andExpect(jsonPath("profile").value(other.getProfile()))
                .andExpect(jsonPath("level").value(other.getLevel()))
                .andExpect(jsonPath("feedCount").value(other.getFeedCount()))
                .andExpect(jsonPath("scrapCount").value(other.getScrapCount()))
                .andExpect(jsonPath("bookmarkedCount").value(other.getBookmarkedCount()))
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.patch-user.href").doesNotExist())
                .andExpect(jsonPath("_links.delete-user.href").doesNotExist())
                .andExpect(jsonPath("_links.check-nickname-exists.href").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("????????? ?????? ?????? ?????? (404)")
    void testFailToGetInvalidUser() throws Exception {
        mvc.perform(
                get("/api/user/{id}", -1)
                        .session(session))
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("errorType")
                                .value(ResourceNotFoundException.class.getSimpleName()))
                .andExpect(
                        jsonPath("messages[0]")
                                .value("???????????? ?????? ??????????????????.(id:-1)"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("????????? ?????????, ????????? ????????????")
    void testUpdateUser() throws Exception {
        String nickname = "this???nikname?????????a";
        String profile = "http://profile.path";
        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .nickname(nickname)
                .profile(profile)
                .build();

        mvc.perform(
                patch("/api/user/{id}", myself.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").value(myself.getEmail()))
                .andExpect(jsonPath("nickname").value(nickname))
                .andExpect(jsonPath("profile").value(profile))
                .andExpect(jsonPath("level").value(myself.getLevel()))
                .andExpect(jsonPath("feedCount").value(myself.getFeedCount()))
                .andExpect(jsonPath("scrapCount").value(myself.getScrapCount()))
                .andExpect(jsonPath("bookmarkedCount").value(myself.getBookmarkedCount()))
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.patch-user.href").exists())
                .andExpect(jsonPath("_links.delete-user.href").exists())
                .andExpect(jsonPath("_links.check-nickname-exists.href").exists())
                .andDo(document("patch-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("nickname").description("????????? ?????????"),
                                fieldWithPath("profile").description("????????? ????????? ?????? ??????")
                        )));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("????????? ???????????? ?????? (400)")
    void testFailToUpdateUserBy400() throws Exception {
        String nickname = "this???nikname?????????!";
        String profile = "httpprofile.path";
        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .nickname(nickname)
                .profile(profile)
                .build();

        mvc.perform(
                patch("/api/user/{id}", myself.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("errorType")
                                .value(ConstraintViolationException.class.getSimpleName()))
                .andExpect(jsonPath("messages", Matchers.containsInAnyOrder(
                        "???????????? ??????/??????/??????/????????? ?????? ????????????, " +
                                    "1~10?????? ???????????? 2~20?????? ??????/??????/????????? ?????? ???????????????.",
                                "???????????? URL ????????? ?????????????????????."
                        )))
                .andDo(document("error-example",
                        responseFields(
                                fieldWithPath("errorType").description("????????? ????????? ??????"),
                                fieldWithPath("messages").description("????????? ?????? ?????? ?????????, " +
                                        "1??? ????????? ???????????? ????????? ??? ??????.")
                        )));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("????????? ???????????? ?????? (403)")
    void testFailToUpdateUserBy403() throws Exception {
        String nickname = "this???nikname?????????a";
        String profile = "http://profile.path";
        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .nickname(nickname)
                .profile(profile)
                .build();

        mvc.perform(
                patch("/api/user/{id}", other.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .session(session))
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("errorType")
                                .value(FailedAuthorizationException.class.getSimpleName()))
                .andExpect(
                        jsonPath("messages[0]")
                                .value("?????? ???????????? ????????? ????????? ??? ????????????.(id:" + other.getId() + ")"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("????????? ??????")
    void testDeleteUser() throws Exception {
        mvc.perform(
                delete("/api/user/{id}", myself.getId())
                        .session(session))
                .andExpect(status().isNoContent())
                .andDo(document("delete-user"));

        assertThat(userRepository.existsById(myself.getId())).isFalse();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("????????? ?????? ?????? (403)")
    void testFailToDeleteUser() throws Exception {
        mvc.perform(
                delete("/api/user/{id}", other.getId())
                        .session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("errorType")
                        .value(FailedAuthorizationException.class.getSimpleName()))
                .andExpect(
                        jsonPath("messages[0]")
                                .value("?????? ???????????? ????????? ????????? ??? ????????????.(id:" + other.getId() + ")"));

        assertThat(userRepository.existsById(other.getId())).isTrue();
    }

    @Test
    @DisplayName("????????? ?????? ???")
    void testNicknameExists() throws Exception {
        mvc.perform(
                get("/api/user/nickname/exists")
                        .param("nickname",  myself.getNickname()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("nickname").value(myself.getNickname()))
                .andExpect(jsonPath("exists").value(true))
                .andExpect(jsonPath("_links.self.href").exists())
                .andDo(document("check-nickname-exists",
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("nickname").description("?????? ????????? ????????? ?????????")
                        ),
                        responseFields(
                                fieldWithPath("nickname").description("?????? ????????? ????????? ?????????"),
                                fieldWithPath("exists").description("?????? ?????? ?????? - true: ?????? ??? / false: ?????? ?????? ??????"),
                                subsectionWithPath("_links").ignored()
                        )));
    }

    @Test
    @DisplayName("????????? ?????? ??????")
    void testNicknameNonExists() throws Exception {
        String nickname = "???????????????";

        mvc.perform(
                get("/api/user/nickname/exists")
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("nickname").value(nickname))
                .andExpect(jsonPath("exists").value(false))
                .andExpect(jsonPath("_links.self.href").exists());
    }

    @Test
    @DisplayName("????????? ?????? ????????? ????????? ?????? ??????")
    void testInvalidNicknameExists() throws Exception {
        mvc.perform(
                get("/api/user/nickname/exists")
                        .param("nickname", "this???nikname?????????!"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("errorType")
                                .value(ConstraintViolationException.class.getSimpleName()))
                .andExpect(jsonPath("messages[0]")
                        .value("???????????? ??????/??????/??????/????????? ?????? ????????????, " +
                                        "1~10?????? ???????????? 2~20?????? ??????/??????/????????? ?????? ???????????????."));
    }

}