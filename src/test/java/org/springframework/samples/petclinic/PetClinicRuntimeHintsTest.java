package org.springframework.samples.petclinic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.samples.petclinic.vet.Vet;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class TestPetClinicRuntimeHints {

    private PetClinicRuntimeHints petClinicRuntimeHints;
    private RuntimeHints mockRuntimeHints;

    @BeforeEach
    void setUp() {
        petClinicRuntimeHints = new PetClinicRuntimeHints();
        mockRuntimeHints = Mockito.mock(RuntimeHints.class);
    }

    @Test
    void testRegisterHintsRegistersResourcePatterns() {
        petClinicRuntimeHints.registerHints(mockRuntimeHints, null);
        
        verify(mockRuntimeHints.resources()).registerPattern("db/*");
        verify(mockRuntimeHints.resources()).registerPattern("messages/*");
        verify(mockRuntimeHints.resources()).registerPattern("help/*");
        verify(mockRuntimeHints.resources()).registerPattern("META-INF/resources/webjars/*");
        verify(mockRuntimeHints.resources()).registerPattern("mysql-default-conf");
    }

    @Test
    void testRegisterHintsRegistersSerializationTypes() {
        petClinicRuntimeHints.registerHints(mockRuntimeHints, null);
        
        verify(mockRuntimeHints.serialization()).registerType(BaseEntity.class);
        verify(mockRuntimeHints.serialization()).registerType(Person.class);
        verify(mockRuntimeHints.serialization()).registerType(Vet.class);
    }

    @Test
    void testRegisterHintsWithNullRuntimeHints() {
        // Should not throw exception
        petClinicRuntimeHints.registerHints(null, null);
    }

    @Test
    void testRegisterHintsWithNullClassLoader() {
        petClinicRuntimeHints.registerHints(mockRuntimeHints, null);
        
        // Verify at least one registration to ensure method executed
        verify(mockRuntimeHints.resources()).registerPattern("db/*");
    }
}
