package de.gematik.demis.lvs.common.validation;

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

import static de.gematik.demis.lvs.disease.DiseaseBasicNotificationLifecycleValidationSrv.NO_VALID_LIFECYCLE_SCENARIO_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.disease.DiseaseBasicNotificationLifecycleValidationSrv;
import de.gematik.demis.lvs.labnotification.validation.NotificationBasicValidationService;
import de.gematik.demis.lvs.metrics.ValidationMetrics;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class NotificationValidationServiceTest {

  @Mock DiseaseBasicNotificationLifecycleValidationSrv basicDiseaseValidationService;
  @Mock NotificationBasicValidationService basicLaboratoryValidationService;

  @Mock NotificationScenarioValidationService laboratoryScenarioValidationService;
  @Mock NotificationScenarioValidationService diseaseScenarioValidationService;
  @Mock ValidationMetrics validationMetricsMock;

  NotificationValidationService diseaseValidationService;

  @Nested
  class ScenarioValidationTests {

    @BeforeEach
    void setUp() {
      diseaseValidationService =
          new NotificationValidationService(
              basicDiseaseValidationService,
              diseaseScenarioValidationService,
              true,
              validationMetricsMock);
    }

    @SneakyThrows
    @Test
    void shouldReturnScenarioValidationResult_Valid() {
      String diseaseExample = getDiseaseExampleString();
      when(diseaseScenarioValidationService.getValidScenariosForNotification(any(), any(), any()))
          .thenReturn("S_IM_V");

      String resultScenario =
          diseaseValidationService.validate(
              diseaseExample, MediaType.APPLICATION_JSON, null, "disease");

      assertThat(resultScenario).isEqualTo("S_IM_V");
    }

    @SneakyThrows
    @Test
    void shouldReturnScenarioValidationResult_Invalid() {
      String diseaseExample = getDiseaseExampleString();
      when(diseaseScenarioValidationService.getValidScenariosForNotification(any(), any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));

      assertThatThrownBy(
              () ->
                  diseaseValidationService.validate(
                      diseaseExample, MediaType.APPLICATION_JSON, null, "disease"))
          .isInstanceOf(LifecycleValidationException.class)
          .hasMessageContaining("No valid lifecycle scenario found");
    }
  }

  @Nested
  class BasicValidationTests {

    @BeforeEach
    void setUp() {
      diseaseValidationService =
          new NotificationValidationService(
              basicDiseaseValidationService,
              diseaseScenarioValidationService,
              false,
              validationMetricsMock);
    }

    @SneakyThrows
    @Test
    void shouldReturnScenarioValidationResult_BothTrue() {
      String diseaseExample = getDiseaseExampleString();
      when(basicDiseaseValidationService.validate(any(), any())).thenReturn("");
      when(diseaseScenarioValidationService.getValidScenariosForNotification(any(), any(), any()))
          .thenReturn("S_IM_V");

      String resultScenario =
          diseaseValidationService.validate(
              diseaseExample, MediaType.APPLICATION_JSON, null, "disease");

      assertThat(resultScenario).isEmpty();
    }

    @SneakyThrows
    @Test
    void shouldReturnScenarioValidationResult_ScenarioTrue_BasicFalse() {
      String diseaseExample = getDiseaseExampleString();
      when(basicDiseaseValidationService.validate(any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));
      when(diseaseScenarioValidationService.getValidScenariosForNotification(any(), any(), any()))
          .thenReturn("S_IM_V");

      assertThatThrownBy(
              () ->
                  diseaseValidationService.validate(
                      diseaseExample, MediaType.APPLICATION_JSON, null, "disease"))
          .isInstanceOf(LifecycleValidationException.class)
          .hasMessageContaining("No valid lifecycle scenario found");
    }

    @SneakyThrows
    @Test
    void shouldReturnScenarioValidationResult_ScenarioFalse_BasicTrue() {
      String diseaseExample = getDiseaseExampleString();
      when(basicDiseaseValidationService.validate(any(), any())).thenReturn("");
      when(diseaseScenarioValidationService.getValidScenariosForNotification(any(), any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));

      String resultScenario =
          diseaseValidationService.validate(
              diseaseExample, MediaType.APPLICATION_JSON, null, "disease");

      assertThat(resultScenario).isEmpty();
    }

    @SneakyThrows
    @Test
    void shouldReturnScenarioValidationResult_BothFalse() {
      String diseaseExample = getDiseaseExampleString();
      when(basicDiseaseValidationService.validate(any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));
      when(diseaseScenarioValidationService.getValidScenariosForNotification(any(), any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));

      assertThatThrownBy(
              () ->
                  diseaseValidationService.validate(
                      diseaseExample, MediaType.APPLICATION_JSON, null, "disease"))
          .isInstanceOf(LifecycleValidationException.class)
          .hasMessageContaining("No valid lifecycle scenario found");
    }
  }

  @Nested
  @SpringBootTest
  class DiseaseMetricTests {

    @BeforeEach
    void setUp() {
      diseaseValidationService =
          new NotificationValidationService(
              basicDiseaseValidationService,
              diseaseScenarioValidationService,
              false,
              validationMetricsMock);
    }

    @SneakyThrows
    @Test
    void shouldCreateDiseaseMetric_equalSuccess() {
      String diseaseExample = getDiseaseExampleString();
      when(diseaseScenarioValidationService.getValidScenariosForNotification(any(), any(), any()))
          .thenReturn("S_IM_V");

      when(basicDiseaseValidationService.validate(any(), any())).thenReturn("S_IM_V");

      diseaseValidationService.validate(
          diseaseExample, MediaType.APPLICATION_JSON, null, "disease");

      verify(validationMetricsMock).countDisValResult(true, true);
    }

    @SneakyThrows
    @Test
    void shouldCreateDiseaseMetric_equalFail() {
      String diseaseExample = getDiseaseExampleString();
      when(diseaseScenarioValidationService.getValidScenariosForNotification(any(), any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));

      when(basicDiseaseValidationService.validate(any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));

      assertThatThrownBy(
              () ->
                  diseaseValidationService.validate(
                      diseaseExample, MediaType.APPLICATION_JSON, null, "disease"))
          .isInstanceOf(LifecycleValidationException.class)
          .hasMessageContaining("No valid lifecycle scenario found");

      verify(validationMetricsMock).countDisValResult(false, false);
    }

    @SneakyThrows
    @Test
    void shouldCreateDiseaseMetric_unequal_legacySuccess() {
      String diseaseExample = getDiseaseExampleString();
      when(diseaseScenarioValidationService.getValidScenariosForNotification(any(), any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));

      when(basicDiseaseValidationService.validate(any(), any())).thenReturn("S_IM_V");

      diseaseValidationService.validate(
          diseaseExample, MediaType.APPLICATION_JSON, null, "disease");

      verify(validationMetricsMock).countDisValResult(true, false);
    }

    @SneakyThrows
    @Test
    void shouldCreateDiseaseMetric_unequal_legacyFail() {
      String diseaseExample = getDiseaseExampleString();
      when(diseaseScenarioValidationService.getValidScenariosForNotification(any(), any(), any()))
          .thenReturn("S_IM_V");

      when(basicDiseaseValidationService.validate(any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));

      assertThatThrownBy(
              () ->
                  diseaseValidationService.validate(
                      diseaseExample, MediaType.APPLICATION_JSON, null, "disease"))
          .isInstanceOf(LifecycleValidationException.class)
          .hasMessageContaining("No valid lifecycle scenario found");

      verify(validationMetricsMock).countDisValResult(false, true);
    }
  }

  @Nested
  @SpringBootTest
  class LaboratoryMetricTests {
    NotificationValidationService laboratoryValidationService;

    @BeforeEach
    void setUp() {

      laboratoryValidationService =
          new NotificationValidationService(
              basicLaboratoryValidationService,
              laboratoryScenarioValidationService,
              false,
              validationMetricsMock);
    }

    @SneakyThrows
    @Test
    void shouldCreateLaboratoryMetric_equalSuccess() {
      String laboratoryExampleString = getLaboratoryExampleString();
      when(laboratoryScenarioValidationService.getValidScenariosForNotification(
              any(), any(), any()))
          .thenReturn("M_POS");

      when(basicLaboratoryValidationService.validate(any(), any())).thenReturn("M_POS");

      laboratoryValidationService.validate(
          laboratoryExampleString, MediaType.APPLICATION_JSON, null, "laboratory");

      verify(validationMetricsMock).countLabValResult(true, true);
    }

    @SneakyThrows
    @Test
    void shouldCreateLaboratoryMetric_equalFail() {
      String laboratoryExampleString = getLaboratoryExampleString();
      when(laboratoryScenarioValidationService.getValidScenariosForNotification(
              any(), any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));

      when(basicLaboratoryValidationService.validate(any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));

      assertThatThrownBy(
              () ->
                  laboratoryValidationService.validate(
                      laboratoryExampleString, MediaType.APPLICATION_JSON, null, "laboratory"))
          .isInstanceOf(LifecycleValidationException.class)
          .hasMessageContaining("No valid lifecycle scenario found");

      verify(validationMetricsMock).countLabValResult(false, false);
    }

    @SneakyThrows
    @Test
    void shouldCreateLaboratoryMetric_unequal_legacySuccess() {
      String laboratoryExampleString = getLaboratoryExampleString();
      when(laboratoryScenarioValidationService.getValidScenariosForNotification(
              any(), any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));

      when(basicLaboratoryValidationService.validate(any(), any())).thenReturn("M_POS");

      laboratoryValidationService.validate(
          laboratoryExampleString, MediaType.APPLICATION_JSON, null, "laboratory");

      verify(validationMetricsMock).countLabValResult(true, false);
    }

    @SneakyThrows
    @Test
    void shouldCreateLaboratoryMetric_unequal_legacyFail() {
      String laboratoryExampleString = getLaboratoryExampleString();
      when(laboratoryScenarioValidationService.getValidScenariosForNotification(
              any(), any(), any()))
          .thenReturn("M_POS");

      when(basicLaboratoryValidationService.validate(any(), any()))
          .thenThrow(new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND));

      assertThatThrownBy(
              () ->
                  laboratoryValidationService.validate(
                      laboratoryExampleString, MediaType.APPLICATION_JSON, null, "laboratory"))
          .isInstanceOf(LifecycleValidationException.class)
          .hasMessageContaining("No valid lifecycle scenario found");

      verify(validationMetricsMock).countLabValResult(false, true);
    }
  }

  private String getLaboratoryExampleString() throws IOException {
    String path = "src/test/resources/notifications/laboratory/scenarioExamples/M_POS.json";
    return Files.readString(Paths.get(path));
  }

  private String getDiseaseExampleString() throws IOException {
    String path = "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V.json";
    return Files.readString(Paths.get(path));
  }
}
