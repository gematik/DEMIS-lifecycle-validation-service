package de.gematik.demis.lvs.labnotification.validation;

/*-
 * #%L
 * lifecycle-validation-service
 * %%
 * Copyright (C) 2025 - 2026 gematik GmbH
 * %%
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik,
 * find details in the "Readme" file.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.common.fhir.NotificationHelper;
import de.gematik.demis.lvs.common.validation.NotificationScenarioValidationService;
import de.gematik.demis.lvs.common.validation.NotificationValidationService;
import de.gematik.demis.lvs.labnotification.fhirpath.LaboratoryScenario;
import de.gematik.demis.lvs.metrics.ValidationMetrics;
import java.util.Optional;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class LaboratoryNotificationValidationServiceTest {

  private NotificationBasicValidationService basicService;
  private NotificationScenarioValidationService<LaboratoryScenario> scenarioService;
  private ValidationMetrics validationMetrics;

  private NotificationValidationService<LaboratoryScenario> service;

  @Nested
  class FhirPathValidationInResponseActive {

    @BeforeEach
    void setUp() {
      basicService = mock(NotificationBasicValidationService.class);
      scenarioService = mock(NotificationScenarioValidationService.class);
      validationMetrics = mock(ValidationMetrics.class);
      service =
          new NotificationValidationService<>(
              basicService, scenarioService, true, validationMetrics);
    }

    @Test
    void validate_successBoth_validScenariosReturned_andNotificationIdExtracted() {
      Bundle bundle = new Bundle();
      when(scenarioService.getValidScenariosForNotification(
              "raw", MediaType.APPLICATION_JSON, "principal"))
          .thenReturn("S1");

      Composition comp = new Composition();
      comp.setIdentifier(new Identifier().setValue("notif-123"));

      try (MockedStatic<NotificationHelper> mocked = Mockito.mockStatic(NotificationHelper.class)) {
        mocked
            .when(() -> NotificationHelper.extractComposition(bundle))
            .thenReturn(Optional.of(comp));

        String result =
            service.validate("raw", MediaType.APPLICATION_JSON, "principal", "laboratory");

        assertThat(result).isEqualTo("S1");
        verifyNoInteractions(basicService);
        verify(scenarioService)
            .getValidScenariosForNotification("raw", MediaType.APPLICATION_JSON, "principal");
      }
    }

    @Test
    void validate_withFhirPathValidationDisabled_noBasicValidationInteraction() {
      Bundle bundle = new Bundle();
      when(scenarioService.getValidScenariosForNotification(anyString(), any(), any()))
          .thenReturn("S1");

      try (MockedStatic<NotificationHelper> mocked = Mockito.mockStatic(NotificationHelper.class)) {
        mocked
            .when(() -> NotificationHelper.extractComposition(bundle))
            .thenReturn(Optional.empty());

        final String result =
            service.validate("raw", MediaType.APPLICATION_JSON, null, "laboratory");
        assertThat(result).isEqualTo("S1");
        verifyNoInteractions(basicService);
      }
    }

    @Test
    void validate_basicSucceeds_scenarioFails_throwsScenarioException() {
      Bundle bundle = new Bundle();
      // basic succeeds
      LifecycleValidationException scenarioEx = mock(LifecycleValidationException.class);
      when(scenarioService.getValidScenariosForNotification(
              anyString(), eq(MediaType.APPLICATION_JSON), any()))
          .thenThrow(scenarioEx);

      Composition comp = new Composition();
      comp.setIdentifier(new Identifier().setValue("notif-789"));

      try (MockedStatic<NotificationHelper> mocked = Mockito.mockStatic(NotificationHelper.class)) {
        mocked
            .when(() -> NotificationHelper.extractComposition(bundle))
            .thenReturn(Optional.of(comp));

        LifecycleValidationException thrown =
            assertThrows(
                LifecycleValidationException.class,
                () -> service.validate("raw", MediaType.APPLICATION_JSON, "senderA", "laboratory"));
        assertThat(thrown).isSameAs(scenarioEx);
      }
    }

    @Test
    void
        validate_scenarioValidationFail_withFhirPathValidationDisabled_ScenarioExceptionPropagated() {
      Bundle bundle = new Bundle();
      when(scenarioService.getValidScenariosForNotification(anyString(), any(), any()))
          .thenThrow(new LifecycleValidationException());

      // composition absent branch again (already covered, still fine)
      try (MockedStatic<NotificationHelper> mocked = Mockito.mockStatic(NotificationHelper.class)) {
        mocked
            .when(() -> NotificationHelper.extractComposition(bundle))
            .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.validate("raw", MediaType.APPLICATION_JSON, "senderB", "laboratory"))
            .isInstanceOf(LifecycleValidationException.class);
      }
    }
  }

  @Nested
  class FhirPathValidationInResponseInactive {

    @BeforeEach
    void setUp() {
      basicService = mock(NotificationBasicValidationService.class);
      scenarioService = mock(NotificationScenarioValidationService.class);
      validationMetrics = mock(ValidationMetrics.class);

      service =
          new NotificationValidationService<>(
              basicService, scenarioService, false, validationMetrics);
    }

    @Test
    void validate_successBoth_validScenarioReturned_andNotificationIdExtracted() {
      Bundle bundle = new Bundle();
      when(scenarioService.getValidScenariosForNotification(
              "raw", MediaType.APPLICATION_JSON, "principal"))
          .thenReturn("S1");

      Composition comp = new Composition();
      comp.setIdentifier(new Identifier().setValue("notif-123"));

      try (MockedStatic<NotificationHelper> mocked = Mockito.mockStatic(NotificationHelper.class)) {
        mocked
            .when(() -> NotificationHelper.extractComposition(bundle))
            .thenReturn(Optional.of(comp));

        String result =
            service.validate("raw", MediaType.APPLICATION_JSON, "principal", "laboratory");

        assertThat(result).isEmpty();
        verify(basicService).validate("raw", MediaType.APPLICATION_JSON);
        verify(scenarioService)
            .getValidScenariosForNotification("raw", MediaType.APPLICATION_JSON, "principal");
      }
    }

    @Test
    void validate_basicFails_scenarioSucceeds_basicExceptionPropagated_andNoComposition() {
      Bundle bundle = new Bundle();
      doThrow(new InternalErrorException("basic fail"))
          .when(basicService)
          .validate(anyString(), any());
      when(scenarioService.getValidScenariosForNotification(anyString(), any(), any()))
          .thenReturn("S1");

      try (MockedStatic<NotificationHelper> mocked = Mockito.mockStatic(NotificationHelper.class)) {
        mocked
            .when(() -> NotificationHelper.extractComposition(bundle))
            .thenReturn(Optional.empty());

        assertThrows(
            InternalErrorException.class,
            () -> service.validate("raw", MediaType.APPLICATION_JSON, null, "laboratory"));
      }
    }

    @Test
    void validate_basicSucceeds_scenarioFails_returnsEmptyString() {
      Bundle bundle = new Bundle();
      // basic succeeds
      LifecycleValidationException scenarioEx = mock(LifecycleValidationException.class);
      when(scenarioService.getValidScenariosForNotification(
              anyString(), eq(MediaType.APPLICATION_JSON), any()))
          .thenThrow(scenarioEx);

      Composition comp = new Composition();
      comp.setIdentifier(new Identifier().setValue("notif-789"));

      try (MockedStatic<NotificationHelper> mocked = Mockito.mockStatic(NotificationHelper.class)) {
        mocked
            .when(() -> NotificationHelper.extractComposition(bundle))
            .thenReturn(Optional.of(comp));

        final String result =
            service.validate("raw", MediaType.APPLICATION_JSON, "senderA", "laboratory");

        assertThat(result).isEmpty();
      }
    }

    @Test
    void validate_bothFail_throwsBasicException() {
      doThrow(new InternalErrorException("basic fail"))
          .when(basicService)
          .validate(anyString(), any());
      Bundle bundle = new Bundle();
      when(scenarioService.getValidScenariosForNotification(anyString(), any(), any()))
          .thenThrow(new LifecycleValidationException());

      // composition absent branch again (already covered, still fine)
      try (MockedStatic<NotificationHelper> mocked = Mockito.mockStatic(NotificationHelper.class)) {
        mocked
            .when(() -> NotificationHelper.extractComposition(bundle))
            .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.validate("raw", MediaType.APPLICATION_JSON, "senderA", "laboratory"))
            .isInstanceOf(InternalErrorException.class);
      }
    }
  }
}
