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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.FhirPathExecutionException;
import ca.uhn.fhir.fhirpath.IFhirPath;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.disease.configmodel.Scenario;
import de.gematik.demis.lvs.disease.fhirpath.DiseaseConfigurationProperties;
import de.gematik.demis.lvs.disease.fhirpath.DiseaseScenario;
import de.gematik.demis.notification.builder.demis.fhir.path.CustomEvaluationContext;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DiseaseNotificationLifecycleValidationSrv {

  private final String diseaseConfigPath;
  private final FhirParser fhirParser;
  private final boolean notifications7_3;
  private final ObjectMapper objectMapper;
  private final List<DiseaseScenario> allowedScenarioList;
  private final FhirContext context;
  private final boolean addAllScenariosPassedToReturnList;
  private List<Scenario> allowList;

  public DiseaseNotificationLifecycleValidationSrv(
      @Value("${disease.config}") String diseaseConfigPath,
      DiseaseConfigurationProperties diseaseConfigurationProperties,
      FhirParser fhirParser,
      ObjectMapper objectMapper,
      FhirContext context,
      List<DiseaseScenario> allowedScenarioList,
      @Value("${feature.flag.notifications.7_3}") boolean notifications7_3) {
    this.diseaseConfigPath = diseaseConfigPath;
    this.fhirParser = fhirParser;
    this.objectMapper = objectMapper;
    this.notifications7_3 = notifications7_3;
    this.allowedScenarioList = allowedScenarioList;
    this.context = context;
    this.addAllScenariosPassedToReturnList =
        diseaseConfigurationProperties.configShowAllScenarioPassed();
  }

  @PostConstruct
  void init() throws IOException {
    allowList = new AllowListPreparator(diseaseConfigPath, objectMapper).build().getAllowList();
  }

  public List<String> validate(Bundle notification) {
    List<String> scenarioIds;
    if (notifications7_3) {
      scenarioIds = validateNotification(notification);
    } else {
      scenarioIds = validateNotificationRegression(notification);
    }
    log.info(
        "Lifecycle Validation of Notification with BundleID {} successful. Valid Scenario: {}",
        notification.getIdentifier().getValue(),
        scenarioIds);
    return scenarioIds;
  }

  public List<String> validate(String notification, MediaType mediaType) {
    MediaType.parseMediaType(mediaType.getType() + "/" + mediaType.getSubtype());
    Bundle fhirMessage = fhirParser.parseBundleOrParameter(notification, mediaType.getSubtype());
    return validate(fhirMessage);
  }

  private List<String> validateNotification(Bundle notification) {
    List<String> returnList = new ArrayList<>();
    IFhirPath fhirPath = context.newFhirPath();
    CustomEvaluationContext evaluationContext = new CustomEvaluationContext(notification);
    fhirPath.setEvaluationContext(evaluationContext);
    for (DiseaseScenario ds : allowedScenarioList) {
      try {
        boolean atLeastOneFhirPathExpressionInvalid =
            ds.getFhirPathExpression().stream()
                .map(DiseaseScenario.FhirPathExpression::getFhirPath)
                .map(s -> fhirPath.evaluate(notification, s, BooleanType.class))
                .map(List::getFirst)
                .map(PrimitiveType::getValue)
                .anyMatch(aBoolean -> aBoolean.equals(false));
        if (!atLeastOneFhirPathExpressionInvalid) {
          returnList.add(ds.getName());
          log.info("Valid scenario found: " + ds.getName());
        }
      } catch (FhirPathExecutionException e) {
        log.error(
            "Error evaluating FhirPath expression: "
                + ds.getName()
                + "|"
                + ds.getFhirPathExpression(),
            e);
        throw new LifecycleValidationException("Error evaluating FhirPath expression", e);
      }

      if (!addAllScenariosPassedToReturnList && !returnList.isEmpty()) {
        return returnList;
      }
    }
    if (returnList.isEmpty()) {
      throw new LifecycleValidationException("No valid scenario found");
    }
    return returnList;
  }

  private List<String> validateNotificationRegression(Bundle notification) {
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
