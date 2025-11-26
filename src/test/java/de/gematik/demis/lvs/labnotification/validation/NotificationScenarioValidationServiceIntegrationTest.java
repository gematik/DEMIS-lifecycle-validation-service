package de.gematik.demis.lvs.labnotification.validation;

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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ca.uhn.fhir.context.FhirContext;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.common.externalchecks.AdditionalOperationExecuter;
import de.gematik.demis.lvs.common.fhirpath.ScenarioLoader;
import de.gematik.demis.lvs.common.validation.NotificationScenarioValidationService;
import de.gematik.demis.lvs.labnotification.fhirpath.LaboratoryConfigurationProperties;
import de.gematik.demis.lvs.labnotification.fhirpath.LaboratoryScenario;
import de.gematik.demis.lvs.metrics.ValidationMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@AutoConfigureMockMvc()
@SpringBootTest(
    properties = {
      "lvs.client.dls=http://localhost:7070",
      "lvs.client.futs.address=http://localhost:7071",
      "lvs.client.futs.address.context-path=/fhir-ui-data-model-translation/"
    })
@AutoConfigureWireMock(port = 0) // dynamic random port
@TestPropertySource(locations = "classpath:application-test.properties")
class NotificationScenarioValidationServiceIntegrationTest {

  private static final WireMockServer DLS_SERVER = new WireMockServer(7070);
  private static final WireMockServer FUTS_SERVER = new WireMockServer(7071);
  public static final String NOTIFICATION_CATEGORY_CVDP =
      """
            {"notificationCategory": "cvdp"}
            """;

  private static List<LaboratoryScenario> scenarios;
  @Autowired private FhirParser fhirParser;

  @BeforeAll
  static void startServer() {
    DLS_SERVER.start();
    FUTS_SERVER.start();
    LaboratoryConfigurationProperties properties =
        new LaboratoryConfigurationProperties(
            "configuration/laboratoryScenarios.json", "configuration/keyToFhirPath.json", true);
    scenarios =
        ScenarioLoader.loadScenarios(
            properties.fhirPathData(), properties.keyToFhirPathData(), LaboratoryScenario[].class);

    configureFor(FUTS_SERVER.port());
    stubFor(
        get(urlEqualTo(
                "/fhir-ui-data-model-translation/conceptmap/NotificationCategoryToTransmissionCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"cvdp\":\"cvd\"}")));
    stubFor(
        get(urlEqualTo(
                "/fhir-ui-data-model-translation/conceptmap/NotificationDiseaseCategoryToTransmissionCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"cvdd\":\"cvd\"}")));
  }

  @AfterAll
  static void stopServer() {
    DLS_SERVER.stop();
    FUTS_SERVER.stop();
  }

