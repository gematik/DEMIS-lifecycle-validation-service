package de.gematik.demis.lvs.common.exception;

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

import static org.junit.jupiter.api.Assertions.*;

import org.hl7.fhir.exceptions.FHIRException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LifecycleValidationExceptionTest {
  @Test
  void runTestForCodeCoverage() {
    final var validationException = new LifecycleValidationException("My exception");
    Assertions.assertThrows(
        LifecycleValidationException.class,
        () -> {
          throw validationException;
        });
    final var validationException2 =
        new LifecycleValidationException("My exception", new FHIRException("FHIR Exception"));
    Assertions.assertThrows(
        LifecycleValidationException.class,
        () -> {
          throw validationException2;
        });
    final var validationException3 =
        new LifecycleValidationException(new Throwable("Generic Exception"));
    Assertions.assertThrows(
        LifecycleValidationException.class,
        () -> {
          throw validationException3;
        });
    final var validationException4 =
        new LifecycleValidationException(
            "My exception", new FHIRException("FHIR Exception"), true, true);
    Assertions.assertThrows(
        LifecycleValidationException.class,
        () -> {
          throw validationException4;
        });
  }
}
