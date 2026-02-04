package de.gematik.demis.lvs.common.validation;

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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.FhirPathExecutionException;
import ca.uhn.fhir.fhirpath.IFhirPath;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.common.externalchecks.AdditionalOperationExecuter;
import de.gematik.demis.lvs.common.fhirpath.FhirPathExecutor;
import de.gematik.demis.lvs.common.fhirpath.Scenario;
import de.gematik.demis.lvs.metrics.ValidationMetrics;
import de.gematik.demis.notification.builder.demis.fhir.path.CustomEvaluationContext;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckForNull;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.MediaType;

@Slf4j
public class NotificationScenarioValidationService<S extends Scenario> {

  public static final String NO_VALID_LIFECYCLE_SCENARIO_FOUND =
      "No valid lifecycle scenario found";
  private final List<S> allowedScenarioList;
  private final FhirContext context;
  private final ValidationMetrics validationMetrics;
  private final AdditionalOperationExecuter additionalOperationExecuter;
  private final FhirParser fhirParser;

  public NotificationScenarioValidationService(
      FhirContext context,
      List<S> allowedScenarioList,
      ValidationMetrics validationMetrics,
      AdditionalOperationExecuter additionalOperationExecuter,
      FhirParser fhirParser) {
    this.allowedScenarioList = allowedScenarioList;
    this.context = context;
    this.validationMetrics = validationMetrics;
    this.additionalOperationExecuter = additionalOperationExecuter;
    this.fhirParser = fhirParser;
  }

  private Bundle getNotification(final String notification, final MediaType mediaType) {
    final var contentType =
        MediaType.parseMediaType(mediaType.getType() + "/" + mediaType.getSubtype());
    return fhirParser.parseBundleOrParameter(notification, contentType.getSubtype());
  }

  public String getValidScenariosForNotification(
      final String fhirMessage, final MediaType mediaType, @CheckForNull final String principalId) {
    final Bundle notification = getNotification(fhirMessage, mediaType);
    IFhirPath fhirPath = context.newFhirPath();
    CustomEvaluationContext evaluationContext = new CustomEvaluationContext(notification);
    fhirPath.setEvaluationContext(evaluationContext);
    for (S ls : allowedScenarioList) {
      try {
        boolean atLeastOneFhirPathExpressionInvalid =
            FhirPathExecutor.isAtLeastOneFhirPathExpressionInvalid(notification, ls, fhirPath);

        if (!atLeastOneFhirPathExpressionInvalid
            && additionalOperationExecuter.checkAllExternalChecks(notification, ls, fhirPath)) {
          log.info(
              "Lifecycle Validation of Notification with BundleID {} successful. Valid Scenario: {}",
              notification.getIdentifier().getValue(),
              ls.getName());
          validationMetrics.saveScenario(ls.getName());
          return ls.getName();
        }
      } catch (FhirPathExecutionException e) {
        log.error(
            "Error evaluating FhirPath expression: "
                + ls.getName()
                + "|"
                + ls.getFhirPathExpression(),
            e);
        throw new LifecycleValidationException("Error evaluating FhirPath expression", e);
      }
    }
    validationMetrics.incUnsuccessfulValidationCount(
        Objects.requireNonNullElse(principalId, "<unknown>"), "LifecycleValidationException");
    throw new LifecycleValidationException(NO_VALID_LIFECYCLE_SCENARIO_FOUND);
  }
}
