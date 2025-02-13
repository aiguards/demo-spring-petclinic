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
        PetType dog = new PetType();
        dog.setName("dog");
        pet.setType(dog);
    }

    @Test
    void testPopulatePetTypes() {
        Collection<PetType> petTypes = new ArrayList<>();
        PetType dog = new PetType();
        dog.setName("dog");
        petTypes.add(dog);

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
    void testFindPetNew() {
        Pet result = petController.findPet(1, null);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
    }

    @Test
    void testFindPetExisting() {
        owner.addPet(pet);
        when(owners.findById(1)).thenReturn(Optional.of(owner));

        Pet result = petController.findPet(1, 1);
        assertThat(result).isEqualTo(pet);
    }

    @Test
    void testInitCreationForm() {
        String view = petController.initCreationForm(owner, modelMap);
        assertThat(view).isEqualTo("pets/createOrUpdatePetForm");
        assertThat(owner.getPets()).hasSize(1);
    }

    @Test
    void testProcessCreationFormSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(view).isEqualTo("redirect:/owners/{ownerId}");
        verify(owners).save(any(Owner.class));
        verify(redirectAttributes).addFlashAttribute("message", "New Pet has been Added");
    }

    @Test
    void testProcessCreationFormHasErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(view).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessCreationFormDuplicateName() {
        owner.addPet(pet);
        Pet newPet = new Pet();
        newPet.setName("Max");

        String view = petController.processCreationForm(owner, newPet, bindingResult, redirectAttributes);
        
        verify(bindingResult).rejectValue("name", "duplicate", "already exists");
    }

    @Test
    void testProcessCreationFormFutureBirthDate() {
        pet.setBirthDate(LocalDate.now().plusDays(1));

        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        verify(bindingResult).rejectValue("birthDate", "typeMismatch.birthDate");
    }

    @Test
    void testInitUpdateForm() {
        String view = petController.initUpdateForm();
        assertThat(view).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessUpdateFormSuccess() {
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(view).isEqualTo("redirect:/owners/{ownerId}");
        verify(owners).save(any(Owner.class));
        verify(redirectAttributes).addFlashAttribute("message", "Pet details has been edited");
    }

    @Test
    void testProcessUpdateFormHasErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(view).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessUpdateFormDuplicateName() {
        Pet existingPet = new Pet();
        existingPet.setId(2);
        existingPet.setName("Max");
        owner.addPet(existingPet);

        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        verify(bindingResult).rejectValue("name", "duplicate", "already exists");
    }

    @Test
    void testProcessUpdateFormFutureBirthDate() {
        pet.setBirthDate(LocalDate.now().plusDays(1));

        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        verify(bindingResult).rejectValue("birthDate", "typeMismatch.birthDate");
    }
}
