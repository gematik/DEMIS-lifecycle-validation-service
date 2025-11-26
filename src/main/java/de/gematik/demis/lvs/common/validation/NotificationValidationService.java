package de.gematik.demis.lvs.common.validation;

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

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.common.fhir.NotificationHelper;
import de.gematik.demis.lvs.common.fhirpath.Scenario;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.CheckForNull;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.springframework.http.MediaType;

@Slf4j
public class NotificationValidationService<S extends Scenario> {

  private final NotifcationBasicValidator notificationBasicValidationService;
  private final NotificationScenarioValidationService<S> notificationScenarioValidationService;
  private final boolean returnFhirpathValidationInResponse;
  private final FhirParser fhirParser;

  public NotificationValidationService(
      final NotifcationBasicValidator notificationBasicValidationService,
      final NotificationScenarioValidationService<S> notificationScenarioValidationService,
      boolean returnFhirpathValidationInResponse,
      final FhirParser fhirParser) {
    this.notificationBasicValidationService = notificationBasicValidationService;
    this.notificationScenarioValidationService = notificationScenarioValidationService;
    this.returnFhirpathValidationInResponse = returnFhirpathValidationInResponse;
    this.fhirParser = fhirParser;
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
      @CheckForNull final String principalId) {
    String validScenario = null;

    RuntimeException basicValidationException = null;
    boolean successfulBasicValidation = false;

    LifecycleValidationException scenarioValidationException = null;
    boolean successfulScenarioValidation = false;

    final Bundle fhirMessage = parseStringToBundle(notification, mediaType);

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

    if (!returnFhirpathValidationInResponse
        && (successfulBasicValidation != successfulScenarioValidation)) {
      logBasicAndScenarioValidationDifference(
          principalId, fhirMessage, successfulBasicValidation, successfulScenarioValidation);
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

  private void logBasicAndScenarioValidationDifference(
      String principalId,
      Bundle fhirMessage,
      boolean successfulBasicValidation,
      boolean successfulScenarioValidation) {
    log.info(
        "Outcome of basic and scenario validation don't match for notification with id {} from sender {}.",
        getNotificationId(fhirMessage),
        Objects.requireNonNullElse(principalId, "<unknown>"));
    log.info(
        "Basic validation: {}, scenario validation: {}",
        successfulBasicValidation ? "success" : "failed",
        successfulScenarioValidation ? "success" : "failed");
  }

  private Bundle parseStringToBundle(String notification, MediaType mediaType) {
    final var contentType =
        MediaType.parseMediaType(mediaType.getType() + "/" + mediaType.getSubtype());
    return fhirParser.parseBundleOrParameter(notification, contentType.getSubtype());
  }

  private String getNotificationId(final Bundle bundle) {
    String notificationId = "<unknown>";
    final Optional<Composition> composition = NotificationHelper.extractComposition(bundle);
    if (composition.isPresent()) {
      notificationId = composition.get().getIdentifier().getValue();
    }
    return notificationId;
  }
}
