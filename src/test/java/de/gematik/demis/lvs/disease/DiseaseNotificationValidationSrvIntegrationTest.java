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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static de.gematik.demis.lvs.common.exception.ExceptionMessages.EXCEPTION_MESSAGE_NOTIFICATION_CATEGORY_MISMATCH;
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
import de.gematik.demis.lvs.disease.fhirpath.DiseaseConfigurationProperties;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.EnableWireMock;

@AutoConfigureMockMvc()
@SpringBootTest(
    properties = {
      "lvs.client.dls=http://localhost:7072",
      "demis.codemapping.client.base-url=http://localhost:7073"
    })
@EnableWireMock
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(MockitoExtension.class)
class DiseaseNotificationValidationSrvIntegrationTest {

  private static final WireMockServer DLS_SERVER = new WireMockServer(7072);
  private static final WireMockServer FUTS_SERVER = new WireMockServer(7073);
  public static final String NOTIFICATION_CATEGORY_BAND =
      """
                {"notificationCategory": "band"}
                """;
  public static final String NOTIFICATION_CATEGORY_CVDD =
      """
                {"notificationCategory": "cvdd"}
                """;
  public static final String NOTIFICATION_CATEGORY_RUND =
      """
                {"notificationCategory": "rund"}
                """;

  private static List<Scenario> scenarios;
  @Autowired private FhirParser fhirParser;
  private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
  @Autowired private AdditionalOperationExecuter additionalOperationExecuter;

