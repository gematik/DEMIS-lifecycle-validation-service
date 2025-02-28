package de.gematik.demis.lvs.labnotification;

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

import static de.gematik.demis.lvs.common.exception.ExceptionMessages.EXCEPTION_MESSAGE_ANONYMOUS_UNSUPPORTED;
import static de.gematik.demis.lvs.common.exception.ExceptionMessages.EXCEPTION_MESSAGE_DIAGNOSTIC_REPORT_UNKNOWN;
import static de.gematik.demis.lvs.common.exception.ExceptionMessages.EXCEPTION_MESSAGE_MISSING_STATUS;
import static de.gematik.demis.lvs.common.fhir.NotificationHelper.ANONYMOUS_LABORATORY_NOTIFICATION_PROFILE;

import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import de.gematik.demis.fhirparserlibrary.FhirParser;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import de.gematik.demis.lvs.common.fhir.NotificationHelper;
import de.gematik.demis.lvs.labnotification.definitions.PathogenDetectionInterpretation;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/** Service validating the lifecycle of Laboratory Notifications. */
@Service
@Slf4j
class LaboratoryNotificationValidationService {

  private final boolean isAcceptingAnonymousNotifications;
  private final FhirParser fhirParser;

  public LaboratoryNotificationValidationService(
      final FhirParser fhirParser,
      @Value("${feature.flag.accepting_anonymous_notifications:false}")
          final boolean isAcceptingAnonymousNotifications) {
    this.isAcceptingAnonymousNotifications = isAcceptingAnonymousNotifications;
    this.fhirParser = fhirParser;
  }

  public void validate(final String bundle, final MediaType mediaType) {
    final var contentType =
        MediaType.parseMediaType(mediaType.getType() + "/" + mediaType.getSubtype());
    final Bundle fhirMessage = fhirParser.parseBundleOrParameter(bundle, contentType.getSubtype());
    validate(fhirMessage);
  }

  public void validate(final Bundle bundle) {
    checkStatusAndInterpretation(bundle);
    log.info(
        "Lifecycle Validation of Notification with BundleID {} successful",
        bundle.getIdentifier().getValue());
  }

  private void checkStatusAndInterpretation(final Bundle bundle) throws InternalErrorException {
    final String bundleId = bundle.getIdentifier().getValue();
    log.info("Checking Status and Interpretation for Notification with BundleID: {}", bundleId);

    final Composition composition = getComposition(bundle);
    final boolean isAnonymous =
        composition.getMeta().hasProfile(ANONYMOUS_LABORATORY_NOTIFICATION_PROFILE);
    if (isAnonymous && !isAcceptingAnonymousNotifications) {
      throw new LifecycleValidationException(EXCEPTION_MESSAGE_ANONYMOUS_UNSUPPORTED);
    }

    final DiagnosticReport diagnosticReport = getDiagnosticReport(composition);
    final Composition.CompositionStatus status = composition.getStatus();
    log.debug("Composition Status: {}", status.toCode());

    final NotificationValidator validator;
    if (isAnonymous) {
      final List<String> pathogens = getPathogens(diagnosticReport);
      validator = new AnonymousNotificationValidator(bundleId, status, pathogens);
    } else {
      final boolean hasRelatesTo = composition.hasRelatesTo();
      validator =
          new RegularNotificationValidator(
              bundleId, status, isPositive(diagnosticReport), hasRelatesTo);
    }
    validator.assertIsValid();
  }

  private List<String> getPathogens(final DiagnosticReport diagnosticReport) {
    return NotificationHelper.extractPathogenCodes(diagnosticReport);
  }

  private DiagnosticReport getDiagnosticReport(final Composition composition) {
    return NotificationHelper.extractDiagnosticReport(composition)
        .orElseThrow(
            () -> new LifecycleValidationException(EXCEPTION_MESSAGE_DIAGNOSTIC_REPORT_UNKNOWN));
  }

  /**
   * Gets all pathogen detections (Erregernachweise) from the provided bundle and checks, if any of
   * them is isPositive.
   *
   * @return True, if any pathogen detection (Erregernachweis) is isPositive with a pathogen,
   *     otherwise false.
   */
  private boolean isPositive(final DiagnosticReport diagnosticReport) {
    final var pathogenDetectionSet =
        NotificationHelper.extractPathogenDetectionSetFromDiagnosticReport(diagnosticReport);
    return pathogenDetectionSet.stream().anyMatch(this::isPositive);
  }

  /**
   * Checks if the provided pathogen detection (Erregernachweis) is isPositive.
   *
   * @param pathogenDetection Incoming observation (pathogenDetection/Erregernachweis) for
   *     processing.
   * @return True, if the provided pathogen detection (Erregernachweis) is isPositive with a
   *     pathogen, otherwise false.
   */
  private boolean isPositive(Observation pathogenDetection) {
    Optional<PathogenDetectionInterpretation> interpretation =
        PathogenDetectionInterpretation.getEnumFromObservation(pathogenDetection);

    return interpretation.filter(PathogenDetectionInterpretation::isPositive).isPresent();
  }

  private Composition getComposition(final Bundle bundle) {
    final String bundleId = bundle.getIdentifier().getValue();
    final Composition composition =
        NotificationHelper.extractComposition(bundle)
            .orElseThrow(
                () ->
                    new LifecycleValidationException(
                        String.format("Could not extract Composition from Bundle %s", bundleId)));

    if (!composition.hasStatus()) {
      throw new LifecycleValidationException(
          String.format(EXCEPTION_MESSAGE_MISSING_STATUS, bundleId));
    }

    return composition;
  }
}
