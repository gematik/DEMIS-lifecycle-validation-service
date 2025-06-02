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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 * #L%
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.demis.lvs.disease.configmodel.Scenario;
import de.gematik.demis.lvs.disease.configmodel.ScenariosWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class AllowListPreparator {

  private final String pathToConfig;
  private final ObjectMapper objectMapper;
  @Getter private List<Scenario> allowList;

  public AllowListPreparator(String pathToConfig, ObjectMapper objectMapper) {
    this.pathToConfig = pathToConfig;
    this.objectMapper = objectMapper;
  }

  public AllowListPreparator build() throws IOException {
    // read file and parse it to Scenario objects

    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource(pathToConfig);
    String string = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);

    // parse string to list of Scenario objects
    ScenariosWrapper scenariosWrapper = objectMapper.readValue(string, ScenariosWrapper.class);
    allowList = scenariosWrapper.scenarios();

    return this;
  }
}
