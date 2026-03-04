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

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.metrics.ValidationMetrics;
import javax.annotation.CheckForNull;
import org.springframework.http.MediaType;

public class NotificationValidationService {

  private final NotifcationBasicValidator notificationBasicValidationService;
  private final NotificationScenarioValidationService notificationScenarioValidationService;
  private final boolean returnFhirpathValidationInResponse;
  private final ValidationMetrics validationMetrics;

  public NotificationValidationService(
      final NotifcationBasicValidator notificationBasicValidationService,
      final NotificationScenarioValidationService notificationScenarioValidationService,
      boolean returnFhirpathValidationInResponse,
      ValidationMetrics validationMetrics) {
    this.notificationBasicValidationService = notificationBasicValidationService;
    this.notificationScenarioValidationService = notificationScenarioValidationService;
    this.returnFhirpathValidationInResponse = returnFhirpathValidationInResponse;
    this.validationMetrics = validationMetrics;
  }

  /**
   * This method validates each notification with the basic validation and the scenario validation.
   * The Basic validation will always be evaluated, the scenario validation is on top. If the
   * validation results for one notification don't match (one is successful, one failed), it will be
   * logged. Therefore it is possible to compare the results of the previous validation with the
   * scenario validation for incoming notifications, without changing the behaviour of the endpoint,
   * in case that the new validation is too strict for the current incoming requests.
   */
  public String validate(
      final String notification,
      final MediaType mediaType,
      @CheckForNull final String principalId,
      final String notificationType) {
    String validScenario = null;

    RuntimeException basicValidationException = null;
    boolean successfulBasicValidation = false;

    LifecycleValidationException scenarioValidationException = null;
    boolean successfulScenarioValidation = false;

    if (!returnFhirpathValidationInResponse) {
      try {
        notificationBasicValidationService.validate(notification, mediaType);
        successfulBasicValidation = true;
      } catch (final InternalErrorException | LifecycleValidationException e) {
        basicValidationException = e;
      }
    }

    try {
      validScenario =
          notificationScenarioValidationService.getValidScenariosForNotification(
              notification, mediaType, principalId);
      successfulScenarioValidation = true;
    } catch (final LifecycleValidationException e) {
      scenarioValidationException = e;
    }

    if (!returnFhirpathValidationInResponse) {
      createMetric(notificationType, successfulBasicValidation, successfulScenarioValidation);
    }

    if (returnFhirpathValidationInResponse) {
      if (!successfulScenarioValidation) {
        throw scenarioValidationException;
      }
      return validScenario;
    }

    if (!successfulBasicValidation) {
      throw basicValidationException;
    }
    return "";
  }

  private void createMetric(
      String notificationType,
      boolean successfulBasicValidation,
      boolean successfulScenarioValidation) {
    if (notificationType.equals("laboratory")) {
      validationMetrics.countLabValResult(successfulBasicValidation, successfulScenarioValidation);
    } else {
      validationMetrics.countDisValResult(successfulBasicValidation, successfulScenarioValidation);
    }
  }
}
