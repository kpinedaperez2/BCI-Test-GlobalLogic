package com.local.bci.unit;

import com.local.bci.application.dto.PhoneDTO;
import com.local.bci.application.dto.SignUpRequestDTO;
import com.local.bci.application.dto.UserResponseDTO;
import com.local.bci.application.mapper.UserDtoModelMapper;
import com.local.bci.application.usecase.impl.SignUpUseCaseImpl;
import com.local.bci.domain.model.PhoneModel;
import com.local.bci.domain.model.UserModel;
import com.local.bci.domain.port.persistence.FindUserByEmailPort;
import com.local.bci.domain.port.persistence.SaveUserPort;
import com.local.bci.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpUseCaseTest {

    @Mock
    private UserDtoModelMapper dtoMapper;

    @Mock
    private FindUserByEmailPort findUserByEmailPort;

    @Mock
    private SaveUserPort saveUserPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private SignUpUseCaseImpl signUpUseCase;

    private SignUpRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new SignUpRequestDTO();
        request.setName("Kevin Pineda");
        request.setEmail("kevin.pineda@example.com");
        request.setPassword("abcdeF12");

        PhoneDTO phone = new PhoneDTO();
        phone.setNumber(123456789L);
        phone.setCityCode(11);
        phone.setCountryCode("+54");
        request.setPhones(Collections.singletonList(phone));

        ReflectionTestUtils.setField(signUpUseCase, "emailPattern", Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"));
        ReflectionTestUtils.setField(signUpUseCase, "passwordPattern", Pattern.compile("^(?=.{8,12}$)(?=[^A-Z]*[A-Z][^A-Z]*$)(?=[^0-9]*[0-9][^0-9]*[0-9][^0-9]*$)[A-Za-z0-9]+$"));

    }

    @Test
    void shouldThrowWhenEmailNull() {
        request.setEmail(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> signUpUseCase.apply(request));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid email"));
    }

    @Test
    void shouldThrowWhenEmailInvalidFormat() {
        request.setEmail("invalid-email");
        assertThrows(IllegalArgumentException.class, () -> signUpUseCase.apply(request));
    }

    @Test
    void shouldThrowWhenPasswordNull() {
        request.setPassword(null);
        assertThrows(IllegalArgumentException.class, () -> signUpUseCase.apply(request));
    }

    @Test
    void shouldThrowWhenPasswordInvalid() {
        request.setPassword("badpass");
        assertThrows(IllegalArgumentException.class, () -> signUpUseCase.apply(request));
    }

    @Test
    void shouldThrowWhenUserAlreadyExists() {
        when(findUserByEmailPort.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(new UserModel()));

        assertThrows(IllegalStateException.class, () -> signUpUseCase.apply(request));
        verify(findUserByEmailPort).findByEmail(request.getEmail());
        verifyNoMoreInteractions(saveUserPort);
    }

    @Test
    void shouldCreateUserSuccessfully() {

        when(findUserByEmailPort.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(dtoMapper.dtoToModel(eq(request))).thenAnswer(inv -> {
            SignUpRequestDTO r = inv.getArgument(0);
            UserModel m = new UserModel();
            m.setName(r.getName());
            m.setEmail(r.getEmail());
            m.setIsActive(true);
            m.setPhones(Collections.singletonList(new PhoneModel()));
            return m;
        });

        when(passwordEncoder.encode(request.getPassword())).thenReturn("ENCODED_PASS");
        when(jwtService.generateToken(request.getEmail())).thenReturn("INITIAL_TOKEN");

        when(saveUserPort.apply(any(UserModel.class))).thenAnswer(inv -> inv.getArgument(0));

        when(dtoMapper.modelToResponseDto(any(UserModel.class))).thenAnswer(inv -> {
            UserModel m = inv.getArgument(0);
            UserResponseDTO resp = new UserResponseDTO();
            resp.setEmail(m.getEmail());
            resp.setId(m.getId());
            resp.setPhones(null);
            return resp;
        });

        UserResponseDTO result = signUpUseCase.apply(request);

        assertNotNull(result);
        assertEquals(request.getEmail(), result.getEmail());

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(saveUserPort).apply(captor.capture());

        UserModel savedArg = captor.getValue();
        assertNotNull(savedArg.getPasswordEncrypted());
        assertEquals("ENCODED_PASS", savedArg.getPasswordEncrypted());
        assertEquals("INITIAL_TOKEN", savedArg.getToken());
        assertNotNull(savedArg.getId());
        assertNotNull(savedArg.getCreated());
        assertNotNull(savedArg.getLastLogin());

        verify(dtoMapper).modelToResponseDto(savedArg);
    }
}