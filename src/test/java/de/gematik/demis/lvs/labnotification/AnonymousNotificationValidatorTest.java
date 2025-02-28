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

import static de.gematik.demis.lvs.labnotification.AnonymousNotificationValidator.COVID_PATHOGEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import java.util.List;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Composition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnonymousNotificationValidatorTest {

  private static final String BUNDLE_ID = "d03611d1-08bd-4e4f-9a1a-99416de375ba";

  private static Stream<Arguments> invalidCompositionStatus() {
    return Stream.of(
        arguments(
            Composition.CompositionStatus.ENTEREDINERROR,
            "LVS-003: Notification with BundleID d03611d1-08bd-4e4f-9a1a-99416de375ba has invalid status 'ENTERED_IN_ERROR'."),
        arguments(
            Composition.CompositionStatus.NULL,
            "LVS-004: Notification with BundleID d03611d1-08bd-4e4f-9a1a-99416de375ba has invalid status 'NULL'."));
  }

  private static AnonymousNotificationValidator validatorWithStatus(
      final Composition.CompositionStatus status) {
    return new AnonymousNotificationValidator(BUNDLE_ID, status, List.of(COVID_PATHOGEN));
  }

  private static AnonymousNotificationValidator validatorWithPathogens(
      final List<String> pathogens) {
    return new AnonymousNotificationValidator(
        BUNDLE_ID, Composition.CompositionStatus.FINAL, pathogens);
  }

  @ParameterizedTest
  @MethodSource("invalidCompositionStatus")
  void someCompositionStatusAreRejected(
      final Composition.CompositionStatus status, final String expectedMessage) {
    final AnonymousNotificationValidator validator = validatorWithStatus(status);
    final LifecycleValidationException throwable =
        catchThrowableOfType(validator::assertIsValid, LifecycleValidationException.class);
    assertThat(throwable).hasMessage(expectedMessage);
  }

  @Test
  void notificationsWithMultiplePathogensAreValid() {
    final AnonymousNotificationValidator validator =
        validatorWithPathogens(List.of(COVID_PATHOGEN, "other"));
    assertDoesNotThrow(validator::assertIsValid);
  }

  @Test
  void onlyCovidIsAllowed() {
    AnonymousNotificationValidator validator = validatorWithPathogens(List.of("unsupported"));
    assertThrows(LifecycleValidationException.class, validator::assertIsValid);

    validator = validatorWithPathogens(List.of(COVID_PATHOGEN));
    assertDoesNotThrow(validator::assertIsValid);
  }
}
