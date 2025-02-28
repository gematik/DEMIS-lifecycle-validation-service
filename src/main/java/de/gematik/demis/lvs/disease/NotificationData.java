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
 * #L%
 */

import static de.gematik.demis.lvs.common.fhir.NotificationHelper.extractComposition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

@Slf4j
public record NotificationData(
    Composition composition,
    Condition condition,
    List<QuestionnaireResponse> questionnaireResponseList) {

  static NotificationData extractRelevantData(Bundle notification) {
    Optional<Composition> compositionOptional = extractComposition(notification);
    Composition composition = null;
    Condition condition = null;
    List<QuestionnaireResponse> questionnaireResponseList = new ArrayList<>();

    if (compositionOptional.isPresent()) {
      composition = compositionOptional.get();
      List<Composition.SectionComponent> sections = compositionOptional.get().getSection();
      for (Composition.SectionComponent section : sections) {
        IBaseResource resource = section.getEntry().getFirst().getResource();
        if (resource instanceof Condition condition1) {
          condition = condition1;
        } else if (resource instanceof QuestionnaireResponse questionnaireResponse) {
          questionnaireResponseList.add(questionnaireResponse);
        }
      }
    }
    return new NotificationData(composition, condition, questionnaireResponseList);
  }
}
