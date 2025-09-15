package com.local.bci.unit;

import com.local.bci.domain.model.UserModel;
import com.local.bci.infrastructure.exception.UserPersistenceException;
import com.local.bci.infrastructure.mapper.UserEntityModelMapper;
import com.local.bci.infrastructure.persistence.adapter.UserRepositoryAdapter;
import com.local.bci.infrastructure.persistence.entity.UserEntity;
import com.local.bci.infrastructure.persistence.jpa.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository jpaRepository;

    @Mock
    private UserEntityModelMapper mapper;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Test
    void findByEmail_ReturnsMappedUserModel() {
        String email = "kevin@example.com";
        UserEntity entity = new UserEntity();
        entity.setEmail(email);

        UserModel model = new UserModel();
        model.setEmail(email);

        when(jpaRepository.findByEmail(email)).thenReturn(Optional.of(entity));
        when(mapper.entityToModel(entity)).thenReturn(model);

        Optional<UserModel> result = adapter.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(jpaRepository).findByEmail(email);
        verify(mapper).entityToModel(entity);
    }

    @Test
    void findByEmail_WhenNotFound_ReturnsEmpty() {
        String email = "notfound@example.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<UserModel> result = adapter.findByEmail(email);

        assertTrue(result.isEmpty());
        verify(jpaRepository).findByEmail(email);
        verifyNoInteractions(mapper);
    }

    @Test
    void findByEmail_WhenDataAccessException_ThrowsUserPersistenceException() {
        String email = "error@example.com";
        when(jpaRepository.findByEmail(email)).thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB down"));

        UserPersistenceException exception = assertThrows(
                UserPersistenceException.class,
                () -> adapter.findByEmail(email)
        );

        assertEquals("Failed to find user by email", exception.getMessage());
        verify(jpaRepository).findByEmail(email);
        verifyNoInteractions(mapper);
    }

    @Test
    void apply_WhenDataAccessException_ThrowsUserPersistenceException() {
        UserModel model = new UserModel();
        model.setEmail("test@example.com");
        UserEntity entity = new UserEntity();
        entity.setEmail("test@example.com");

        when(mapper.modelToEntity(model)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB down"));

        UserPersistenceException exception = assertThrows(
                UserPersistenceException.class,
                () -> adapter.apply(model)
        );

        assertEquals("Failed to save user", exception.getMessage());
        verify(jpaRepository).save(entity);
        verify(mapper).modelToEntity(model);
        verify(mapper, never()).entityToModel(any());
    }

    @Test
    void findByToken_ReturnsMappedUserModel() {
        String token = "token123";
        UserEntity entity = new UserEntity();
        entity.setToken(token);

        UserModel model = new UserModel();
        model.setToken(token);

        when(jpaRepository.findByToken(token)).thenReturn(Optional.of(entity));
        when(mapper.entityToModel(entity)).thenReturn(model);

        Optional<UserModel> result = adapter.findByToken(token);

        assertTrue(result.isPresent());
        assertEquals(token, result.get().getToken());
        verify(jpaRepository).findByToken(token);
        verify(mapper).entityToModel(entity);
    }

    @Test
    void findByToken_WhenNotFound_ReturnsEmpty() {
        String token = "notfoundToken";
        when(jpaRepository.findByToken(token)).thenReturn(Optional.empty());

        Optional<UserModel> result = adapter.findByToken(token);

        assertTrue(result.isEmpty());
        verify(jpaRepository).findByToken(token);
        verifyNoInteractions(mapper);
    }

    @Test
    void findByToken_WhenDataAccessException_ThrowsUserPersistenceException() {
        String token = "errorToken";
        when(jpaRepository.findByToken(token)).thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB down"));

        UserPersistenceException exception = assertThrows(
                UserPersistenceException.class,
                () -> adapter.findByToken(token)
        );

        assertEquals("Failed to find user by token", exception.getMessage());
        verify(jpaRepository).findByToken(token);
        verifyNoInteractions(mapper);
    }
}
