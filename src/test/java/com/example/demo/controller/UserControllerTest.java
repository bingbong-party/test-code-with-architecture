package com.example.demo.controller;

import com.example.demo.model.UserStatus;
import com.example.demo.model.dto.UserUpdateDto;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@SqlGroup({
        @Sql(value = "/sql/user-controller-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public UserControllerTest() {
    }

    @Test
    public void 사용자는_특정_유저의_정보를_전달받을_수_있다() throws Exception {
        //given

        //when

        //then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("bing@test.com"));
    }

    @Test
    public void 사용자는_존재하지_않는_유저의_id로_api를_호출할_경우_404를_응답받는다() throws Exception {
        //given

        //when

        //then
        mockMvc.perform(get("/api/users/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void 사용자는_인증_코드로_계정을_활성화할_수_있다() throws Exception {
        //given

        //when

        //then
        mockMvc.perform(get("/api/users/2/verify")
                        .queryParam("certificationCode", "aaaa-aaaa-aaaa"))
                .andExpect(status().isFound());
        assertThat(userRepository.findByIdAndStatus(2, UserStatus.ACTIVE)).isPresent();
    }

    @Test
    public void 사용자는_내_정보를_불러올_때_개인정보인_주소도_가지고_온다() throws Exception {
        //given

        //when

        //then
        mockMvc.perform(get("/api/users/me")
                        .header("EMAIL", "bing@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("seoul-1"));
    }

    @Test
    public void 사용자는_내_정보를_수정할_수_있다() throws Exception {
        //given
        UserUpdateDto userUpdateDto = UserUpdateDto
                .builder()
                .address("bong-edit@test.com")
                .nickname("봉수정")
                .build();

        //when

        //then
        mockMvc.perform(put("/api/users/me")
                        .header("EMAIL", "bing@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("bong-edit@test.com"))
                .andExpect(jsonPath("$.nickname").value("봉수정"));
    }
}
