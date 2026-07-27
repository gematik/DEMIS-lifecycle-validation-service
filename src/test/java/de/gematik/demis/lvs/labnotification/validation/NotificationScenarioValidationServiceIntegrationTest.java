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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ca.uhn.fhir.context.FhirContext;
import com.github.tomakehurst.wiremock.WireMockServer;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.common.externalchecks.AdditionalOperationExecuter;
import de.gematik.demis.lvs.common.fhirpath.Scenario;
import de.gematik.demis.lvs.common.fhirpath.ScenarioLoader;
import de.gematik.demis.lvs.common.validation.NotificationScenarioValidationService;
import de.gematik.demis.lvs.labnotification.fhirpath.LaboratoryConfigurationProperties;
import de.gematik.demis.lvs.metrics.ValidationMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.EnableWireMock;

@AutoConfigureMockMvc()
@SpringBootTest(
    properties = {
      "lvs.client.dls=http://localhost:7070",
      "demis.codemapping.client.base-url=http://localhost:7071"
    })
@EnableWireMock
@TestPropertySource(locations = "classpath:application-test.properties")
class NotificationScenarioValidationServiceIntegrationTest {

  private static final WireMockServer DLS_SERVER = new WireMockServer(7070);
  private static final WireMockServer FUTS_SERVER = new WireMockServer(7071);
  public static final String NOTIFICATION_CATEGORY_CVDP =
      """
            {"notificationCategory": "cvdp"}
            """;

  public static final String NOTIFICATION_CATEGORY_HIVP =
      """
                {"notificationCategory": "hivp"}
                """;

  private static List<Scenario> scenarios;
  @Autowired private FhirParser fhirParser;

