package de.gematik.demis.lvs.common.fhir;

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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

/** Helper Class for handling FHIR Bundles. */
public final class NotificationHelper {

  public static final String ANONYMOUS_LABORATORY_NOTIFICATION_PROFILE =
      "https://demis.rki.de/fhir/StructureDefinition/NotificationLaboratoryNegative";

  private NotificationHelper() {}

  /**
   * Utility method to extract a {@link Composition} from a {@link Bundle} object.
   *
   * @param bundle the bundle to be analyzed
   * @return an instance of {@link Composition} or nothing, if it doesn't exist
   */
  public static Optional<Composition> extractComposition(final Bundle bundle) {
    final Resource notificationLaboratoryResource = bundle.getEntryFirstRep().getResource();

    if (notificationLaboratoryResource instanceof Composition composition) {
      return Optional.of(composition);
    }

    return Optional.empty();
  }

  /**
   * Utility method to extract a {@link DiagnosticReport} from a {@link Composition} object.
   *
   * @param notificationLaboratory the Composition object to be analyzed
   * @return an instance of {@link DiagnosticReport} or nothing, if it doesn't exist
   */
  public static Optional<DiagnosticReport> extractDiagnosticReport(
      final Composition notificationLaboratory) {

    final List<Composition.SectionComponent> compositionSections =
        notificationLaboratory.getSection();
    for (Composition.SectionComponent section : compositionSections) {
      final List<Reference> references = section.getEntry();
      for (final Reference reference : references) {
        IBaseResource referenceResource = reference.getResource();
        if (referenceResource instanceof DiagnosticReport diagnosticReport) {
          return Optional.of(diagnosticReport);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Utility method to extract a set of {@link Observation} from a {@link DiagnosticReport} object.
   *
   * @param diagnosticReport the DiagnosticReport object to be analyzed
   * @return a set of {@link Observation}
   */
  public static Set<Observation> extractPathogenDetectionSetFromDiagnosticReport(
      final DiagnosticReport diagnosticReport) {
    Set<Observation> pathogenDetectionSet = new HashSet<>();
    for (Reference reference : diagnosticReport.getResult()) {
      if (reference.hasReference()) {
        IBaseResource referenceResource = reference.getResource();
        if (referenceResource instanceof Observation observation) {
          pathogenDetectionSet.add(observation);
        }
      }
    }
    return pathogenDetectionSet;
  }

  public static List<String> extractPathogenCodes(final DiagnosticReport report) {
    return report.getCode().getCoding().stream()
        .map(Coding::getCode)
        .map(s -> s.toLowerCase(Locale.ROOT))
        .toList();
  }
}
