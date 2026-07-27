package de.gematik.demis.lvs.integration;

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

import de.gematik.demis.lvs.Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@SpringBootTest(
    classes = Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Slf4j
class LaboratoryNotificationAppContextTest {

  @Autowired TestRestTemplate restTemplate;

  @LocalServerPort private int port;

  @Test
  void expect400OnEmptyBodyPayload() {
    final var result =
        Assertions.assertDoesNotThrow(
            () ->
                RequestEntityHelper.sendLaboratoryNotification(
                    restTemplate,
                    createURLWithPort("/laboratory/$validate"),
                    "",
                    MediaType.APPLICATION_JSON));
    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatusCode().value());
  }

  @Test
  void expect400OnBlankBodyPayload() {
    final var result =
        Assertions.assertDoesNotThrow(
            () ->
                RequestEntityHelper.sendLaboratoryNotification(
                    restTemplate,
                    createURLWithPort("/laboratory/$validate"),
                    "       ",
                    MediaType.APPLICATION_JSON));
    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatusCode().value());
  }

  @Test
  void expect400OnEmptyJsonBody() {
    final var result =
        Assertions.assertDoesNotThrow(
            () ->
                RequestEntityHelper.sendLaboratoryNotification(
                    restTemplate,
                    createURLWithPort("/laboratory/$validate"),
                    "{}",
                    MediaType.APPLICATION_JSON));
    log.error(result.getBody());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatusCode().value());
  }

  @Test
  void expect415OnUnsupportedMediaType() {
    // GIVEN a Valid Laboratory Notification is loaded
    final var notification =
        FileLoaderHelper.loadResourceFile(
            "src/test/resources/notifications/laboratory/positive_test_notification_Dv2.json");
    final var result =
        Assertions.assertDoesNotThrow(
            () ->
                RequestEntityHelper.sendLaboratoryNotification(
                    restTemplate,
                    createURLWithPort("/laboratory/$validate"),
                    notification,
                    MediaType.APPLICATION_PDF));
    Assertions.assertEquals(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), result.getStatusCode().value());
  }

  private String createURLWithPort(final String path) {
    return "http://localhost:" + port + path;
  }
}
