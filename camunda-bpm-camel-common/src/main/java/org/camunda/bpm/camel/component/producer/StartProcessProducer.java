/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.camel.component.producer;

import org.apache.camel.Exchange;
import org.camunda.bpm.camel.common.CamundaBpmEndpoint;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.camel.common.CamundaBpmConstants.*;

/**
 * Starts a process instance given a process definition key.
 *
 * Example: camunda-bpm://start?processDefinitionKey=aProcessDefinitionKey
 *
 * @author Ryan Johnston (@rjfsu)
 * @author Tijs Rademakers (@tijsrademakers)
 * @author Rafael Cordones (@rafacm)
 */
public class StartProcessProducer extends CamundaBpmProducer {

  public final static String PROCESS_DEFINITION_KEY_PARAMETER = "processDefinitionKey";
  public final static String COPY_MESSAGE_PROPERTIES_PARAMETER = "copyProperties";
  public final static String COPY_MESSAGE_HEADERS_PARAMETER = "copyHeaders";
  public final static String COPY_MESSAGE_BODY_AS_PROCESS_VARIABLE_PARAMETER = "copyBodyAsVariable";

  private String processDefinitionKey;
  private ProcessEngine processEngine;
  private RuntimeService runtimeService;
  private Map<String, Object> parameters;

  public StartProcessProducer(CamundaBpmEndpoint endpoint, Map<String, Object> parameters) {
    super(endpoint, endpoint.getProcessEngine().getRuntimeService());
    this.processEngine = endpoint.getProcessEngine();
    this.runtimeService = processEngine.getRuntimeService();
    this.parameters = parameters;

    if (parameters.containsKey(PROCESS_DEFINITION_KEY_PARAMETER)) {
      this.processDefinitionKey = (String) parameters.get(PROCESS_DEFINITION_KEY_PARAMETER);
    } else {
      throw new IllegalArgumentException("You need to pass the '" + PROCESS_DEFINITION_KEY_PARAMETER + "' parameter! Parameters received: " + parameters);
    }
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    Map<String, Object> processVariables = new HashMap<String, Object>();
    if (parameters.containsKey(COPY_MESSAGE_PROPERTIES_PARAMETER)) {
      processVariables.putAll(exchange.getProperties());
    }
    if (parameters.containsKey(COPY_MESSAGE_HEADERS_PARAMETER)) {
      processVariables.putAll(exchange.getIn().getHeaders());
    }
    if (parameters.containsKey(COPY_MESSAGE_BODY_AS_PROCESS_VARIABLE_PARAMETER)) {
      String processVariable = (String) parameters.get(COPY_MESSAGE_BODY_AS_PROCESS_VARIABLE_PARAMETER);
      processVariables.put(processVariable, exchange.getIn().getBody());
    }
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(processDefinitionKey, processVariables);
    exchange.setProperty(CAMUNDA_BPM_PROCESS_INSTANCE_ID, instance.getProcessInstanceId());
    exchange.setProperty(CAMUNDA_BPM_PROCESS_DEFINITION_ID, instance.getProcessDefinitionId());
    exchange.getOut().setBody(instance.getId());
  }
}