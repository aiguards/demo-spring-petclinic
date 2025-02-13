package org.springframework.samples.petclinic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

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
        petClinicRuntimeHints.registerHints(mockRuntimeHints, getClass().getClassLoader());

        verify(mockRuntimeHints.resources()).registerPattern("db/*");
        verify(mockRuntimeHints.resources()).registerPattern("messages/*");
        verify(mockRuntimeHints.resources()).registerPattern("META-INF/resources/webjars/*");
        verify(mockRuntimeHints.resources()).registerPattern("mysql-default-conf");
    }

    @Test
    void testRegisterHintsRegistersSerializationTypes() {
        petClinicRuntimeHints.registerHints(mockRuntimeHints, getClass().getClassLoader());

        verify(mockRuntimeHints.serialization()).registerType(TypeReference.of("org.springframework.samples.petclinic.model.BaseEntity"));
        verify(mockRuntimeHints.serialization()).registerType(TypeReference.of("org.springframework.samples.petclinic.model.Person"));
        verify(mockRuntimeHints.serialization()).registerType(TypeReference.of("org.springframework.samples.petclinic.vet.Vet"));
    }

    @Test
    void testRegisterHintsWithNullRuntimeHints() {
        petClinicRuntimeHints.registerHints(null, getClass().getClassLoader());
        // No exception should be thrown
    }

    @Test
    void testRegisterHintsWithNullClassLoader() {
        petClinicRuntimeHints.registerHints(mockRuntimeHints, null);
        
        // Verify that resource patterns are still registered
        verify(mockRuntimeHints.resources(), times(4)).registerPattern(Mockito.anyString());
    }
}
