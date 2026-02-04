package de.gematik.demis.lvs.common.destination;

/*-
 * #%L
 * lifecycle-validation-service
 * %%
 * Copyright (C) 2025 - 2026 gematik GmbH
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
 * For additional notes and disclaimer from gematik and in case of changes by gematik,
 * find details in the "Readme" file.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NotificationCategoryDTOTest {

  @Test
  void constructorShouldSetNotificationCategory() {
    NotificationCategoryDTO dto = new NotificationCategoryDTO("category");
    assertEquals("category", dto.getNotificationCategory());
  }

  @Test
  void getNotificationCategoryShouldReturnSetValue() {
    NotificationCategoryDTO dto = new NotificationCategoryDTO("initialCategory");
    dto.setNotificationCategory("updatedCategory");
    assertEquals("updatedCategory", dto.getNotificationCategory());
  }
}
