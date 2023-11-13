package com.example.demo.user.service;


import com.example.demo.user.exception.CertificationCodeNotMatchedException;
import com.example.demo.user.exception.ResourceNotFoundException;
import com.example.demo.user.domain.UserStatus;
import com.example.demo.user.domain.UserCreate;
import com.example.demo.user.domain.UserUpdate;
import com.example.demo.user.infrastructure.UserEntity;
import com.example.demo.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
@SqlGroup({
        @Sql(value = "/sql/user-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void getByEmail_은_ACTIVE_상태인_유저를_찾아올_수_있다() {
        // given
        String email = "bing@test.com";

        // when
        UserEntity userEntity = userService.getByEmail(email);

        // then
        assertThat(userEntity.getNickname()).isEqualTo("bing");
    }

    @Test
    void getByEmail_은_PENDING_상태인_유저는_찾아올_수_없다() {
        // given
        String email = "bong@test.com";

        // when
        // then
        assertThatThrownBy(() -> {
            userService.getByEmail(email);
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_는_ACTIVE_상태인_유저를_찾아올_수_있다() {
        // given
        long id = 1;

        // when
        UserEntity userEntity = userService.getById(id);

        // then
        assertThat(userEntity.getNickname()).isEqualTo("bing");
    }

    @Test
    void getById_는_PENDING_상태인_유저는_찾아올_수_없다() {
        // given
        long id = 2;

        // when
        // then
        assertThatThrownBy(() -> {
            userService.getById(id);
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_으로_유저를_생성할_수_있다() {
        // given
        UserCreate userCreate = UserCreate
                .builder()
                .email("ming@test.com")
                .nickname("ming")
                .address("seoul-1")
                .build();

        // when
        UserEntity userEntity = userService.create(userCreate);

        // then
        assertThat(userEntity.getId()).isNotNull();
        assertThat(userEntity.getStatus()).isEqualTo(UserStatus.PENDING);
    }

    @Test
    void updateUser_으로_유저를_수정할_수_있다() {
        // given
        long id = 1;
        UserUpdate userUpdate = UserUpdate
                .builder()
                .nickname("ming-edit")
                .address("seoul-2")
                .build();

        // when
        userService.updateUser(id, userUpdate);

        // then
        UserEntity userEntity = userService.getById(1);
        assertThat(userEntity.getNickname()).isEqualTo("ming-edit");
        assertThat(userEntity.getAddress()).isEqualTo("seoul-2");
    }

    @Test
    void user를_로그인_시키면_마지막_로그인_시간이_변경된다() {
        // given

        // when
        userService.login(1);

        // then
        UserEntity userEntity = userService.getById(1);
        assertThat(userEntity.getLastLoginAt()).isGreaterThan(0L);
    }

    @Test
    void PENDING_상태의_사용자는_인증_코드로_ACTIVE_시킬_수_있다() {
        // given
        String certificationCode = "aaaa-aaaa-aaaa";

        // when
        userService.verifyEmail(2, certificationCode);

        // then
        UserEntity userEntity = userService.getById(2);
        assertThat(userEntity.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void PENDING_상태의_사용자는_잘못된_인증_코드를_받으면_에러를_던진다() {
        // given
        String wrongCertificationCode = "aaaa-aaaa-aaab";

        // when
        // then
        assertThatThrownBy(
                () -> userService.verifyEmail(2, wrongCertificationCode)
        ).isInstanceOf(CertificationCodeNotMatchedException.class);
    }
}
