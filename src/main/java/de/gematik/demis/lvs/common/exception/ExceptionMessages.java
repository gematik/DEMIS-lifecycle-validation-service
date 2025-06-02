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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

/** Definition of some Exception Messages */
public final class ExceptionMessages {
  private ExceptionMessages() {}

  public static final String EXCEPTION_MESSAGE_ONLY_POSITIVE_FINAL =
      "LVS-001: Final notifications are only allowed with a positive interpretation or a reference to another notification.";
  public static final String EXCEPTION_MESSAGE_ONLY_POSITIVE_PRELIMINARY =
      "LVS-002: Preliminary notifications are only allowed with a positive interpretation.";
  public static final String EXCEPTION_MESSAGE_ENTERED_IN_ERROR_NOT_ALLOWED =
      "LVS-003: Notification with BundleID %s has invalid status 'ENTERED_IN_ERROR'.";
  public static final String EXCEPTION_MESSAGE_NULL_STATUS_NOT_ALLOWED =
      "LVS-004: Notification with BundleID %s has invalid status 'NULL'.";
  public static final String EXCEPTION_MESSAGE_MISSING_STATUS =
      "LVS-005: Missing Status for Notification with BundleID %s.";
  public static final String EXCEPTION_MESSAGE_UNKNOWN_STATUS =
      "LVS-006: Notification with BundleID %s has unprocessable status: %s";

  public static final String EXCEPTION_MESSAGE_DIAGNOSTIC_REPORT_UNKNOWN =
      "LVS-007: Failed to extract Diagnostic Report from Notification";

  public static final String EXCEPTION_MESSAGE_ANONYMOUS_UNSUPPORTED =
      "Anonymous notifications are not supported";

  public static final String EXCEPTION_MESSAGE_PATHOGEN_NOT_SUPPORTED_FOR_ANONYMOUS =
      "LVS-008: Pathogen not supported for anonymous notifications";
}
