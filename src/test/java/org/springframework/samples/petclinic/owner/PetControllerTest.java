package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestPetController {

    @Mock
    private OwnerRepository owners;

    @Mock
    private ModelMap model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private PetController petController;

    private Owner owner;
    private Pet pet;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setId(1);
        
        pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setBirthDate(LocalDate.now().minusYears(2));
        owner.addPet(pet);
    }

    @Test
    void testPopulatePetTypes() {
        when(owners.findPetTypes()).thenReturn(Collections.singletonList(new PetType()));
        
        Collection<PetType> types = petController.populatePetTypes();
        
        assertNotNull(types);
        assertEquals(1, types.size());
        verify(owners).findPetTypes();
    }

    @Test
    void testFindOwnerSuccess() {
        when(owners.findById(anyInt())).thenReturn(Optional.of(owner));
        
        Owner result = petController.findOwner(1);
        
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testFindOwnerNotFound() {
        when(owners.findById(anyInt())).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> {
            petController.findOwner(999);
        });
    }

    @Test
    void testFindPetExisting() {
        when(owners.findById(anyInt())).thenReturn(Optional.of(owner));
        
        Pet result = petController.findPet(1, 1);
        
        assertNotNull(result);
        assertEquals("Buddy", result.getName());
    }

    @Test
    void testFindPetNew() {
        Pet result = petController.findPet(1, null);
        
        assertNotNull(result);
        assertNull(result.getId());
    }

    @Test
    void testInitCreationForm() {
        String view = petController.initCreationForm(owner, model);
        
        assertEquals("pets/createOrUpdatePetForm", view);
        assertEquals(1, owner.getPets().size());
    }

    @Test
    void testProcessCreationFormSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("redirect:/owners/{ownerId}", view);
        verify(owners).save(owner);
        verify(redirectAttributes).addFlashAttribute("message", "New Pet has been Added");
    }

    @Test
    void testProcessCreationFormWithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        
        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("pets/createOrUpdatePetForm", view);
        verify(owners, never()).save(any());
    }

    @Test
    void testProcessCreationFormWithDuplicateName() {
        Pet existingPet = new Pet();
        existingPet.setName("Buddy");
        owner.addPet(existingPet);
        
        when(bindingResult.hasErrors()).thenReturn(true);
        
        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("pets/createOrUpdatePetForm", view);
        verify(bindingResult).rejectValue("name", "duplicate", "already exists");
    }

    @Test
    void testProcessCreationFormWithFutureBirthDate() {
        pet.setBirthDate(LocalDate.now().plusDays(1));
        when(bindingResult.hasErrors()).thenReturn(true);
        
        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("pets/createOrUpdatePetForm", view);
        verify(bindingResult).rejectValue("birthDate", "typeMismatch.birthDate");
    }

    @Test
    void testInitUpdateForm() {
        String view = petController.initUpdateForm();
        
        assertEquals("pets/createOrUpdatePetForm", view);
    }

    @Test
    void testProcessUpdateFormSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("redirect:/owners/{ownerId}", view);
        verify(owners).save(owner);
        verify(redirectAttributes).addFlashAttribute("message", "Pet details has been edited");
    }

    @Test
    void testProcessUpdateFormWithErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);
        
        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("pets/createOrUpdatePetForm", view);
        verify(owners, never()).save(any());
    }

    @Test
    void testProcessUpdateFormWithDuplicateName() {
        Pet existingPet = new Pet();
        existingPet.setId(2);
        existingPet.setName("Buddy");
        owner.addPet(existingPet);
        
        when(bindingResult.hasErrors()).thenReturn(true);
        
        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("pets/createOrUpdatePetForm", view);
        verify(bindingResult).rejectValue("name", "duplicate", "already exists");
    }

    @Test
    void testProcessUpdateFormWithFutureBirthDate() {
        pet.setBirthDate(LocalDate.now().plusDays(1));
        when(bindingResult.hasErrors()).thenReturn(true);
        
        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("pets/createOrUpdatePetForm", view);
        verify(bindingResult).rejectValue("birthDate", "typeMismatch.birthDate");
    }
}
