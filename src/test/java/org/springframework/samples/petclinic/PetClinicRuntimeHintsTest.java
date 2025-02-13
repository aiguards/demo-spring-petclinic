package org.springframework.samples.petclinic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.samples.petclinic.vet.Vet;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TestPetClinicRuntimeHints {

    @Mock
    private RuntimeHints hints;

    private PetClinicRuntimeHints petClinicRuntimeHints;

    @BeforeEach
    void setUp() {
        petClinicRuntimeHints = new PetClinicRuntimeHints();
    }

    @Test
    void testRegisterHintsRegistersResourcePatterns() {
        petClinicRuntimeHints.registerHints(hints, getClass().getClassLoader());
        
        verify(hints.resources(), times(1)).registerPattern("db/*");
        verify(hints.resources(), times(1)).registerPattern("messages/*");
        verify(hints.resources(), times(1)).registerPattern("META-INF/resources/webjars/*");
        verify(hints.resources(), times(1)).registerPattern("mysql-default-conf");
    }

    @Test
    void testRegisterHintsRegistersSerializationTypes() {
        petClinicRuntimeHints.registerHints(hints, getClass().getClassLoader());
        
        verify(hints.serialization(), times(1)).registerType(BaseEntity.class);
        verify(hints.serialization(), times(1)).registerType(Person.class);
        verify(hints.serialization(), times(1)).registerType(Vet.class);
    }

    @Test
    void testRegisterHintsWithNullClassLoader() {
        petClinicRuntimeHints.registerHints(hints, null);
        
        // Verify that resource patterns are still registered even with null class loader
        verify(hints.resources(), times(1)).registerPattern("db/*");
        verify(hints.resources(), times(1)).registerPattern("messages/*");
        verify(hints.resources(), times(1)).registerPattern("META-INF/resources/webjars/*");
        verify(hints.resources(), times(1)).registerPattern("mysql-default-conf");
    }
}
