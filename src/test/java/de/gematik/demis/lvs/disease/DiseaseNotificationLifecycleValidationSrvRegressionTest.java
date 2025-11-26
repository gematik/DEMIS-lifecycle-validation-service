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
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class DiseaseNotificationLifecycleValidationSrvRegressionTest {

  private final FhirParser fhirParser = new FhirParser(FhirContext.forR4Cached());

  static Stream<Arguments> scenarioNames() {
    return Stream.of(
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V-11.json", "S_IM_V"),
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
            "src/test/resources/notifications/disease/scenarioExamples/RUND.json", "S_FM_V2E"));
  }

  @Test
  void shouldCreateDataOnBuildAndValidateNotificationsRegression() throws IOException {

    String jsonString =
        Files.readString(
            Path.of("src/test/resources/notifications/disease/scenarioExamples/S_FM_V2E-11.json"));

    DiseaseBasicNotificationLifecycleValidationSrv diseaseNotificationLifecycleValidationSrv =
        new DiseaseBasicNotificationLifecycleValidationSrv(
            "notifications/disease/diseaseConfiguration.json", fhirParser, new ObjectMapper());

    diseaseNotificationLifecycleValidationSrv.init();

    String validate =
        diseaseNotificationLifecycleValidationSrv.validate(jsonString, MediaType.APPLICATION_JSON);

    assertThat(validate).isEqualTo("S_FM_V2E");
  }

  @Test
  void shouldThrowException() throws IOException {

    String jsonString =
        Files.readString(
            Path.of(
                "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V_not_valid.json"));

    DiseaseBasicNotificationLifecycleValidationSrv diseaseNotificationLifecycleValidationSrv =
        new DiseaseBasicNotificationLifecycleValidationSrv(
            "notifications/disease/diseaseConfiguration.json", fhirParser, new ObjectMapper());

    diseaseNotificationLifecycleValidationSrv.init();

    assertThatThrownBy(
            () ->
                diseaseNotificationLifecycleValidationSrv.validate(
                    jsonString, MediaType.APPLICATION_JSON))
        .isInstanceOf(LifecycleValidationException.class);
  }

  @ParameterizedTest
  @MethodSource("scenarioNames")
  void shouldReturnScenarioNameForEachExample(String path, String expectedScenarios)
      throws IOException {

    String jsonString = Files.readString(Path.of(path));

    DiseaseBasicNotificationLifecycleValidationSrv diseaseNotificationLifecycleValidationSrv =
        new DiseaseBasicNotificationLifecycleValidationSrv(
            "notifications/disease/diseaseConfiguration.json", fhirParser, new ObjectMapper());

    diseaseNotificationLifecycleValidationSrv.init();

    String validate =
        diseaseNotificationLifecycleValidationSrv.validate(jsonString, MediaType.APPLICATION_JSON);

    assertThat(validate).isEqualTo(expectedScenarios);
  }
}
