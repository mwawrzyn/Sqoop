/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sqoop.json;

import static org.apache.sqoop.json.util.ConfigInputSerialization.extractConfigList;
import static org.apache.sqoop.json.util.ConfigBundleSerialization.extractConfigParamBundle;
import static org.apache.sqoop.json.util.ConfigBundleSerialization.restoreConfigParamBundle;

import static org.apache.sqoop.json.util.ConfigInputSerialization.restoreConfigs;
import static org.apache.sqoop.json.util.ConfigInputSerialization.restoreValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.sqoop.classification.InterfaceAudience;
import org.apache.sqoop.classification.InterfaceStability;
import org.apache.sqoop.common.Direction;
import org.apache.sqoop.json.util.ConfigInputConstants;
import org.apache.sqoop.model.MConfig;
import org.apache.sqoop.model.MConnector;
import org.apache.sqoop.model.MFromConfig;
import org.apache.sqoop.model.MLinkConfig;
import org.apache.sqoop.model.MToConfig;
import org.apache.sqoop.model.MValidator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Json representation of the connector object
 *
 */
@InterfaceAudience.Private
@InterfaceStability.Unstable
public class ConnectorBean extends ConfigurableBean {

  // to represent the config and inputs with values
  public static final String CONNECTOR_LINK_CONFIG = "link-config";
  public static final String CONNECTOR_JOB_CONFIG = "job-config";
  private static final String CONNECTOR = "connector";

  private List<MConnector> connectors;
  private Map<String, ResourceBundle> connectorConfigBundles;

  // for "extract"
  public ConnectorBean(List<MConnector> connectors, Map<String, ResourceBundle> bundles) {
    this.connectors = connectors;
    this.connectorConfigBundles = bundles;
  }

  // for "restore"
  public ConnectorBean() {
  }

  public List<MConnector> getConnectors() {
    return connectors;
  }

  public Map<String, ResourceBundle> getResourceBundles() {
    return connectorConfigBundles;
  }

  @SuppressWarnings("unchecked")
  @Override
  public JSONObject extract(boolean skipSensitive) {
    JSONObject connector = new JSONObject();
    connector.put(CONNECTOR, extractConnector(skipSensitive, connectors.get(0)));
    return connector;
  }

  @SuppressWarnings("unchecked")
  protected JSONArray extractConnectors(boolean skipSensitive) {
    JSONArray connectorArray = new JSONArray();
    for (MConnector connector : connectors) {
      connectorArray.add(extractConnector(skipSensitive, connector));
    }
    return connectorArray;
  }

  @SuppressWarnings("unchecked")
  private JSONObject extractConnector(boolean skipSensitive, MConnector connector) {
    JSONObject connectorJsonObject = new JSONObject();
    connectorJsonObject.put(ID, connector.getPersistenceId());
    connectorJsonObject.put(NAME, connector.getUniqueName());
    connectorJsonObject.put(CLASS, connector.getClassName());
    connectorJsonObject.put(CONFIGURABLE_VERSION, connector.getVersion());
    connectorJsonObject.put(
        CONNECTOR_LINK_CONFIG,
        extractConfigList(connector.getLinkConfig(), skipSensitive));

    connectorJsonObject.put(CONNECTOR_JOB_CONFIG, new JSONObject());
    // add sub fields to the job config for from and to
    if (connector.getFromConfig() != null) {
      ((JSONObject) connectorJsonObject.get(CONNECTOR_JOB_CONFIG)).put(
          Direction.FROM,
          extractConfigList(connector.getFromConfig(), skipSensitive));
    }
    if (connector.getToConfig() != null) {
      ((JSONObject) connectorJsonObject.get(CONNECTOR_JOB_CONFIG)).put(
          Direction.TO,
          extractConfigList(connector.getToConfig(), skipSensitive));
    }
    // add the config-param inside each connector
    connectorJsonObject.put(ALL_CONFIGS, new JSONObject());
    if (connectorConfigBundles != null && !connectorConfigBundles.isEmpty()) {
      connectorJsonObject.put(ALL_CONFIGS,
          extractConfigParamBundle(connectorConfigBundles.get(connector.getUniqueName())));
    }
    return connectorJsonObject;
  }

