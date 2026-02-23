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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.FhirPathExecutionException;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.common.externalchecks.AdditionalOperationExecuter;
import de.gematik.demis.lvs.common.fhirpath.FhirPathExecutor;
import de.gematik.demis.lvs.common.fhirpath.Scenario;
import de.gematik.demis.lvs.common.fhirpath.ScenarioLoader;
import de.gematik.demis.lvs.common.validation.NotificationScenarioValidationService;
import de.gematik.demis.lvs.labnotification.fhirpath.LaboratoryConfigurationProperties;
import de.gematik.demis.lvs.labnotification.fhirpath.LaboratoryScenario;
import de.gematik.demis.lvs.metrics.ValidationMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class NotificationScenarioValidationServiceTest {

  private static final FhirParser fhirParser = new FhirParser(FhirContext.forR4Cached());
  private static NotificationScenarioValidationService<LaboratoryScenario>
      notificationScenarioValidationService;
  private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
  @Mock private AdditionalOperationExecuter additionalOperationExecuter;

  static Stream<Arguments> scenarioNames() {
    return Stream.of(
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/M_POS.json", "M_POS"),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/FM_NEG.json", "EM_NEG"),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/aFM_POS.json", "aFM_POS"),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/aFM_NEG.json", "aFM_NEG"),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/EM_NEG.json", "EM_NEG"),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/IM_NEG_CVDP.json",
            "IM_NEG_CVDP"));
  }

  static Stream<String> scenarioNamesInvalid() {
    return Stream.of(
        "src/test/resources/notifications/laboratory/scenarioExamples/invalid/IM_NEG.json",
        "src/test/resources/notifications/laboratory/scenarioExamples/invalid/IM_NEG-otherCategoryThanCvdp.json",
        "src/test/resources/notifications/laboratory/scenarioExamples/invalid/IM_NEG_CVDP-withRelatesTo.json");
  }

  @BeforeEach
  void setup() {
    LaboratoryConfigurationProperties properties =
        new LaboratoryConfigurationProperties(
            "configuration/laboratoryScenarios.json", "configuration/keyToFhirPath.json", true);
    List<LaboratoryScenario> scenarios =
        ScenarioLoader.loadScenarios(
            properties.fhirPathData(), properties.keyToFhirPathData(), LaboratoryScenario[].class);

    notificationScenarioValidationService =
        new NotificationScenarioValidationService<>(
            FhirContext.forR4Cached(),
            scenarios,
            new ValidationMetrics(meterRegistry),
            additionalOperationExecuter,
            fhirParser);
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("scenarioNames")
  void shouldReturnScenarioNameForEachExample(String notificationPath, String expectedScenarios) {

    final String notification = Files.readString(Path.of(notificationPath));

    when(additionalOperationExecuter.checkAllExternalChecks(
            any(), any(Scenario.class), any(FhirPathR4.class)))
        .thenReturn(true);
    String validatedScenario =
        notificationScenarioValidationService.getValidScenariosForNotification(
            notification, MediaType.APPLICATION_JSON, null);
    assertThat(validatedScenario).isEqualTo(expectedScenarios);
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("scenarioNamesInvalid")
  void shouldThrowLifecycleValidationErrorForEachExample(String notificationPath) {

    final String notification = Files.readString(Path.of(notificationPath));

    assertThatThrownBy(
            () ->
                notificationScenarioValidationService.getValidScenariosForNotification(
                    notification, MediaType.APPLICATION_JSON, null))
        .hasMessageContaining("No valid lifecycle scenario found");

    verify(additionalOperationExecuter, never()).checkAllExternalChecks(any(), any(), any());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "src/test/resources/notifications/laboratory/nonnominal-notifiedperson.json",
        "src/test/resources/notifications/laboratory/anonymous.json",
        "src/test/resources/notifications/laboratory/negative_covid19_notification_Dv2.json"
      })
  void shouldNotThrowErrorForEachExample(String notificationPath) throws IOException {
    final String notification = Files.readString(Path.of(notificationPath));
    when(additionalOperationExecuter.checkAllExternalChecks(
            any(), any(Scenario.class), any(FhirPathR4.class)))
        .thenReturn(true);
    Assertions.assertDoesNotThrow(
        () ->
            notificationScenarioValidationService.getValidScenariosForNotification(
                notification, MediaType.APPLICATION_JSON, null));
  }

  @Test
  void shouldThrowErrorWhenFhirPathExecutionException() {
    String validJson = "{\"resourceType\":\"Bundle\"}";
    try (final MockedStatic<FhirPathExecutor> mockedExecutor =
        Mockito.mockStatic(FhirPathExecutor.class)) {
      mockedExecutor
          .when(() -> FhirPathExecutor.isAtLeastOneFhirPathExpressionInvalid(any(), any(), any()))
          .thenReturn(false);

      when(additionalOperationExecuter.checkAllExternalChecks(
              any(), any(Scenario.class), any(FhirPathR4.class)))
          .thenThrow(new FhirPathExecutionException("just a mocked throw"));

      assertThatThrownBy(
              () ->
                  notificationScenarioValidationService.getValidScenariosForNotification(
                      validJson, MediaType.APPLICATION_JSON, "somePrincipalId"))
          .isInstanceOf(LifecycleValidationException.class)
          .hasMessage("Error evaluating FhirPath expression");
    }
  }

  @Test
  void shouldThrowLifecycleValidationException() {
    String validJson = "{\"resourceType\":\"Bundle\"}";

    LaboratoryConfigurationProperties properties =
        new LaboratoryConfigurationProperties(
            "configuration/laboratoryScenarios.json", "configuration/keyToFhirPath.json", true);
    List<LaboratoryScenario> scenarios =
        ScenarioLoader.loadScenarios(
            properties.fhirPathData(), properties.keyToFhirPathData(), LaboratoryScenario[].class);

    ValidationMetrics validationMetrics = mock(ValidationMetrics.class);
    notificationScenarioValidationService =
        new NotificationScenarioValidationService<>(
            FhirContext.forR4Cached(),
            scenarios,
            validationMetrics,
            additionalOperationExecuter,
            fhirParser);

    assertThatThrownBy(
            () ->
                notificationScenarioValidationService.getValidScenariosForNotification(
                    validJson, MediaType.APPLICATION_JSON, "somePrincipalId"))
        .isInstanceOf(LifecycleValidationException.class)
        .hasMessage("No valid lifecycle scenario found");

    verify(validationMetrics)
        .incUnsuccessfulValidationCount("somePrincipalId", "LifecycleValidationException");

    assertThatThrownBy(
            () ->
                notificationScenarioValidationService.getValidScenariosForNotification(
                    validJson, MediaType.APPLICATION_JSON, null))
        .isInstanceOf(LifecycleValidationException.class)
        .hasMessage("No valid lifecycle scenario found");

    verify(validationMetrics)
        .incUnsuccessfulValidationCount("<unknown>", "LifecycleValidationException");
  }
}
