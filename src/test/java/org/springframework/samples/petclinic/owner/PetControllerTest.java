package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private RedirectAttributes redirectAttributes;

    @Mock
    private ModelMap modelMap;

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
        assertThat(result).isEqualTo(petTypes);
    }

    @Test
    void testFindOwner() {
        when(owners.findById(1)).thenReturn(Optional.of(owner));
        Owner result = petController.findOwner(1);
        assertThat(result).isEqualTo(owner);
    }

    @Test
    void testFindOwnerNotFound() {
        when(owners.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> petController.findOwner(99));
    }

    @Test
    void testFindPet() {
        when(owners.findById(1)).thenReturn(Optional.of(owner));
        owner.addPet(pet);
        Pet result = petController.findPet(1, 1);
        assertThat(result).isEqualTo(pet);
    }

    @Test
    void testFindPetNewPet() {
        Pet result = petController.findPet(1, null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
    }

    @Test
    void testInitCreationForm() {
        String viewName = petController.initCreationForm(owner, modelMap);
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
        assertThat(owner.getPets()).hasSize(1);
    }

    @Test
    void testProcessCreationFormSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        String viewName = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(viewName).isEqualTo("redirect:/owners/{ownerId}");
        verify(owners).save(any(Owner.class));
        verify(redirectAttributes).addFlashAttribute("message", "New Pet has been Added");
    }

    @Test
    void testProcessCreationFormWithDuplicateName() {
        owner.addPet(pet);
        Pet newPet = new Pet();
        newPet.setName("Max");
        
        String viewName = petController.processCreationForm(owner, newPet, bindingResult, redirectAttributes);
        
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessCreationFormWithFutureBirthDate() {
        pet.setBirthDate(LocalDate.now().plusDays(1));
        
        String viewName = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testInitUpdateForm() {
        String viewName = petController.initUpdateForm();
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessUpdateFormSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        String viewName = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(viewName).isEqualTo("redirect:/owners/{ownerId}");
        verify(owners).save(any(Owner.class));
        verify(redirectAttributes).addFlashAttribute("message", "Pet details has been edited");
    }

    @Test
    void testProcessUpdateFormWithDuplicateName() {
        Pet existingPet = new Pet();
        existingPet.setId(2);
        existingPet.setName("Max");
        owner.addPet(existingPet);
        
        String viewName = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessUpdateFormWithFutureBirthDate() {
        pet.setBirthDate(LocalDate.now().plusDays(1));
        
        String viewName = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
    }
}
