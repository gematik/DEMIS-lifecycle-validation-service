package de.gematik.demis.lvs.disease;

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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.common.externalchecks.AdditionalOperationExecuter;
import de.gematik.demis.lvs.common.fhirpath.ScenarioLoader;
import de.gematik.demis.lvs.common.validation.NotificationScenarioValidationService;
import de.gematik.demis.lvs.disease.fhirpath.DiseaseConfigurationProperties;
import de.gematik.demis.lvs.disease.fhirpath.DiseaseScenario;
import de.gematik.demis.lvs.metrics.ValidationMetrics;
import de.gematik.demis.notification.builder.demis.fhir.notification.utils.Compositions;
import de.gematik.demis.notification.builder.demis.fhir.notification.utils.Conditions;
import de.gematik.demis.notification.builder.demis.fhir.notification.utils.DemisConstants;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class DiseaseNotificationLifecycleValidationSrvTest {

  private final FhirParser fhirParser = new FhirParser(FhirContext.forR4Cached());
  private List<DiseaseScenario> scenarios;
  private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
  @Mock private AdditionalOperationExecuter additionalOperationExecuter;

  static Stream<Arguments> scenarioNames() {
    return Stream.of(
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V-11.json",
            "S_FM_V2V"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V-12.json",
            "S_FM_V2V"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V-21.json",
            "S_FM_V2V"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V-12.json",
            "S_FM_V2V"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2I-11.json",
            "S_FM_V2I"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2I-12.json",
            "S_FM_V2I"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2I-21.json",
            "S_FM_V2I"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2I-22.json",
            "S_FM_V2I"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E-11.json",
            "S_FM_V2E"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E-12.json",
            "S_FM_V2E"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E-21.json",
            "S_FM_V2E"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E-22.json",
            "S_FM_V2E"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2E-11.json",
            "S_FM_E2E"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2E-12.json",
            "S_FM_E2E"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2E-21.json",
            "S_FM_E2E"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2E-22.json",
            "S_FM_E2E"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2I-11.json",
            "S_FM_E2I"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2I-12.json",
            "S_FM_E2I"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2I-21.json",
            "S_FM_E2I"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2I-22.json",
            "S_FM_E2I"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2V-1.json",
            "S_FM_T2V"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2V-2.json",
            "S_FM_T2V"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/RUND.json", "S_FM_V2E"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/no_common_questionnaire.json",
            "S_FM_V2E"));
  }

  @BeforeEach
  void setUp() {
    DiseaseConfigurationProperties properties =
        new DiseaseConfigurationProperties(
            "configuration/diseaseScenarios.json", "configuration/keyToFhirPath.json", true);
    scenarios =
        ScenarioLoader.loadScenarios(
            properties.fhirPathData(), properties.keyToFhirPathData(), DiseaseScenario[].class);
  }

  @Test
  void shouldCreateDataOnBuildAndValidateNotificationsRegression() throws IOException {
    String diseaseExample =
        getString("src/test/resources/notifications/disease/scenarioExamples/S_FM_E2E-11.json");

    when(additionalOperationExecuter.checkAllExternalChecks(
            any(Bundle.class), any(DiseaseScenario.class), any(FhirPathR4.class)))
        .thenReturn(true);

    NotificationScenarioValidationService<DiseaseScenario>
        diseaseNotificationLifecycleValidationSrv =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    final String validate =
        diseaseNotificationLifecycleValidationSrv.getValidScenariosForNotification(
            diseaseExample, MediaType.APPLICATION_JSON, null);

    assertThat(validate).contains("S_FM_E2E");
  }

  @Test
  void shouldThrowException() throws IOException {

    String diseaseExample =
        getString(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V_not_valid.json");

    NotificationScenarioValidationService<DiseaseScenario>
        diseaseNotificationLifecycleValidationSrv =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    assertThatThrownBy(
            () ->
                diseaseNotificationLifecycleValidationSrv.getValidScenariosForNotification(
                    diseaseExample, MediaType.APPLICATION_JSON, null))
        .isInstanceOf(LifecycleValidationException.class);
  }

  @Test
  void shouldCreateDataOnBuildAndValidateNotificationsAlternativeValidateMethodRegression()
      throws IOException {

    String diseaseExample =
        getString("src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E-11.json");

    when(additionalOperationExecuter.checkAllExternalChecks(
            any(Bundle.class), any(DiseaseScenario.class), any(FhirPathR4.class)))
        .thenReturn(true);

    NotificationScenarioValidationService<DiseaseScenario>
        diseaseNotificationLifecycleValidationSrv =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    final String validate =
        diseaseNotificationLifecycleValidationSrv.getValidScenariosForNotification(
            diseaseExample, MediaType.APPLICATION_JSON, null);

    assertThat(validate).isEqualTo("S_FM_V2E");
  }

  @ParameterizedTest
  @MethodSource("scenarioNames")
  void shouldReturnOnlyOneScenario(String path, String expectedScenarios) throws IOException {

    when(additionalOperationExecuter.checkAllExternalChecks(
            any(Bundle.class), any(DiseaseScenario.class), any(FhirPathR4.class)))
        .thenReturn(true);

    String diseaseExample = getString(path);

    NotificationScenarioValidationService<DiseaseScenario>
        diseaseNotificationLifecycleValidationSrv =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    final String validate =
        diseaseNotificationLifecycleValidationSrv.getValidScenariosForNotification(
            diseaseExample, MediaType.APPLICATION_JSON, null);

    assertThat(validate).isNotNull().isInstanceOf(String.class).isEqualTo(expectedScenarios);
  }

  @ParameterizedTest
  @MethodSource("clinicalStatusTestBundles")
  void thatInvalidStatusCodeMattersForNonNominalNotifications(@Nonnull final String bundlePath)
      throws IOException {
    final String bundleAsString = getString(bundlePath);
    Bundle bundle =
        FhirContext.forR4Cached().newJsonParser().parseResource(Bundle.class, bundleAsString);
    setInvalidClinicalStatusCode(bundle);
    final String testBundle =
        FhirContext.forR4Cached().newJsonParser().encodeResourceToString(bundle);

    NotificationScenarioValidationService<DiseaseScenario>
        diseaseNotificationLifecycleValidationSrv =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    assertThatExceptionOfType(LifecycleValidationException.class)
        .isThrownBy(
            () ->
                diseaseNotificationLifecycleValidationSrv.getValidScenariosForNotification(
                    testBundle, MediaType.APPLICATION_JSON, null));
  }

  void thatMissingClinicalStatusDoesNotMatterForNominalNotifications() throws IOException {
    final String testBundleString =
        getString("src/test/resources/notifications/disease/MissingClinicalStatus.json");
    Bundle testBundle =
        FhirContext.forR4Cached().newJsonParser().parseResource(Bundle.class, testBundleString);
    removeClinicalStatusCode(testBundle);
    final String preppedTestBundle =
        FhirContext.forR4Cached().newJsonParser().encodeResourceToString(testBundle);
    final NotificationScenarioValidationService<DiseaseScenario>
        diseaseNotificationLifecycleValidationSrv =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    final String actual =
        diseaseNotificationLifecycleValidationSrv.getValidScenariosForNotification(
            preppedTestBundle, MediaType.APPLICATION_JSON, null);
    assertThat(actual).isEqualTo("S_FM_E2T-2");
  }

  @ParameterizedTest
  @MethodSource("clinicalStatusTestBundles")
  void thatClinicalStatusDoesMatterForNonNominalNotifications(@Nonnull final String bundlePath)
      throws IOException {
    // GIVEN a test Bundle
    final String testBundleString = getString(bundlePath);
    Bundle testBundle =
        FhirContext.forR4Cached().newJsonParser().parseResource(Bundle.class, testBundleString);
    // AND the Bundle is non nominal
    setNonNominalBundleProfile(testBundle);
    // AND contains an invalid status code
    setInvalidClinicalStatusCode(testBundle);
    final String preppedBundleString =
        FhirContext.forR4Cached().newJsonParser().encodeResourceToString(testBundle);

    final NotificationScenarioValidationService<DiseaseScenario>
        diseaseNotificationLifecycleValidationSrv =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    assertThatExceptionOfType(LifecycleValidationException.class)
        .isThrownBy(
            () ->
                diseaseNotificationLifecycleValidationSrv.getValidScenariosForNotification(
                    preppedBundleString, MediaType.APPLICATION_JSON, null));
  }

  /** Bundles with some extra validation rules related to their clinicalStatus */
  @Nonnull
  private static Stream<Arguments> clinicalStatusTestBundles() {
    return Stream.of(
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2E-11.json",
            "S_FM_E2E"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2V-1.json",
            "S_FM_T2V"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E-11.json",
            "S_FM_V2E"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2I-11.json",
            "S_FM_V2I"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V-11.json",
            "S_FM_V2V"));
  }

  private static void removeClinicalStatusCode(@Nonnull final Bundle testBundle) {
    final Composition composition = Compositions.from(testBundle).orElseThrow();
    final Condition condition = Conditions.from(composition).orElseThrow();
    condition.setClinicalStatus(null);
  }

  private static void setNonNominalBundleProfile(@Nonnull final Bundle testBundle) {
    testBundle
        .getMeta()
        .setProfile(
            List.of(
                new CanonicalType(DemisConstants.PROFILE_NOTIFICATION_BUNDLE_DISEASE_NON_NOMINAL)));
  }

  private static void setInvalidClinicalStatusCode(@Nonnull final Bundle testBundle) {
    final Composition composition = Compositions.from(testBundle).orElseThrow();
    final Condition condition = Conditions.from(composition).orElseThrow();
    condition.setClinicalStatus(
        new CodeableConcept(new Coding("any system", "invalid code", "any display")));
  }

  private String getString(final String path) throws IOException {
    return Files.readString(Paths.get(path));
  }
}
