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

import static de.gematik.demis.lvs.disease.NotificationData.extractRelevantData;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.common.validation.NotifcationBasicValidator;
import de.gematik.demis.lvs.disease.configmodel.Scenario;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DiseaseBasicNotificationLifecycleValidationSrv implements NotifcationBasicValidator {

  public static final String NO_VALID_LIFECYCLE_SCENARIO_FOUND =
      "No valid lifecycle scenario found";
  private final String diseaseConfigPath;
  private final FhirParser fhirParser;
  private final ObjectMapper objectMapper;
  private List<Scenario> allowList;

  public DiseaseBasicNotificationLifecycleValidationSrv(
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

  private Bundle parse(String notification, MediaType mediaType) {
    MediaType.parseMediaType(mediaType.getType() + "/" + mediaType.getSubtype());
    return fhirParser.parseBundleOrParameter(notification, mediaType.getSubtype());
  }

  /**
   * @deprecated
   * @param notificationString
   * @param mediaType
   * @return
   */
  @Deprecated(forRemoval = true)
  public String validate(String notificationString, MediaType mediaType) {
    Bundle notification = parse(notificationString, mediaType);
    NotificationData data = extractRelevantData(notification);
    return checkScenarios(data);
  }

  /**
   * @deprecated
   * @param data
   * @return
   */
  @Deprecated(forRemoval = true)
  private String checkScenarios(NotificationData data) {
    for (Scenario scenario : allowList) {
      if (scenario.isScenarioValid(data)) {
        log.info("Valid scenario found: " + scenario.scenarioId());
        return scenario.scenarioId();
      }
    }
    throw new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND);
  }
}