  @BeforeAll
  static void startServer() {
    DLS_SERVER.start();
    FUTS_SERVER.start();
    LaboratoryConfigurationProperties properties =
        new LaboratoryConfigurationProperties(
            "configuration/laboratoryScenarios.json",
            "configuration/laboratoryScenarios_anonymous73.json",
            "configuration/keyToFhirPath.json");
    scenarios =
        ScenarioLoader.loadScenarios(
            properties.fhirPathDataAnonymous73(), properties.keyToFhirPathData());

    configureFor(FUTS_SERVER.port());
    stubFor(
        get(urlEqualTo(
                "/fhir-ui-data-model-translation/conceptmap/NotificationCategoryToTransmissionCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"cvdp\":\"cvd\", \"hivp\":\"hiv\"}")));

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
            "src/test/resources/notifications/laboratory/scenarioExamples/M_POS.json",
            "M_POS",
            null,
            null,
            null,
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/aM_73_POS.json",
            "aM_73_POS",
            null,
            null,
            null,
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/aM_73_POS-withRelatesTo.json",
            "aM_73_POS",
            null,
            null,
            null,
            null),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/aM_73_POS-withRelatesTo.json",
            "aM_73_POS",
            null,
            null,
            "9b0d637c-e163-4380-adb7-8e207f4462c9",
            NOTIFICATION_CATEGORY_HIVP),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/FM_NEG.json",
            "FM_NEG",
            null,
            null,
            "92d99f62-fe4f-4337-b833-351751db12dc",
            NOTIFICATION_CATEGORY_CVDP),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/aFM_POS.json",
            "aFM_POS",
            null,
            null,
            "92d99f62-fe4f-4337-b833-351751db12dc",
            NOTIFICATION_CATEGORY_CVDP),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/aFM_NEG.json",
            "aFM_NEG",
            null,
            null,
            "92d99f62-fe4f-4337-b833-351751db12dc",
            NOTIFICATION_CATEGORY_CVDP),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/EM_NEG.json",
            "EM_NEG",
            200,
            "d7a333e9-439f-4be4-9824-bab1444fe085",
            null,
            NOTIFICATION_CATEGORY_CVDP),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/IM_NEG_CVDP.json",
            "IM_NEG_CVDP",
            404,
            "e8d8cc43-32c2-4f93-8eaf-b2f3e6deb2a9",
            null,
            null));
  }

  static Stream<String> scenarioNamesInvalidUuid() {
    return Stream.of(
        "src/test/resources/notifications/laboratory/scenarioExamples/invalid/FM_NEG_invalid_uuid.json",
        "src/test/resources/notifications/laboratory/scenarioExamples/invalid/EM_NEG_invalid_uuid.json",
        "src/test/resources/notifications/laboratory/scenarioExamples/invalid/aFM_NEG_relatesToId_invalid_uuid.json",
        "src/test/resources/notifications/laboratory/scenarioExamples/invalid/aFM_POS_relatesToId_invalid_uuid.json");
  }

  static Stream<Arguments> scenarioNamesIdDoesNotExist() {
    return Stream.of(
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/FM_NEG.json",
            "3bc0a462-5088-4f04-b94a-f9b2d3433cfe"),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/EM_NEG.json",
            "d7a333e9-439f-4be4-9824-bab1444fe085"),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/aFM_NEG.json",
            "92d99f62-fe4f-4337-b833-351751db12dc"),
        Arguments.of(
            "src/test/resources/notifications/laboratory/scenarioExamples/aFM_POS.json",
            "92d99f62-fe4f-4337-b833-351751db12dc"));
  }

  private void configureMockServer(
      final String notificationId,
      final Integer statusDlsForNotificationId,
      final String relatesToId,
      final String notificationCategoryOfIM) {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());
    if (notificationId != null && statusDlsForNotificationId != 0) {
      if (statusDlsForNotificationId == 404) {
        stubFor(
            get(urlEqualTo("/notification/" + notificationId + "/notificationCategory"))
                .willReturn(aResponse().withStatus(statusDlsForNotificationId)));
      } else {
        stubFor(
            get(urlEqualTo("/notification/" + notificationId + "/notificationCategory"))
                .willReturn(
                    aResponse()
                        .withStatus(statusDlsForNotificationId)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            Optional.ofNullable(notificationCategoryOfIM)
                                .orElse(NOTIFICATION_CATEGORY_CVDP))));
      }
    }

    if (relatesToId != null) {
      stubFor(
          get(urlEqualTo("/notification/" + relatesToId + "/notificationCategory"))
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(
                          Optional.ofNullable(notificationCategoryOfIM)
                              .orElse(NOTIFICATION_CATEGORY_CVDP))));
    }
  }

  private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
  @Autowired private AdditionalOperationExecuter additionalOperationExecuter;

  @ParameterizedTest
  @MethodSource("scenarioNames")
  void shouldProcessScenarioExample(
      final String notificationPath,
      final String expectedScenario,
      final Integer statusDlsForNotificationId,
      final String notificationId,
      final String relatesToId,
      final String notificationCategoryOfIM)
      throws IOException {
    configureMockServer(
        notificationId, statusDlsForNotificationId, relatesToId, notificationCategoryOfIM);

    String fileContent = Files.readString(Paths.get(notificationPath));

    NotificationScenarioValidationService notificationScenarioValidationService =
        new NotificationScenarioValidationService(
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
  void shouldProcessPositiveNotification_withRelatesTo_shouldNotCallDLS() throws IOException {
    final String notificationId = "3bc0a462-5088-4f04-b94a-f9b2d3433cfe";
    final String relatesToId = "92d99f62-fe4f-4337-b833-351751db12dc";

    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/notifications/laboratory/scenarioExamples/M_POS.json"));

    NotificationScenarioValidationService notificationScenarioValidationService =
        new NotificationScenarioValidationService(
            FhirContext.forR4Cached(),
            scenarios,
            new ValidationMetrics(meterRegistry),
            additionalOperationExecuter,
            fhirParser);

    final String scenario =
        notificationScenarioValidationService.getValidScenariosForNotification(
            fileContent, MediaType.APPLICATION_JSON, null);

    assertThat(scenario).isEqualTo("M_POS");

    // verify no request with notificationId to DLS is made, when byName notification is positive
    verify(
        0,
        getRequestedFor(urlEqualTo("/notification/" + notificationId + "/notificationCategory")));

    // verify no request with relatesToId to DLS is made, when byName notification is positive
    verify(
        0, getRequestedFor(urlEqualTo("/notification/" + relatesToId + "/notificationCategory")));
  }

  @ParameterizedTest
  @MethodSource("scenarioNamesIdDoesNotExist")
  void shouldThrowLifeCycleValidationError_NotExistingRelatesToId(
      final String notificationPath, final String id) throws IOException {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());

    stubFor(
        get(urlEqualTo("/notification/" + id + "/notificationCategory"))
            .willReturn(aResponse().withStatus(404)));

    final String fileContent = Files.readString(Paths.get(notificationPath));

    NotificationScenarioValidationService notificationScenarioValidationService =
        new NotificationScenarioValidationService(
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

    verify(1, getRequestedFor(urlEqualTo("/notification/" + id + "/notificationCategory")));
  }

  @Test
  void shouldThrowLifeCycleValidationError_anonymous73_otherNotificationCategory()
      throws IOException {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());

    stubFor(
        get(urlEqualTo(
                "/notification/"
                    + "9b0d637c-e163-4380-adb7-8e207f4462c9"
                    + "/notificationCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(NOTIFICATION_CATEGORY_CVDP)));

    final String fileContent =
        Files.readString(
            Paths.get(
                "src/test/resources/notifications/laboratory/scenarioExamples/aM_73_POS-withRelatesTo.json"));

    NotificationScenarioValidationService notificationScenarioValidationService =
        new NotificationScenarioValidationService(
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
        .hasMessageContaining("Notification-category of related notification does not match");
  }

  @ParameterizedTest
  @MethodSource("scenarioNamesInvalidUuid")
  void shouldThrowLifeCycleValidationError_RelatesToId_invalidUUID(final String notificationPath)
      throws IOException {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());

    final String fileContent = Files.readString(Paths.get(notificationPath));

    NotificationScenarioValidationService notificationScenarioValidationService =
        new NotificationScenarioValidationService(
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

    // verify no reuest to DLS is made, when ID is invalid UUID
    verify(0, getRequestedFor(urlEqualTo("/notification/12345/notificationCategory")));
  }

  @Test
  void shouldThrowLifeCycleValidationError_7_4_NotificationIdExists() throws IOException {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());

    final String notificationId = "e8d8cc43-32c2-4f93-8eaf-b2f3e6deb2a9";
    stubFor(
        get(urlEqualTo("/notification/" + notificationId + "/notificationCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(NOTIFICATION_CATEGORY_CVDP)));

    String fileContent =
        Files.readString(
            Paths.get(
                "src/test/resources/notifications/laboratory/scenarioExamples/IM_NEG_CVDP.json"));

    NotificationScenarioValidationService notificationScenarioValidationService =
        new NotificationScenarioValidationService(
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

    // fhirPath validation valid, verify that notificationCategory for notificationId was requested
    // at DLS
    verify(
        1,
        getRequestedFor(urlEqualTo("/notification/" + notificationId + "/notificationCategory")));
  }
}
