package com.local.bci.unit;

import com.local.bci.application.dto.UserResponseDTO;
import com.local.bci.application.mapper.UserDtoModelMapper;
import com.local.bci.application.usecase.impl.LoginUseCaseImpl;
import com.local.bci.domain.model.UserModel;
import com.local.bci.domain.port.persistence.FindUserByTokenPort;
import com.local.bci.domain.port.persistence.SaveUserPort;
import com.local.bci.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private FindUserByTokenPort findUserByTokenPort;

    @Mock
    private SaveUserPort saveUserPort;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDtoModelMapper dtoMapper;

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;

    private final String rawToken = "some.token.value";

    @BeforeEach
    void setUp() { }

    @Test
    void shouldThrowWhenTokenNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> loginUseCase.apply(null));
        assertThrows(IllegalArgumentException.class, () -> loginUseCase.apply(""));
    }

    @Test
    void shouldThrowWhenTokenInvalidAccordingToJwtService() {
        when(jwtService.validateToken(rawToken)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> loginUseCase.apply(rawToken));
    }

    @Test
    void shouldThrowWhenUserNotFoundForToken() {
        when(jwtService.validateToken(rawToken)).thenReturn(true);
        when(jwtService.getSubject(rawToken)).thenReturn("kevin@example.com");
        when(findUserByTokenPort.findByToken(rawToken)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> loginUseCase.apply(rawToken));
    }

    @Test
    void shouldThrowWhenSubjectMismatch() {
        when(jwtService.validateToken(rawToken)).thenReturn(true);
        when(jwtService.getSubject(rawToken)).thenReturn("kevin@example.com");

        UserModel found = new UserModel();
        found.setEmail("other@example.com");
        when(findUserByTokenPort.findByToken(rawToken)).thenReturn(Optional.of(found));

        assertThrows(IllegalArgumentException.class, () -> loginUseCase.apply(rawToken));
    }

    @Test
    void shouldLoginSuccessfullyAndRotateToken() {
        String subject = "kevin.pineda@example.com";
        when(jwtService.validateToken(rawToken)).thenReturn(true);
        when(jwtService.getSubject(rawToken)).thenReturn(subject);

        UserModel stored = new UserModel();
        stored.setEmail(subject);
        stored.setId(UUID.randomUUID());
        stored.setPasswordEncrypted("ENC-PASS");
        stored.setToken(rawToken);
        stored.setLastLogin(LocalDateTime.now().minusDays(1));
        stored.setIsActive(true);

        when(findUserByTokenPort.findByToken(rawToken)).thenReturn(Optional.of(stored));
        when(jwtService.generateToken(subject)).thenReturn("NEW_TOKEN");
        when(saveUserPort.apply(any(UserModel.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO mapped = new UserResponseDTO();
        mapped.setEmail(subject);
        mapped.setId(stored.getId());
        when(dtoMapper.modelToResponseDto(any(UserModel.class))).thenReturn(mapped);

        UserResponseDTO resp = loginUseCase.apply(rawToken);

        assertNotNull(resp);
        assertEquals(subject, resp.getEmail());
        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(saveUserPort).apply(captor.capture());
        UserModel savedArg = captor.getValue();

        assertEquals("NEW_TOKEN", savedArg.getToken());
        assertNotNull(savedArg.getLastLogin());
        verify(dtoMapper).modelToResponseDto(savedArg);

        assertEquals(savedArg.getPasswordEncrypted(), resp.getPassword());
    }

    @Test
    void shouldThrowWhenSubjectNullOrBlank() {
        when(jwtService.validateToken(rawToken)).thenReturn(true);

        when(jwtService.getSubject(rawToken)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> loginUseCase.apply(rawToken));

        when(jwtService.getSubject(rawToken)).thenReturn("");
        assertThrows(IllegalArgumentException.class, () -> loginUseCase.apply(rawToken));
    }

    @Test
    void shouldThrowWhenUserIsInactive() {
        String subject = "kevin.pineda@example.com";

        when(jwtService.validateToken(rawToken)).thenReturn(true);
        when(jwtService.getSubject(rawToken)).thenReturn(subject);

        UserModel inactiveUser = new UserModel();
        inactiveUser.setEmail(subject);
        inactiveUser.setIsActive(false);
        inactiveUser.setId(UUID.randomUUID());
        when(findUserByTokenPort.findByToken(rawToken)).thenReturn(Optional.of(inactiveUser));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> loginUseCase.apply(rawToken)
        );

        assertEquals("Cannot login inactive user", exception.getMessage());
    }

}
