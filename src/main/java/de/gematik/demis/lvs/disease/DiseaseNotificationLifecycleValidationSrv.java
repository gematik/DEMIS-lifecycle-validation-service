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

import static de.gematik.demis.lvs.disease.NotificationData.extractRelevantData;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.disease.configmodel.Scenario;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DiseaseNotificationLifecycleValidationSrv {

  private final String diseaseConfigPath;
  private final FhirParser fhirParser;
  private List<Scenario> allowList;

  private final ObjectMapper objectMapper;

  public DiseaseNotificationLifecycleValidationSrv(
      @Value("${disease.config}") String diseaseConfigPath,
      FhirParser fhirParser,
      ObjectMapper objectMapper) {
    this.diseaseConfigPath = diseaseConfigPath;
    this.fhirParser = fhirParser;
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  void init() throws IOException {
    allowList = new AllowListPreparator(diseaseConfigPath, objectMapper).build().getAllowList();
  }

  public List<String> validate(Bundle notification) {
    List<String> scenarioId = validateNotification(notification);
    log.info(
        "Lifecycle Validation of Notification with BundleID {} successful. Valid Scenario: {}",
        notification.getIdentifier().getValue(),
        scenarioId);
    return scenarioId;
  }

  public List<String> validate(String notification, MediaType mediaType) {
    MediaType.parseMediaType(mediaType.getType() + "/" + mediaType.getSubtype());
    Bundle fhirMessage = fhirParser.parseBundleOrParameter(notification, mediaType.getSubtype());
    return validate(fhirMessage);
  }

  private List<String> validateNotification(Bundle notification) {
    NotificationData data = extractRelevantData(notification);
    return checkScenarios(data);
  }

  private List<String> checkScenarios(NotificationData data) {
    List<String> collectScenarios = new ArrayList<>();
    for (Scenario scenario : allowList) {
      if (scenario.isScenarioValid(data)) {
        log.info("Valid scenario found: " + scenario.scenarioId());
        collectScenarios.add(scenario.scenarioId());
      }
    }
    if (collectScenarios.isEmpty()) {
      throw new LifecycleValidationException("No valid scenario found");
    }
    return collectScenarios;
  }
}