  @Override
  public void restore(JSONObject jsonObject) {
    connectors = new ArrayList<MConnector>();
    connectorConfigBundles = new HashMap<String, ResourceBundle>();
    JSONObject obj = JSONUtils.getJSONObject(jsonObject, CONNECTOR);
    connectors.add(restoreConnector(obj));
  }

  protected void restoreConnectors(JSONArray array) {
    connectors = new ArrayList<MConnector>();
    connectorConfigBundles = new HashMap<String, ResourceBundle>();
    for (Object obj : array) {
      connectors.add(restoreConnector(obj));
    }
  }

  private MConnector restoreConnector(Object obj) {
    JSONObject object = (JSONObject) obj;
    long connectorId = JSONUtils.getLong(object, ID);
    String uniqueName = JSONUtils.getString(object, NAME);
    String className = JSONUtils.getString(object, CLASS);
    String version = JSONUtils.getString(object, CONFIGURABLE_VERSION);

    JSONObject jsonLink = JSONUtils.getJSONObject(object, CONNECTOR_LINK_CONFIG);
    List<MConfig> linkConfigs = restoreConfigs(JSONUtils.getJSONArray(jsonLink, ConfigInputConstants.CONFIGS));
    List<MValidator> linkValidators = restoreValidator(JSONUtils.getJSONArray(jsonLink, ConfigInputConstants.CONFIG_VALIDATORS));

    // parent that encapsulates both the from/to configs
    JSONObject jobConfigJson = JSONUtils.getJSONObject(object, CONNECTOR_JOB_CONFIG);
    JSONObject fromJobConfigJson = jobConfigJson.containsKey(Direction.FROM.name()) ? JSONUtils.getJSONObject(jobConfigJson, Direction.FROM.name()) : null;
    JSONObject toJobConfigJson = jobConfigJson.containsKey(Direction.TO.name()) ? JSONUtils.getJSONObject(jobConfigJson, Direction.TO.name()) : null;

    MFromConfig fromConfig = null;
    MToConfig toConfig = null;
    if (fromJobConfigJson != null) {
      List<MConfig> fromLinkConfigs = restoreConfigs(JSONUtils.getJSONArray(fromJobConfigJson, ConfigInputConstants.CONFIGS));
      List<MValidator> fromLinkValidators = restoreValidator(JSONUtils.getJSONArray(fromJobConfigJson, ConfigInputConstants.CONFIG_VALIDATORS));
      fromConfig = new MFromConfig(fromLinkConfigs, fromLinkValidators);
    }
    if (toJobConfigJson != null) {
      List<MConfig> toLinkConfigs = restoreConfigs(JSONUtils.getJSONArray(toJobConfigJson, ConfigInputConstants.CONFIGS));
      List<MValidator> toLinkValidators = restoreValidator(JSONUtils.getJSONArray(toJobConfigJson, ConfigInputConstants.CONFIG_VALIDATORS));
      toConfig = new MToConfig(toLinkConfigs, toLinkValidators);
    }

    MLinkConfig linkConfig = new MLinkConfig(linkConfigs, linkValidators);
    MConnector connector = new MConnector(uniqueName, className, version, linkConfig, fromConfig,
        toConfig);

    connector.setPersistenceId(connectorId);
    if (object.containsKey(ALL_CONFIGS)) {
      JSONObject jsonConfigBundle = JSONUtils.getJSONObject(object, ALL_CONFIGS);
      connectorConfigBundles.put(uniqueName, restoreConfigParamBundle(jsonConfigBundle));
    }
    return connector;
  }
}
