package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
        pet.setType(new PetType());
    }

    @Test
    void testPopulatePetTypes() {
        // Given
        Collection<PetType> petTypes = new ArrayList<>();
        when(owners.findPetTypes()).thenReturn(petTypes);

        // When
        Collection<PetType> result = petController.populatePetTypes();

        // Then
        assertThat(result).isEqualTo(petTypes);
    }

    @Test
    void testFindOwner() {
        // Given
        when(owners.findById(1)).thenReturn(Optional.of(owner));

        // When
        Owner result = petController.findOwner(1);

        // Then
        assertThat(result).isEqualTo(owner);
    }

    @Test
    void testFindOwnerNotFound() {
        // Given
        when(owners.findById(anyInt())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> petController.findOwner(999));
    }

    @Test
    void testFindPetNew() {
        // When
        Pet result = petController.findPet(1, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
    }

    @Test
    void testFindPetExisting() {
        // Given
        owner.addPet(pet);
        when(owners.findById(1)).thenReturn(Optional.of(owner));

        // When
        Pet result = petController.findPet(1, 1);

        // Then
        assertThat(result).isEqualTo(pet);
    }

    @Test
    void testInitCreationForm() {
        // When
        String viewName = petController.initCreationForm(owner, modelMap);

        // Then
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
        assertThat(owner.getPets()).hasSize(1);
    }

    @Test
    void testProcessCreationFormSuccess() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);

        // When
        String viewName = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/owners/{ownerId}");
        verify(owners).save(any(Owner.class));
        verify(redirectAttributes).addFlashAttribute("message", "New Pet has been Added");
    }

    @Test
    void testProcessCreationFormWithFutureDate() {
        // Given
        pet.setBirthDate(LocalDate.now().plusDays(1));

        // When
        String viewName = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);

        // Then
        verify(bindingResult).rejectValue("birthDate", "typeMismatch.birthDate");
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessCreationFormWithDuplicateName() {
        // Given
        Pet existingPet = new Pet();
        existingPet.setName("Max");
        owner.addPet(existingPet);

        // When
        String viewName = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);

        // Then
        verify(bindingResult).rejectValue("name", "duplicate", "already exists");
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testInitUpdateForm() {
        // When
        String viewName = petController.initUpdateForm();

        // Then
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessUpdateFormSuccess() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);

        // When
        String viewName = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);

        // Then
        assertThat(viewName).isEqualTo("redirect:/owners/{ownerId}");
        verify(owners).save(any(Owner.class));
        verify(redirectAttributes).addFlashAttribute("message", "Pet details has been edited");
    }

    @Test
    void testProcessUpdateFormWithFutureDate() {
        // Given
        pet.setBirthDate(LocalDate.now().plusDays(1));

        // When
        String viewName = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);

        // Then
        verify(bindingResult).rejectValue("birthDate", "typeMismatch.birthDate");
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessUpdateFormWithDuplicateName() {
        // Given
        Pet existingPet = new Pet();
        existingPet.setId(2);
        existingPet.setName("Max");
        owner.addPet(existingPet);

        // When
        String viewName = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);

        // Then
        verify(bindingResult).rejectValue("name", "duplicate", "already exists");
        assertThat(viewName).isEqualTo("pets/createOrUpdatePetForm");
    }
}
