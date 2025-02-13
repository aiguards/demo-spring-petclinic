package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
        pet.setOwner(owner);
    }

    @Test
    void testPopulatePetTypes() {
        Collection<PetType> petTypes = new ArrayList<>();
        PetType dog = new PetType();
        dog.setName("Dog");
        petTypes.add(dog);

        given(owners.findPetTypes()).willReturn(petTypes);

        Collection<PetType> result = petController.populatePetTypes();
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getName()).isEqualTo("Dog");
    }

    @Test
    void testFindOwner() {
        given(owners.findById(1)).willReturn(Optional.of(owner));

        Owner result = petController.findOwner(1);
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
    }

    @Test
    void testFindOwnerNotFound() {
        given(owners.findById(99)).willReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> petController.findOwner(99));
    }

    @Test
    void testFindPet() {
        owner.addPet(pet);
        given(owners.findById(1)).willReturn(Optional.of(owner));

        Pet result = petController.findPet(1, 1);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Max");
    }

    @Test
    void testFindPetWithNullId() {
        assertThrows(IllegalArgumentException.class, () -> petController.findPet(1, null));
    }

    @Test
    void testInitCreationForm() {
        String view = petController.initCreationForm(owner, modelMap);
        assertThat(view).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessCreationFormSuccess() {
        given(bindingResult.hasErrors()).willReturn(false);

        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(view).isEqualTo("redirect:/owners/{ownerId}");
        verify(owners).save(any(Owner.class));
        verify(redirectAttributes).addFlashAttribute("message", "New Pet has been Added");
    }

    @Test
    void testProcessCreationFormWithErrors() {
        given(bindingResult.hasErrors()).willReturn(true);

        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(view).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessCreationFormWithFutureBirthDate() {
        pet.setBirthDate(LocalDate.now().plusDays(1));

        String view = petController.processCreationForm(owner, pet, bindingResult, redirectAttributes);
        
        verify(bindingResult).rejectValue("birthDate", "typeMismatch.birthDate");
        assertThat(view).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testInitUpdateForm() {
        String view = petController.initUpdateForm();
        assertThat(view).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessUpdateFormSuccess() {
        given(bindingResult.hasErrors()).willReturn(false);

        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(view).isEqualTo("redirect:/owners/{ownerId}");
        verify(owners).save(any(Owner.class));
        verify(redirectAttributes).addFlashAttribute("message", "Pet details has been edited");
    }

    @Test
    void testProcessUpdateFormWithErrors() {
        given(bindingResult.hasErrors()).willReturn(true);

        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        assertThat(view).isEqualTo("pets/createOrUpdatePetForm");
    }

    @Test
    void testProcessUpdateFormWithDuplicateName() {
        Pet existingPet = new Pet();
        existingPet.setId(2);
        existingPet.setName("Max");
        owner.addPet(existingPet);

        String view = petController.processUpdateForm(owner, pet, bindingResult, redirectAttributes);
        
        verify(bindingResult).rejectValue("name", "duplicate", "already exists");
        assertThat(view).isEqualTo("pets/createOrUpdatePetForm");
    }
}
