package de.gematik.demis.lvs.api;

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

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import de.gematik.demis.lvs.common.validation.NotificationValidationService;
import de.gematik.demis.lvs.disease.fhirpath.DiseaseScenario;
import de.gematik.demis.lvs.labnotification.fhirpath.LaboratoryScenario;
import jakarta.validation.constraints.NotBlank;
import javax.annotation.CheckForNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "feature.flag.fhirpath.validation.enabled", havingValue = "true")
public class LifecycleValidationController {

  public static final String HEADER_SENDER = "x-sender";

  private final NotificationValidationService<DiseaseScenario> diseaseNotificationValidationService;
  private final NotificationValidationService<LaboratoryScenario>
      laboratoryNotificationValidationService;

  @PostMapping(
      path = "{notificationType}/$validate",
      consumes = {
        APPLICATION_JSON_VALUE,
        APPLICATION_XML_VALUE,
        "application/json+fhir",
        "application/fhir+json"
      })
  ResponseEntity<String> validate(
      @RequestBody @NotBlank final String notification,
      @RequestHeader(name = CONTENT_TYPE) final MediaType mediaType,
      @RequestHeader(name = HEADER_SENDER, required = false) @CheckForNull final String principalId,
      @PathVariable final String notificationType) {

    if (notificationType.equals("disease")) {
      return ResponseEntity.ok()
          .body(
              diseaseNotificationValidationService.validate(notification, mediaType, principalId));
    }
    if (notificationType.equals("laboratory")) {
      return ResponseEntity.ok()
          .body(
              laboratoryNotificationValidationService.validate(
                  notification, mediaType, principalId));
    }

    throw new IllegalArgumentException("Invalid or unknown notification type");
  }

  @PostMapping(
      path = "$validate",
      consumes = {
        APPLICATION_JSON_VALUE,
        APPLICATION_XML_VALUE,
        "application/json+fhir",
        "application/fhir+json"
      })
  ResponseEntity<String> validate(
      @RequestBody @NotBlank final String notification,
      @RequestHeader(name = CONTENT_TYPE) final MediaType mediaType,
      @RequestHeader(name = HEADER_SENDER, required = false) @CheckForNull
          final String principalId) {

    var type = NotificationTypeDiscerner.detectNotificationType(notification);
    return validate(notification, mediaType, principalId, type);
  }
}
