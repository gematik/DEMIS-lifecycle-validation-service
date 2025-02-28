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

import static de.gematik.demis.lvs.common.exception.ExceptionMessages.EXCEPTION_MESSAGE_ENTERED_IN_ERROR_NOT_ALLOWED;
import static de.gematik.demis.lvs.common.exception.ExceptionMessages.EXCEPTION_MESSAGE_NULL_STATUS_NOT_ALLOWED;

import de.gematik.demis.lvs.common.exception.ExceptionMessages;
import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Composition;

@RequiredArgsConstructor
public class AnonymousNotificationValidator implements NotificationValidator {
  public static final String COVID_PATHOGEN = "cvdp";

  private final String bundleId;
  private final Composition.CompositionStatus status;
  private final List<String> pathogens;

  @Override
  public void assertIsValid() {
    switch (status) {
      // All other statuses are not valid in the life cycle management
      case ENTEREDINERROR ->
          throw new LifecycleValidationException(
              String.format(EXCEPTION_MESSAGE_ENTERED_IN_ERROR_NOT_ALLOWED, bundleId));
      case NULL ->
          throw new LifecycleValidationException(
              String.format(EXCEPTION_MESSAGE_NULL_STATUS_NOT_ALLOWED, bundleId));
      default -> {
        boolean isCovid = pathogens.contains(COVID_PATHOGEN);
        if (!isCovid) {
          throw new LifecycleValidationException(
              ExceptionMessages.EXCEPTION_MESSAGE_PATHOGEN_NOT_SUPPORTED_FOR_ANONYMOUS);
        }
      }
    }
  }
}
