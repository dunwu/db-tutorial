package io.github.dunwu.javadb.h2.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Boot + JPA 基于 REST 的 CRUD 测试
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2019-10-12
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
public class SpringBootJpaRestTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void deleteAllBeforeTests() {
        userRepository.deleteAll();
    }

    @Test
    public void shouldCreateEntity() throws Exception {
        User user = new User("张三", 18, "北京", "user1@163.com");
        mockMvc.perform(post("/user").content(objectMapper.writeValueAsString(user))).andExpect(status().isCreated())
               .andExpect(header().string("Location", containsString("user/")));
    }

    @Test
    public void shouldDeleteEntity() throws Exception {
        User user = new User("张三", 18, "北京", "user1@163.com");
        MvcResult mvcResult = mockMvc.perform(post("/user").content(objectMapper.writeValueAsString(user)))
                                     .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();

        mockMvc.perform(delete(location)).andExpect(status().isNoContent());
        mockMvc.perform(get(location)).andExpect(status().isNotFound());
    }

    @Test
    public void shouldPartiallyUpdateEntity() throws Exception {
        User user = new User("张三", 18, "北京", "user1@163.com");
        User user2 = new User("李四", 19, "上海", "user2@163.com");

        MvcResult mvcResult = mockMvc.perform(post("/user").content(objectMapper.writeValueAsString(user)))
                                     .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();

        mockMvc.perform(patch(location).content(objectMapper.writeValueAsString(user2)))
               .andExpect(status().isNoContent());
        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("李四"))
               .andExpect(jsonPath("$.password").value("123456")).andExpect(jsonPath("$.email").value("user2@163.com"));
    }

    @Test
    public void shouldQueryEntity() throws Exception {
        User user = new User("张三", 18, "北京", "user1@163.com");
        mockMvc.perform(post("/user").content(objectMapper.writeValueAsString(user))).andExpect(status().isCreated());
        mockMvc.perform(get("/user/search/findByEmail?email={email}", "user1@163.com")).andExpect(status().isOk());
    }

    @Test
    public void shouldRetrieveEntity() throws Exception {
        User user = new User("张三", 18, "北京", "user1@163.com");
        MvcResult mvcResult = mockMvc.perform(post("/user").content(objectMapper.writeValueAsString(user)))
                                     .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("张三"))
               .andExpect(jsonPath("$.email").value("user1@163.com"));
    }

    @Test
    public void shouldReturnRepositoryIndex() throws Exception {
        mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
               .andExpect(jsonPath("$._links.user").exists());
    }

    @Test
    public void shouldUpdateEntity() throws Exception {
        User user = new User("张三", 18, "北京", "user1@163.com");
        User user2 = new User("李四", 19, "上海", "user2@163.com");

        MvcResult mvcResult = mockMvc.perform(post("/user").content(objectMapper.writeValueAsString(user)))
                                     .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();

        mockMvc.perform(put(location).content(objectMapper.writeValueAsString(user2)))
               .andExpect(status().isNoContent());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("李四"))
               .andExpect(jsonPath("$.password").value("123456"));
    }

}
