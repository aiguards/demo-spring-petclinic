package org.springframework.samples.petclinic.owner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ExtendWith(MockitoExtension.class)
class TestPetController {

    @Mock
    private OwnerRepository owners;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ModelMap modelMap;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private PetController petController;

    private Owner owner;
    private Pet pet;
    private Collection<PetType> petTypes;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Doe");

        pet = new Pet();
        pet.setId(1);
        pet.setName("Max");
        pet.setBirthDate(LocalDate.now().minusYears(2));

        petTypes = new ArrayList<>();
        PetType dog = new PetType();
        dog.setName("dog");
        petTypes.add(dog);
    }

    @Test
    void testPopulatePetTypes() {
        when(owners.findPetTypes()).thenReturn(petTypes);
        Collection<PetType> result = petController.populatePetTypes();
        assertEquals(petTypes, result);
    }

    @Test
    void testFindOwner() {
        when(owners.findById(1)).thenReturn(Optional.of(owner));
        Owner result = petController.findOwner(1);
        assertEquals(owner, result);
    }

    @Test
    void testFindOwnerNotFound() {
        when(owners.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> petController.findOwner(99));
    }

    @Test
    void testFindPetNew() {
        Pet result = petController.findPet(1, null);
        assertNotNull(result);
        assertTrue(result.isNew());
    }

    @Test
    void testFindPetExisting() {
        owner.addPet(pet);
        when(owners.findById(1)).thenReturn(Optional.of(owner));
        Pet result = petController.findPet(1, 1);
        assertEquals(pet, result);
    }

    @Test
    void testInitCreationForm() {
        String view = petController.initCreationForm(owner, modelMap);
        assertEquals("pets/createOrUpdatePetForm", view);
    }

    @Test
    void testProcessCreationFormSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("redirect:/owners/{ownerId}", view);
        verify(owners).save(any(Owner.class));
        verify(redirectAttributes).addFlashAttribute("message", "New Pet has been Added");
    }

    @Test
    void testProcessCreationFormWithDuplicateName() {
        owner.addPet(pet);
        Pet newPet = new Pet();
        newPet.setName("Max");
        
        String view = petController.processCreationForm(owner, newPet, bindingResult, redirectAttributes);
        
        assertEquals("pets/createOrUpdatePetForm", view);
    }

    @Test
    void testProcessCreationFormWithFutureBirthDate() {
        pet.setBirthDate(LocalDate.now().plusDays(1));
        
        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("pets/createOrUpdatePetForm", view);
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
        verify(owners).save(any(Owner.class));
        verify(redirectAttributes).addFlashAttribute("message", "Pet details has been edited");
    }

    @Test
    void testProcessUpdateFormWithDuplicateName() {
        Pet existingPet = new Pet();
        existingPet.setId(2);
        existingPet.setName("Max");
        owner.addPet(existingPet);
        
        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("pets/createOrUpdatePetForm", view);
    }

    @Test
    void testProcessUpdateFormWithFutureBirthDate() {
        pet.setBirthDate(LocalDate.now().plusDays(1));
        
        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertEquals("pets/createOrUpdatePetForm", view);
    }
}
