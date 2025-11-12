package de.gematik.demis.lvs.common.fhirpath;

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

import ca.uhn.fhir.fhirpath.IFhirPath;
import java.util.List;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.PrimitiveType;

public class FhirPathExecutor {

  private FhirPathExecutor() {}

  /**
   * All parts of the scenario must be fulfilled for the message to be considered valid. Therefore,
   * we check every single FHIR PATH expression. If even one of them does not return a positive
   * result, the scenario is not fulfilled.
   *
   * @param notification
   * @param ls
   * @param fhirPath
   * @return
   */
  public static boolean isAtLeastOneFhirPathExpressionInvalid(
      Bundle notification, Scenario ls, IFhirPath fhirPath) {
    return ls.getFhirPathExpression().stream()
        .map(Scenario.FhirPathExpression::getFhirPath)
        .map(s -> fhirPath.evaluate(notification, s, BooleanType.class))
        .map(List::getFirst)
        .map(PrimitiveType::getValue)
        .anyMatch(aBoolean -> aBoolean.equals(false));
  }
}
