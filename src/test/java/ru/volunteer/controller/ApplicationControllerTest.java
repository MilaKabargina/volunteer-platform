package ru.volunteer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.volunteer.config.SecurityConfig;
import ru.volunteer.model.dto.ApplicationRequestDto;
import ru.volunteer.model.dto.ApplicationResponseDto;
import ru.volunteer.model.entity.Initiative;
import ru.volunteer.model.entity.User;
import ru.volunteer.model.enums.ApplicationStatus;
import ru.volunteer.service.ApplicationService;
import ru.volunteer.service.AuthService;
import ru.volunteer.service.InitiativeService;
import ru.volunteer.service.security.JwtService;
import ru.volunteer.testutil.TestDataFactory;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApplicationController.class)
@Import(SecurityConfig.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private AuthService authService;

    @MockBean
    private InitiativeService initiativeService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private User testUser;
    private User initiativeAuthor;
    private Initiative testInitiative;
    private ApplicationResponseDto testApplicationDto;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser(1L, "testUser");
        testUser.setFirstName("Тестовый");
        testUser.setSecondName("Пользователь");

        initiativeAuthor = TestDataFactory.createTestUser(2L, "authorUser");
        initiativeAuthor.setFirstName("Автор");
        initiativeAuthor.setSecondName("Инициативы");

        testInitiative = TestDataFactory.createTestInitiative(1L, initiativeAuthor, "Тестовая инициатива", "ПОМОЩЬ");

        testApplicationDto = new ApplicationResponseDto(
                1L,
                1L,
                "Тестовая инициатива",
                1L,
                "testUser",
                ApplicationStatus.PENDING,
                "Хочу помочь",
                null  // contactInfo (может быть null)
        );
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("POST /applications - успешное создание заявки")
    void createApplication_shouldReturn201() throws Exception {
        ApplicationRequestDto requestDto = new ApplicationRequestDto();
        requestDto.setIdInitiative(1L);
        requestDto.setMessage("Хочу помочь");

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(initiativeService.findEntityById(1L)).thenReturn(testInitiative);
        when(applicationService.respondToInitiative(eq(testUser), eq(testInitiative), eq("Хочу помочь")))
                .thenReturn(testApplicationDto);

        mockMvc.perform(post("/api/v1/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("POST /applications - невалидные данные -> 400")
    void createApplication_withoutInitiativeId_shouldReturn400() throws Exception {
        ApplicationRequestDto invalidDto = new ApplicationRequestDto();
        invalidDto.setMessage("Сообщение");

        mockMvc.perform(post("/api/v1/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("GET /applications/my - получение своих заявок")
    void getMyApplications_shouldReturnUserApplications() throws Exception {
        List<ApplicationResponseDto> applications = List.of(testApplicationDto);
        when(applicationService.getUserApplicationsAsDto(eq(testUser))).thenReturn(applications);
        when(authService.getCurrentUser()).thenReturn(testUser);

        mockMvc.perform(get("/api/v1/applications/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("GET /applications/my-initiatives - получение заявок на свои инициативы")
    void getApplicationsForMyInitiatives_shouldReturnApplications() throws Exception {
        List<ApplicationResponseDto> applications = List.of(testApplicationDto);
        when(applicationService.getApplicationsForAuthor(eq(testUser))).thenReturn(applications);
        when(authService.getCurrentUser()).thenReturn(testUser);

        mockMvc.perform(get("/api/v1/applications/my-initiatives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "authorUser", roles = {"USER"})
    @DisplayName("PUT /applications/{id}/status/{status} - автор инициативы меняет статус заявки")
    void updateApplicationStatus_asAuthor_shouldUpdateStatus() throws Exception {
        ApplicationResponseDto updatedDto = new ApplicationResponseDto(
                1L, 1L, "Тестовая инициатива", 1L, "testUser",
                ApplicationStatus.APPROVED, "Хочу помочь", null
        );
        when(authService.getCurrentUser()).thenReturn(initiativeAuthor);
        when(applicationService.updateStatusDto(eq(1L), eq(ApplicationStatus.APPROVED), eq(initiativeAuthor)))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/v1/applications/1/status/APPROVED")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("GET /applications/{id} - получение заявки по ID")
    void getApplicationById_shouldReturnApplication() throws Exception {
        when(applicationService.findDtoById(1L)).thenReturn(testApplicationDto);

        mockMvc.perform(get("/api/v1/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("POST /applications - без аутентификации -> 401")
    void createApplication_withoutAuth_shouldReturn401() throws Exception {
        ApplicationRequestDto requestDto = new ApplicationRequestDto();
        requestDto.setIdInitiative(1L);
        requestDto.setMessage("Сообщение");

        mockMvc.perform(post("/api/v1/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }
}