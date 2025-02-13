package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestPetValidator {

    private PetValidator petValidator;
    
    @Mock
    private Pet mockPet;
    
    @BeforeEach
    void setUp() {
        petValidator = new PetValidator();
    }

    @Test
    void testSupportsWithPetClass() {
        assertTrue(petValidator.supports(Pet.class));
    }

    @Test
    void testSupportsWithNonPetClass() {
        assertFalse(petValidator.supports(Object.class));
    }

    @Test
    void testValidateWithValidPet() {
        when(mockPet.getName()).thenReturn("Buddy");
        when(mockPet.getType()).thenReturn(new PetType());
        when(mockPet.getBirthDate()).thenReturn(LocalDate.now());
        
        Errors errors = new BeanPropertyBindingResult(mockPet, "pet");
        petValidator.validate(mockPet, errors);
        
        assertFalse(errors.hasErrors());
    }

    @Test
    void testValidateWithEmptyName() {
        when(mockPet.getName()).thenReturn("");
        when(mockPet.getType()).thenReturn(new PetType());
        when(mockPet.getBirthDate()).thenReturn(LocalDate.now());
        
        Errors errors = new BeanPropertyBindingResult(mockPet, "pet");
        petValidator.validate(mockPet, errors);
        
        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("name"));
    }

    @Test
    void testValidateWithNullName() {
        when(mockPet.getName()).thenReturn(null);
        when(mockPet.getType()).thenReturn(new PetType());
        when(mockPet.getBirthDate()).thenReturn(LocalDate.now());
        
        Errors errors = new BeanPropertyBindingResult(mockPet, "pet");
        petValidator.validate(mockPet, errors);
        
        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("name"));
    }

    @Test
    void testValidateWithNullTypeForNewPet() {
        when(mockPet.isNew()).thenReturn(true);
        when(mockPet.getName()).thenReturn("Buddy");
        when(mockPet.getType()).thenReturn(null);
        when(mockPet.getBirthDate()).thenReturn(LocalDate.now());
        
        Errors errors = new BeanPropertyBindingResult(mockPet, "pet");
        petValidator.validate(mockPet, errors);
        
        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("type"));
    }

    @Test
    void testValidateWithNullBirthDate() {
        when(mockPet.getName()).thenReturn("Buddy");
        when(mockPet.getType()).thenReturn(new PetType());
        when(mockPet.getBirthDate()).thenReturn(null);
        
        Errors errors = new BeanPropertyBindingResult(mockPet, "pet");
        petValidator.validate(mockPet, errors);
        
        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("birthDate"));
    }

    @Test
    void testValidateWithAllFieldsInvalid() {
        when(mockPet.isNew()).thenReturn(true);
        when(mockPet.getName()).thenReturn("");
        when(mockPet.getType()).thenReturn(null);
        when(mockPet.getBirthDate()).thenReturn(null);
        
        Errors errors = new BeanPropertyBindingResult(mockPet, "pet");
        petValidator.validate(mockPet, errors);
        
        assertTrue(errors.hasErrors());
        assertEquals(3, errors.getErrorCount());
    }
}
