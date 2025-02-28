package de.gematik.demis.lvs.disease.configmodel;

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

import java.util.List;
import org.hl7.fhir.r4.model.Condition;

public record ConditionRule(
    List<String> clinicalStatus, List<String> verificationStatus, String note) {

  public boolean compareConditionData(Condition condition) {
    boolean clinicalStatus = checkClinicalStatus(condition);
    String verificationStatus =
        condition.getVerificationStatus().getCodingFirstRep().getCode().toLowerCase();
    boolean additionalCheck =
        this.note == null
            || this.note.equals("not_empty")
                && condition.getNoteFirstRep() != null
                && condition.getNoteFirstRep().getText() != null
                && !condition.getNoteFirstRep().getText().isEmpty();
    return clinicalStatus
        && this.verificationStatus.contains(verificationStatus)
        && additionalCheck;
  }

  private boolean checkClinicalStatus(Condition condition) {
    String code = condition.getClinicalStatus().getCodingFirstRep().getCode();
    return code == null || this.clinicalStatus.contains(code.toLowerCase());
  }
}
