package de.gematik.demis.lvs.common.externalchecks;

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

import static de.gematik.demis.lvs.common.exception.ExceptionMessages.EXCEPTION_MESSAGE_NOTIFICATION_CATEGORY_MISMATCH;

import ca.uhn.fhir.fhirpath.IFhirPath;
import de.gematik.demis.lvs.common.codemapping.CodeMappingService;
import de.gematik.demis.lvs.common.destination.DestinationLookupServiceClient;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.common.fhirpath.Scenario;
import de.gematik.demis.lvs.common.util.UUIDValidator;
import feign.FeignException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Executes the configured externalChecks of a scenario. All checks must return true (AND),
 * otherwise false.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdditionalOperationExecuter {

  public static final String NOTIFICATION_ID = "notificationId";
  public static final String RELATES_TO_ID = "relatesToId";
  public static final String HAS_TO_EXIST = "hasToExist";
  public static final String NOTIFICATION_CATEGORY = "notificationCategory";
  private final CodeMappingService codeMappingService;
  private final DestinationLookupServiceClient destinationLookupServiceClient;

  public boolean checkAllExternalChecks(
      final Bundle bundle, final Scenario scenario, final IFhirPath fhirPath) {
    for (Scenario.ExternalCheckConfig externalCheckConfig : scenario.getExternalChecks()) {
      if (!isExternalCheckValid(externalCheckConfig, bundle, fhirPath)) return false;
    }
    return true;
  }

  private boolean isExternalCheckValid(
      final Scenario.ExternalCheckConfig check,
      final Bundle notification,
      final IFhirPath fhirPath) {
    final Map<String, Object> inputs = check.getInputs();
    return switch (check.getType()) {
      case NOTIFICATION_ID_CATEGORY_MAPPING -> {
        log.debug("running category check with notification id");
        yield checkNotificationCategoryWithNotificationIdAndDLSData(inputs, notification, fhirPath);
      }
      case RELATES_TO_ID_CATEGORY_MAPPING -> {
        log.debug("running category check with relatesTo id");
        yield checkNotificationCategoryWithRelatesToIdAndDLSData(inputs, notification, fhirPath);
      }
      case NOTIFICATION_ID_NOT_EXISTING -> {
        log.debug("not existing check");
        yield checkNotificationIdDoesNotExist(inputs, notification, fhirPath);
      }
    };
  }

  /**
   * NotificationCategories differ between disease and pathogen notifications on the 4th position.
   * Therefore we map the notification category to the transmission code that is also used in the
   * pseudonymization process. The goal is to allow followup notifications between §6.1 and §7.1 as
   * well as between §7.3 disease and §7.3 pathogen notifications
   *
   * @param inputs
   * @param notification
   * @param fhirPath
   * @return
   */
  private boolean checkNotificationCategoryWithNotificationIdAndDLSData(
      final Map<String, Object> inputs, final Bundle notification, final IFhirPath fhirPath) {
    final String notificationId = getIdFromBundle(notification, inputs, fhirPath, NOTIFICATION_ID);
    final String notificationCategoryFromDLS = getNotificationCategoryFromDLS(notificationId);
    return notificationCategoryFromDLS == null
        || compareMappedNotificationCategories(
            notification, fhirPath, inputs, notificationCategoryFromDLS);
  }

  /**
   * @param inputs
   * @param notification
   * @param fhirPath
   * @return
   */
  private boolean checkNotificationCategoryWithRelatesToIdAndDLSData(
      final Map<String, Object> inputs, final Bundle notification, final IFhirPath fhirPath) {
    final String relatesToId = getIdFromBundle(notification, inputs, fhirPath, RELATES_TO_ID);
    boolean hasToExist = (boolean) inputs.get(HAS_TO_EXIST);
    final String notificationCategoryFromDLS = getNotificationCategoryFromDLS(relatesToId);
    if (notificationCategoryFromDLS == null) {
      return !hasToExist;
    }
    return compareMappedNotificationCategories(
        notification, fhirPath, inputs, notificationCategoryFromDLS);
  }

  private boolean compareMappedNotificationCategories(
      final Bundle notification,
      final IFhirPath fhirPath,
      final Map<String, Object> inputs,
      final String notificationCategoryFromDLS) {
    final String mappedCategoryCodeFromDLS =
        codeMappingService.getSurvNetCode(notificationCategoryFromDLS);
    final String notificationCategoryPath = (String) inputs.get(NOTIFICATION_CATEGORY);
    final String notificationCategory =
        fhirPath
            .evaluate(notification, notificationCategoryPath, StringType.class)
            .getFirst()
            .getValue();
    final String mappedCategoryCode = codeMappingService.getSurvNetCode(notificationCategory);
    if (!mappedCategoryCode.equals(mappedCategoryCodeFromDLS)) {
      throw new LifecycleValidationException(EXCEPTION_MESSAGE_NOTIFICATION_CATEGORY_MISMATCH);
    }
    return true;
  }

  /**
   * @param inputs
   * @param notification
   * @param fhirPath
   * @return
   */
  private boolean checkNotificationIdDoesNotExist(
      final Map<String, Object> inputs, final Bundle notification, final IFhirPath fhirPath) {
    final String notificationId = getIdFromBundle(notification, inputs, fhirPath, NOTIFICATION_ID);
    return getNotificationCategoryFromDLS(notificationId) == null;
  }

  /**
   * @param notificationId
   * @return
   */
  private String getNotificationCategoryFromDLS(final String notificationId) {
    try {
      if (UUIDValidator.isValidUUID(notificationId)) {
        return destinationLookupServiceClient
            .getNotificationCategory(notificationId)
            .getNotificationCategory();
      }
      return null;
    } catch (FeignException.FeignClientException e) {
      if (e.status() == HttpStatus.NOT_FOUND.value()) {
        log.warn("Notification ID {} not found in DLS", notificationId);
        return null;
      } else {
        throw e;
      }
    }
  }

  /**
   * @param notification
   * @param inputs
   * @param fhirPath
   * @param idType
   * @return
   */
  private String getIdFromBundle(
      final Bundle notification,
      final Map<String, Object> inputs,
      final IFhirPath fhirPath,
      final String idType) {
    final String notificationIdPath = (String) inputs.get(idType);
    final Optional<StringType> id =
        fhirPath.evaluate(notification, notificationIdPath, StringType.class).stream().findFirst();
    return id.map(PrimitiveType::getValue).orElse(null);
  }
}
