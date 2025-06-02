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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.demis.lvs.common.exception.LifecycleValidationException;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Composition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RegularNotificationValidatorTest {

  private static final String BUNDLE_ID = "b4bd6b04-1058-4941-8776-b4ba45f2ee86";

  private static RegularNotificationValidator validatorWithStatus(
      final Composition.CompositionStatus status) {
    return new RegularNotificationValidator(BUNDLE_ID, status, true, true);
  }

  private static Stream<Arguments> invalidCompositionStatus() {
    return Stream.of(
        arguments(
            Composition.CompositionStatus.ENTEREDINERROR,
            "LVS-003: Notification with BundleID b4bd6b04-1058-4941-8776-b4ba45f2ee86 has invalid status 'ENTERED_IN_ERROR'."),
        arguments(
            Composition.CompositionStatus.NULL,
            "LVS-004: Notification with BundleID b4bd6b04-1058-4941-8776-b4ba45f2ee86 has invalid status 'NULL'."));
  }

  @Test
  void acceptFinalNotificationIfPositive() {
    final RegularNotificationValidator regularNotificationValidator =
        new RegularNotificationValidator(
            BUNDLE_ID, Composition.CompositionStatus.FINAL, true, false);
    assertDoesNotThrow(regularNotificationValidator::assertIsValid);
  }

  @Test
  void acceptFinalNotificationWithRelatesToIfNegative() {
    RegularNotificationValidator regularNotificationValidator =
        new RegularNotificationValidator(
            BUNDLE_ID, Composition.CompositionStatus.FINAL, false, true);
    assertDoesNotThrow(regularNotificationValidator::assertIsValid);

    regularNotificationValidator =
        new RegularNotificationValidator(
            BUNDLE_ID, Composition.CompositionStatus.FINAL, false, false);
    assertThrows(LifecycleValidationException.class, regularNotificationValidator::assertIsValid);
  }

  @Test
  void acceptPreliminaryOnlyIfPositive() {
    RegularNotificationValidator regularNotificationValidator =
        new RegularNotificationValidator(
            BUNDLE_ID, Composition.CompositionStatus.PRELIMINARY, true, false);
    assertDoesNotThrow(regularNotificationValidator::assertIsValid);

    regularNotificationValidator =
        new RegularNotificationValidator(
            BUNDLE_ID, Composition.CompositionStatus.PRELIMINARY, false, false);
    assertThrows(LifecycleValidationException.class, regularNotificationValidator::assertIsValid);
  }

  @ParameterizedTest
  @MethodSource("invalidCompositionStatus")
  void throwsExceptionForInvalidStatus(
      final Composition.CompositionStatus status, final String expectedMessage) {
    final RegularNotificationValidator validator = validatorWithStatus(status);
    final LifecycleValidationException throwable =
        catchThrowableOfType(LifecycleValidationException.class, validator::assertIsValid);
    assertThat(throwable).hasMessage(expectedMessage);
  }
}
