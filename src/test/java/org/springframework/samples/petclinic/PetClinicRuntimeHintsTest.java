package org.springframework.samples.petclinic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aot.hint.ResourcePatternHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.SerializationHints;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.samples.petclinic.vet.Vet;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TestPetClinicRuntimeHints {

    private PetClinicRuntimeHints runtimeHintsRegistrar;

    @Mock
    private RuntimeHints runtimeHints;

    @Mock
    private ResourcePatternHints resourcePatternHints;

    @Mock
    private SerializationHints serializationHints;

    @BeforeEach
    void setUp() {
        runtimeHintsRegistrar = new PetClinicRuntimeHints();
        when(runtimeHints.resources()).thenReturn(resourcePatternHints);
        when(runtimeHints.serialization()).thenReturn(serializationHints);
    }

    @Test
    void testRegisterHints() {
        // Act
        runtimeHintsRegistrar.registerHints(runtimeHints, getClass().getClassLoader());

        // Assert resource pattern registrations
        verify(resourcePatternHints).registerPattern("db/*");
        verify(resourcePatternHints).registerPattern("messages/*");
        verify(resourcePatternHints).registerPattern("META-INF/resources/webjars/*");
        verify(resourcePatternHints).registerPattern("mysql-default-conf");

        // Assert serialization type registrations
        verify(serializationHints).registerType(BaseEntity.class);
        verify(serializationHints).registerType(Person.class);
        verify(serializationHints).registerType(Vet.class);

        // Verify no more interactions
        verifyNoMoreInteractions(resourcePatternHints);
        verifyNoMoreInteractions(serializationHints);
    }

    @Test
    void testRegisterHintsWithNullClassLoader() {
        // Act & Assert
        assertDoesNotThrow(() -> 
            runtimeHintsRegistrar.registerHints(runtimeHints, null)
        );
    }

    @Test
    void testRegisterHintsWithNullRuntimeHints() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            runtimeHintsRegistrar.registerHints(null, getClass().getClassLoader())
        );
    }
}