  static Stream<Arguments> scenarioNames() {
    return Stream.of(
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S1.json",
            "1",
            404,
            "e8d8cc43-32c2-4f93-8eaf-b2f3e6deb2a9",
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S2A.json",
            "2A",
            200,
            "3bc0a462-5088-4f04-b94a-f9b2d3433cfe",
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S2B.json",
            "2B",
            404,
            "d8091399-f4a9-431e-a81a-c7dc6b5b7e96",
            "92d99f62-fe4f-4337-b833-351751db12dc"),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S2C.json",
            "2C",
            404,
            "3bc0a462-5088-4f04-b94a-f9b2d3433cfe",
            "92d99f62-fe4f-4337-b833-351751db12dc"),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S3A.json",
            "3A",
            404,
            "43c2f6f1-f935-4a26-9aeb-9a6f3f1bd36c",
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S3A1.json",
            "3A1",
            200,
            "43c2f6f1-f935-4a26-9aeb-9a6f3f1bd36c",
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S3A2.json",
            "3A2",
            200,
            "d7a333e9-439f-4be4-9824-bab1444fe085",
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S4A.json",
            "4A",
            200,
            "47a7f3e6-18f8-44ba-88e1-ec7a014372c9",
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S4B.json",
            "4B",
            200,
            "47a7f3e6-18f8-44ba-88e1-ec7a014372c9",
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S5A.json",
            "5A",
            200,
            "84075dca-4049-4d7d-aa33-a9dbd6e13062",
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S5B.json",
            "5B",
            200,
            "84075dca-4049-4d7d-aa33-a9dbd6e13062",
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/S7.json",
            "7",
            404,
            "e8d8cc43-32c2-4f93-8eaf-b2f3e6deb2a9",
            null));
  }

  private void configureMockServer(
      final String notificationId, final int status, final String relatesToId) {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());
    if (status == 404) {
      stubFor(
          get(urlEqualTo("/notification/" + notificationId + "/notificationCategory"))
              .willReturn(aResponse().withStatus(status)));
    } else {
      stubFor(
          get(urlEqualTo("/notification/" + notificationId + "/notificationCategory"))
              .willReturn(
                  aResponse()
                      .withStatus(status)
                      .withHeader("Content-Type", "application/json")
                      .withBody(NOTIFICATION_CATEGORY_CVDP)));
    }

    if (relatesToId != null) {
      stubFor(
          get(urlEqualTo("/notification/" + relatesToId + "/notificationCategory"))
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(NOTIFICATION_CATEGORY_CVDP)));
    }
  }

  private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
  @Autowired private AdditionalOperationExecuter additionalOperationExecuter;

  @ParameterizedTest
  @MethodSource("scenarioNames")
  void shouldProcessScenarioExample(
      final String notificationPath,
      final String expectedScenario,
      final int status,
      final String notificationId,
      final String relatesToId)
      throws IOException {
    configureMockServer(notificationId, status, relatesToId);

    String fileContent = Files.readString(Paths.get(notificationPath));

    NotificationScenarioValidationService<LaboratoryScenario>
        notificationScenarioValidationService =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    final String scenario =
        notificationScenarioValidationService.getValidScenariosForNotification(
            fileContent, MediaType.APPLICATION_JSON, null);

    assertThat(scenario).isEqualTo(expectedScenario);
  }

  @Test
  void shouldReturnExpectedScenario_S2A_NotExistingNotificationId() throws IOException {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());
    final String notificationId = "3bc0a462-5088-4f04-b94a-f9b2d3433cfe";
    stubFor(
        get(urlEqualTo("/notification/" + notificationId + "/notificationCategory"))
            .willReturn(aResponse().withStatus(404)));

    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/notifications/laboratory/scenarioExamples/S2A.json"));

    NotificationScenarioValidationService<LaboratoryScenario>
        notificationScenarioValidationService =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    final String scenario =
        notificationScenarioValidationService.getValidScenariosForNotification(
            fileContent, MediaType.APPLICATION_JSON, null);

    assertThat(scenario).isEqualTo("2A");
  }

  @Test
  void shouldReturnExpectedScenario_S2B_NotExistingRelatesToId() throws IOException {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());
    final String notificationId = "3bc0a462-5088-4f04-b94a-f9b2d3433cfe";
    final String relatesToId = "92d99f62-fe4f-4337-b833-351751db12dc";
    stubFor(
        get(urlEqualTo("/notification/" + notificationId + "/notificationCategory"))
            .willReturn(aResponse().withStatus(404)));

    stubFor(
        get(urlEqualTo("/notification/" + relatesToId + "/notificationCategory"))
            .willReturn(aResponse().withStatus(404)));

    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/notifications/laboratory/scenarioExamples/S2B.json"));

    NotificationScenarioValidationService<LaboratoryScenario>
        notificationScenarioValidationService =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    final String scenario =
        notificationScenarioValidationService.getValidScenariosForNotification(
            fileContent, MediaType.APPLICATION_JSON, null);

    assertThat(scenario).isEqualTo("2B");
    verify(
        1, getRequestedFor(urlEqualTo("/notification/" + relatesToId + "/notificationCategory")));
  }

  @Test
  void shouldReturnExpectedScenario_S2B_RelatesToId_invalidUUID() throws IOException {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());
    final String notificationId = "3bc0a462-5088-4f04-b94a-f9b2d3433cfe";
    stubFor(
        get(urlEqualTo("/notification/" + notificationId + "/notificationCategory"))
            .willReturn(aResponse().withStatus(404)));

    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/notifications/laboratory/followup_invalid_uuid.json"));

    NotificationScenarioValidationService<LaboratoryScenario>
        notificationScenarioValidationService =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    final String scenario =
        notificationScenarioValidationService.getValidScenariosForNotification(
            fileContent, MediaType.APPLICATION_JSON, null);

    assertThat(scenario).isEqualTo("2B");
    verify(0, getRequestedFor(urlEqualTo("/notification/12345/notificationCategory")));
  }

  @Test
  void shouldThrowLifeCycleValidationError_S2C_NotExistingRelatesToId() throws IOException {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());
    final String relatesToId = "92d99f62-fe4f-4337-b833-351751db12dc";
    stubFor(
        get(urlEqualTo("/notification/" + relatesToId + "/notificationCategory"))
            .willReturn(aResponse().withStatus(404)));

    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/notifications/laboratory/scenarioExamples/S2C.json"));

    NotificationScenarioValidationService<LaboratoryScenario>
        notificationScenarioValidationService =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    assertThatThrownBy(
            () ->
                notificationScenarioValidationService.getValidScenariosForNotification(
                    fileContent, MediaType.APPLICATION_JSON, null))
        .isInstanceOf(LifecycleValidationException.class)
        .hasMessageContaining("No valid lifecycle scenario found");

    verify(
        1, getRequestedFor(urlEqualTo("/notification/" + relatesToId + "/notificationCategory")));
  }

  @Test
  void shouldThrowLifeCycleValidationError_S2C_RelatesToId_invalidUUID() throws IOException {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());

    String fileContent =
        Files.readString(
            Paths.get(
                "src/test/resources/notifications/laboratory/anonymous_followup_invalid_uuid.json"));

    NotificationScenarioValidationService<LaboratoryScenario>
        notificationScenarioValidationService =
            new NotificationScenarioValidationService<>(
                FhirContext.forR4Cached(),
                scenarios,
                new ValidationMetrics(meterRegistry),
                additionalOperationExecuter,
                fhirParser);

    assertThatThrownBy(
            () ->
                notificationScenarioValidationService.getValidScenariosForNotification(
                    fileContent, MediaType.APPLICATION_JSON, null))
        .isInstanceOf(LifecycleValidationException.class)
        .hasMessageContaining("No valid lifecycle scenario found");

    verify(0, getRequestedFor(urlEqualTo("/notification/12345/notificationCategory")));
  }
}