  static Stream<Arguments> scenarioNameInitial() {
    return Stream.of(
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_IM_V.json", "S_IM_V"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_IM_EoT.json", "S_IM_EoT"));
  }

  static Stream<Arguments> scenarioNamesSupplementary() {
    return Stream.of(
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2V.json", "S_FM_V2V"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2I.json", "S_FM_V2I"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_V2EoT.json",
            "S_FM_V2EoT"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_confirmed.json",
            "S_FM"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_enteredInError.json",
            "S_FM"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/S_FM_unconfirmed.json",
            "S_FM"));
  }

  static Stream<Arguments> scenarioNamesAnonymous() {
    return Stream.of(
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/anonymous/S_FM_V2V.json",
            "S_FM_V2V_Anonymous"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/anonymous/S_FM_V2I.json",
            "S_FM_V2I_Anonymous"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/anonymous/S_FM_V2EoT.json",
            "S_FM_V2EoT_Anonymous"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/anonymous/S_FM_confirmed.json",
            "S_FM_Anonymous"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/anonymous/S_FM_enteredInError.json",
            "S_FM_Anonymous"),
        Arguments.of(
            "src/test/resources/notifications/disease/scenarioExamples/anonymous/S_FM_unconfirmed.json",
            "S_FM_Anonymous"));
  }

  @BeforeAll
  static void startServer() {
    DLS_SERVER.start();
    FUTS_SERVER.start();
    DiseaseConfigurationProperties properties =
        new DiseaseConfigurationProperties(
            "configuration/diseaseScenarios.json", "configuration/keyToFhirPath.json");
    scenarios =
        ScenarioLoader.loadScenarios(properties.fhirPathData(), properties.keyToFhirPathData());
  }

  @AfterAll
  static void stopServer() {
    DLS_SERVER.stop();
    FUTS_SERVER.stop();
  }

  @ParameterizedTest
  @MethodSource("scenarioNameInitial")
  @MethodSource("scenarioNamesSupplementary")
  @MethodSource("scenarioNamesAnonymous")
  void shouldProcessScenarioExample(final String notificationPath, final String expectedScenario)
      throws IOException {
    configureDlsMockServerForValidScenarios();
    configureFutsMockServer();
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

  @ParameterizedTest
  @MethodSource("scenarioNamesSupplementary")
  @MethodSource("scenarioNamesAnonymous")
  void shouldThrowExceptionForEachNotification_IdNotFound(final String notificationPath)
      throws IOException {
    configureDlsMockServerIdDoesNotExist();

    String fileContent = Files.readString(Paths.get(notificationPath));

    NotificationScenarioValidationService notificationScenarioValidationService =
        new NotificationScenarioValidationService(
            FhirContext.forR4Cached(),
            scenarios,
            new ValidationMetrics(meterRegistry),
            additionalOperationExecuter,
            fhirParser);

    if (notificationPath.contains("scenarioExamples/S_FM_V2V")
        || notificationPath.contains("scenarioExamples/S_FM_V2E")) {
      final String scenario =
          notificationScenarioValidationService.getValidScenariosForNotification(
              fileContent, MediaType.APPLICATION_JSON, null);
      assertThat(scenario).startsWith("S_IM");
    } else {
      assertThatThrownBy(
              () ->
                  notificationScenarioValidationService.getValidScenariosForNotification(
                      fileContent, MediaType.APPLICATION_JSON, null))
          .isInstanceOf(LifecycleValidationException.class)
          .hasMessageContaining("No valid lifecycle scenario found");
    }
  }

  @ParameterizedTest
  @MethodSource("scenarioNamesSupplementary")
  @MethodSource("scenarioNamesAnonymous")
  void shouldThrowExceptionForEachNotification_NotificationCategoryDoesNotMatch(
      final String notificationPath) throws IOException {

    configureFutsMockServer();
    configureDlsMockServerDifferentNotificationCategory();

    String fileContent = Files.readString(Paths.get(notificationPath));
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
        .hasMessageContaining(EXCEPTION_MESSAGE_NOTIFICATION_CATEGORY_MISMATCH);
  }

  private void configureDlsMockServerForValidScenarios() {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());

    // configure dls to return band category for relatesToId of followUp band scenarios
    stubFor(
        get(urlEqualTo(
                "/notification/"
                    + "92d99f62-fe4f-4337-b833-351751db12dc"
                    + "/notificationCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(NOTIFICATION_CATEGORY_BAND)));

    // configure dls to return band category for notificationId of supplementary band scenarios
    stubFor(
        get(urlEqualTo(
                "/notification/"
                    + "d02cb640-eecd-4f8e-9695-b489321bd9b7"
                    + "/notificationCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(NOTIFICATION_CATEGORY_BAND)));

    // configure dls to return rund category for notificationId of supplementary rund scenario
    stubFor(
        get(urlEqualTo(
                "/notification/"
                    + "2693612f-847d-4dd8-becc-16a7da0bb453"
                    + "/notificationCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(NOTIFICATION_CATEGORY_RUND)));

    // configure dls to return 404 for notificationId of initial scenario
    stubFor(
        get(urlEqualTo("/notification/e719e5f6-f8de-470f-8d88-60a3820c037a/notificationCategory"))
            .willReturn(aResponse().withStatus(404)));
  }

  private void configureDlsMockServerDifferentNotificationCategory() {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());
    // configure dls to return non-matching category for notificationId of supplementary band
    // scenarios
    stubFor(
        get(urlEqualTo(
                "/notification/"
                    + "d02cb640-eecd-4f8e-9695-b489321bd9b7"
                    + "/notificationCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(NOTIFICATION_CATEGORY_CVDD)));

    // configure dls to return non-matching category for notificationId of supplementary rund
    // scenario
    stubFor(
        get(urlEqualTo(
                "/notification/"
                    + "2693612f-847d-4dd8-becc-16a7da0bb453"
                    + "/notificationCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(NOTIFICATION_CATEGORY_CVDD)));

    // configure dls to return non-matching category for relatesToId of follow up band scenarios
    stubFor(
        get(urlEqualTo(
                "/notification/"
                    + "92d99f62-fe4f-4337-b833-351751db12dc"
                    + "/notificationCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(NOTIFICATION_CATEGORY_CVDD)));
  }

  private void configureDlsMockServerIdDoesNotExist() {
    DLS_SERVER.resetAll();
    configureFor(DLS_SERVER.port());
    // configure dls to return 404 for notificationId of supplementary band scenarios
    stubFor(
        get(urlEqualTo("/notification/d02cb640-eecd-4f8e-9695-b489321bd9b7/notificationCategory"))
            .willReturn(aResponse().withStatus(404)));

    // configure dls to return 404 for notificationId of supplementary rund scenario
    stubFor(
        get(urlEqualTo("/notification/2693612f-847d-4dd8-becc-16a7da0bb453/notificationCategory"))
            .willReturn(aResponse().withStatus(404)));

    // configure dls to return 404 for relatesToId of follow up band scenarios
    stubFor(
        get(urlEqualTo("/notification/92d99f62-fe4f-4337-b833-351751db12dc/notificationCategory"))
            .willReturn(aResponse().withStatus(404)));
  }

  private void configureFutsMockServer() {
    FUTS_SERVER.resetAll();
    configureFor(FUTS_SERVER.port());
    stubFor(
        get(urlEqualTo(
                "/fhir-ui-data-model-translation/conceptmap/NotificationCategoryToTransmissionCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"banp\":\"ban\", \"cvdp\":\"cvd\", \"runp\":\"run\"}")));
    stubFor(
        get(urlEqualTo(
                "/fhir-ui-data-model-translation/conceptmap/NotificationDiseaseCategoryToTransmissionCategory"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"band\":\"ban\", \"cvdd\":\"cvd\", \"rund\":\"run\"}")));
  }
}
