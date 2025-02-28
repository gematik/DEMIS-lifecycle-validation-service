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
 * #L%
 */

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(path = "/disease")
@RequiredArgsConstructor
@Slf4j
public class DiseaseNotificationLifecycleValidationCtr {

  private final DiseaseNotificationLifecycleValidationSrv diseaseNotificationLifecycleValidationSrv;

  @PostMapping(
      path = "$validate",
      consumes = {
        APPLICATION_JSON_VALUE,
        APPLICATION_XML_VALUE,
        "application/json+fhir",
        "application/fhir+json"
      },
      produces = {
        APPLICATION_JSON_VALUE,
        APPLICATION_XML_VALUE,
        "application/json+fhir",
        "application/fhir+json"
      })
  ResponseEntity<List<String>> validate(
      @RequestBody @NotBlank final String notification,
      @RequestHeader(name = CONTENT_TYPE) final MediaType mediaType) {
    List<String> validate =
        diseaseNotificationLifecycleValidationSrv.validate(notification, mediaType);
    return ResponseEntity.ok().body(validate);
  }
}
