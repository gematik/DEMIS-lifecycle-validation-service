package de.gematik.demis.lvs.labnotification.definitions;

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

import java.util.Optional;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;

/** Lists the values for the interpretation of a pathogen detection (Erregernachweis). */
public enum PathogenDetectionInterpretation {
  POSITIVE("POS"),
  NEGATIVE("NEG"),
  INDETERMINATE("IND");

  private final String code;

  PathogenDetectionInterpretation(String code) {
    this.code = code;
  }

  /**
   * Compares the interpretation code of the provided observation with the codes defined in {@code
   * PathogenDetectionInterpretation}. Returns an Optional with the enum, in case of a match.
   *
   * @param observation A pathogenDetection (Erregernachweis).
   * @return An optional that either contains a {@link PathogenDetectionInterpretation} enum or is
   *     empty.
   */
  public static Optional<PathogenDetectionInterpretation> getEnumFromObservation(
      Observation observation) {
    if (observation != null) {

      CodeableConcept interpretation = observation.getInterpretationFirstRep();
      Coding coding = interpretation.getCodingFirstRep();

      if (POSITIVE.getInterpretationCode().equals(coding.getCode())) {
        return Optional.of(PathogenDetectionInterpretation.POSITIVE);
      }
      if (NEGATIVE.getInterpretationCode().equals(coding.getCode())) {
        return Optional.of(PathogenDetectionInterpretation.NEGATIVE);
      }
      if (INDETERMINATE.getInterpretationCode().equals(coding.getCode())) {
        return Optional.of(PathogenDetectionInterpretation.INDETERMINATE);
      }
    }
    return Optional.empty();
  }

  /**
   * Returns the Interpretation Code.
   *
   * @return The code as a string value of the current enum instance.
   */
  public String getInterpretationCode() {
    return code;
  }

  /**
   * Checks, if the interpretation can be defined as a positive result. {@link #POSITIVE} is thereby
   * defined as positive, {@link #NEGATIVE} and {@link #INDETERMINATE} as non-positive.
   *
   * @return True, if the current enum instance defines a positive result. False, otherwise.
   */
  public boolean isPositive() {
    return this == POSITIVE;
  }
}
