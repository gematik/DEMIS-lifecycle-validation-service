package de.gematik.demis.lvs.disease;

/*-
 * #%L
 * lifecycle-validation-service
 * %%
 * Copyright (C) 2025 gematik GmbH
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
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.fhirparserlibrary.MessageType;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.disease.fhirpath.DiseaseConfiguration;
import de.gematik.demis.lvs.disease.fhirpath.DiseaseConfigurationProperties;
import de.gematik.demis.lvs.disease.fhirpath.DiseaseScenario;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

class DiseaseNotificationLifecycleValidationSrvTest {

  private final FhirParser fhirParser = new FhirParser(FhirContext.forR4Cached());
  private List<DiseaseScenario> scenarios;
  private DiseaseConfigurationProperties properties;

  static Stream<Arguments> scenarioNames() {
    return Stream.of(
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E.json",
            List.of("S_FM_V2E", "S_IM_E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V-11.json",
            List.of("S_IM_V", "S_FM_V2V")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V-12.json",
            List.of("S_FM_V2V")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V-21.json",
            List.of("S_FM_V2V")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V-12.json",
            List.of("S_FM_V2V")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_IM_V.json",
            List.of("S_IM_V", "S_FM_V2V")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2I-11.json",
            List.of("S_FM_V2I")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2I-12.json",
            List.of("S_FM_V2I")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2I-21.json",
            List.of("S_FM_V2I")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2I-22.json",
            List.of("S_FM_V2I")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E-11.json",
            List.of("S_FM_V2E", "S_IM_E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E-12.json",
            List.of("S_FM_V2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E-21.json",
            List.of("S_FM_V2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E-22.json",
            List.of("S_FM_V2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2T-1.json",
            List.of("S_FM_V2T", "S_FM_V2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2T-2.json",
            List.of("S_FM_V2T", "S_FM_V2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_IM_E.json",
            List.of("S_IM_E", "S_FM_V2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2E-11.json",
            List.of("S_FM_E2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2E-12.json",
            List.of("S_FM_E2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2E-21.json",
            List.of("S_FM_E2E", "S_FM_T2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2E-22.json",
            List.of("S_FM_E2E", "S_FM_T2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2I-11.json",
            List.of("S_FM_E2I", "S_FM_T2I_1")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2I-12.json",
            List.of("S_FM_E2I", "S_FM_T2I_1")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2I-21.json",
            List.of("S_FM_E2I", "S_FM_T2I_1", "S_FM_T2I_2")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2I-22.json",
            List.of("S_FM_E2I", "S_FM_T2I_1", "S_FM_T2I_2")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2T-1.json",
            List.of("S_FM_E2T", "S_FM_E2E", "S_FM_T2T")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_E2T-2.json",
            List.of("S_FM_E2T", "S_FM_E2E", "S_FM_T2T")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2V-1.json",
            List.of("S_FM_T2V")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2V-2.json",
            List.of("S_FM_T2V")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_IM_T.json",
            List.of("S_IM_T", "S_FM_V2E", "S_IM_E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2T-11.json",
            List.of("S_FM_T2T", "S_FM_E2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2T-12.json",
            List.of("S_FM_T2T", "S_FM_E2E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2T-21.json",
            List.of("S_FM_T2T", "S_FM_E2E", "S_FM_E2T")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2T-22.json",
            List.of("S_FM_T2T", "S_FM_E2E", "S_FM_E2T")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2I_1-11.json",
            List.of("S_FM_T2I_1", "S_FM_E2I")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2I_1-12.json",
            List.of("S_FM_T2I_1", "S_FM_E2I")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2I_1-21.json",
            List.of("S_FM_T2I_1", "S_FM_E2I", "S_FM_T2I_2")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2I_1-22.json",
            List.of("S_FM_T2I_1", "S_FM_E2I", "S_FM_T2I_2")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2I_2-1.json",
            List.of("S_FM_T2I_2", "S_FM_T2I_1", "S_FM_E2I")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_T2I_2-2.json",
            List.of("S_FM_T2I_2", "S_FM_T2I_1", "S_FM_E2I")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/RUND.json",
            List.of("S_FM_V2E", "S_IM_E")),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/no_common_questionnaire.json",
            List.of("S_FM_V2E", "S_FM_V2T", "S_IM_E", "S_IM_T")));
  }

  @BeforeEach
  void setUp() {
    DiseaseConfiguration configuration = new DiseaseConfiguration();
    properties = new DiseaseConfigurationProperties("configuration/diseaseScenarios.json", true);
    scenarios = configuration.loadDiseaseScenarios(properties);
  }

  @Test
  void shouldCreateDataOnBuildAndValidateNotificationsRegression() throws IOException {

    File file = new File("src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E.json");
    FileInputStream inputStream = new FileInputStream(file);
    String jsonString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    inputStream.close();

    Bundle diseaseExample = fhirParser.parseBundleOrParameter(jsonString, MessageType.JSON);

    DiseaseNotificationLifecycleValidationSrv diseaseNotificationLifecycleValidationSrv =
        new DiseaseNotificationLifecycleValidationSrv(
            "notifications/disease/diseaseConfiguration.json",
            properties,
            fhirParser,
            new ObjectMapper(),
            FhirContext.forR4Cached(),
            scenarios,
            true);

    diseaseNotificationLifecycleValidationSrv.init();

    List<String> validate = diseaseNotificationLifecycleValidationSrv.validate(diseaseExample);

    assertThat(validate).containsExactlyInAnyOrder("S_FM_V2E", "S_IM_E");
  }

  @Test
  void shouldThrowException() throws IOException {

    File file =
        new File("src/test/resources/notifications/disease/scenarioExamples/S_IM_V_not_valid.json");
    FileInputStream inputStream = new FileInputStream(file);
    String jsonString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    inputStream.close();

    Bundle diseaseExample = fhirParser.parseBundleOrParameter(jsonString, MessageType.JSON);

    DiseaseNotificationLifecycleValidationSrv diseaseNotificationLifecycleValidationSrv =
        new DiseaseNotificationLifecycleValidationSrv(
            "notifications/disease/diseaseConfiguration.json",
            properties,
            fhirParser,
            new ObjectMapper(),
            FhirContext.forR4Cached(),
            scenarios,
            true);

    diseaseNotificationLifecycleValidationSrv.init();

    assertThatThrownBy(() -> diseaseNotificationLifecycleValidationSrv.validate(diseaseExample))
        .isInstanceOf(LifecycleValidationException.class);
  }

  @Test
  void shouldCreateDataOnBuildAndValidateNotificationsAlternativValidateMethodRegression()
      throws IOException {

    File file = new File("src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E.json");
    FileInputStream inputStream = new FileInputStream(file);
    String jsonString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    inputStream.close();

    DiseaseNotificationLifecycleValidationSrv diseaseNotificationLifecycleValidationSrv =
        new DiseaseNotificationLifecycleValidationSrv(
            "notifications/disease/diseaseConfiguration.json",
            properties,
            fhirParser,
            new ObjectMapper(),
            FhirContext.forR4Cached(),
            scenarios,
            true);

    diseaseNotificationLifecycleValidationSrv.init();

    List<String> validate =
        diseaseNotificationLifecycleValidationSrv.validate(jsonString, MediaType.APPLICATION_JSON);

    assertThat(validate).containsExactlyInAnyOrder("S_FM_V2E", "S_IM_E");
  }

  @ParameterizedTest
  @MethodSource("scenarioNames")
  void shouldReturnScenarioNameForEachExample(String path, List<String> expectedScenarios)
      throws IOException {

    File file = new File(path);
    FileInputStream inputStream = new FileInputStream(file);
    String jsonString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    inputStream.close();

    Bundle diseaseExample = fhirParser.parseBundleOrParameter(jsonString, MessageType.JSON);

    DiseaseNotificationLifecycleValidationSrv diseaseNotificationLifecycleValidationSrv =
        new DiseaseNotificationLifecycleValidationSrv(
            "notifications/disease/diseaseConfiguration.json",
            properties,
            fhirParser,
            new ObjectMapper(),
            FhirContext.forR4Cached(),
            scenarios,
            true);

    diseaseNotificationLifecycleValidationSrv.init();

    List<String> validate = diseaseNotificationLifecycleValidationSrv.validate(diseaseExample);

    assertThat(validate).containsExactlyInAnyOrderElementsOf(expectedScenarios);
  }

  @ParameterizedTest
  @MethodSource("scenarioNames")
  void shouldReturnOnlyOneScenario(String path, List<String> expectedScenarios) throws IOException {

    File file = new File(path);
    FileInputStream inputStream = new FileInputStream(file);
    String jsonString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    inputStream.close();

    Bundle diseaseExample = fhirParser.parseBundleOrParameter(jsonString, MessageType.JSON);

    properties = new DiseaseConfigurationProperties("configuration/diseaseScenarios.json", false);

    DiseaseNotificationLifecycleValidationSrv diseaseNotificationLifecycleValidationSrv =
        new DiseaseNotificationLifecycleValidationSrv(
            "notifications/disease/diseaseConfiguration.json",
            properties,
            fhirParser,
            new ObjectMapper(),
            FhirContext.forR4Cached(),
            scenarios,
            true);

    diseaseNotificationLifecycleValidationSrv.init();

    List<String> validate = diseaseNotificationLifecycleValidationSrv.validate(diseaseExample);

    assertThat(validate).hasSize(1);
  }
}
