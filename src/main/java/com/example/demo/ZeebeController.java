package com.example.demo;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.impl.BpmnModelInstanceImpl;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("zeebe")
public class ZeebeController {

    private static Logger log = LoggerFactory.getLogger(DemoApplication.class);

    @Autowired
    private ZeebeClient client;

    @PostMapping("/deploy")
    public void deploy(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        File file = new File("C:\\Users\\Suporte\\Desktop\\Codigos Java\\demo\\src\\main\\resources\\demoProcess.bpmn");
//        FileUtils.writeByteArrayToFile(file, multipartFile.getBytes());

        System.out.println("AQuiiiiii");
        BpmnModelInstance model = Bpmn.readModelFromFile(file);

        client.newDeployResourceCommand()
                .addProcessModel(model, "demoProcess.bpmn")
                .send()
                .join();
    }

    @PostMapping("/start/{bpmnProcessId}")
    public void startProcess(@PathVariable("bpmnProcessId") String bpmnProcessId, @RequestBody String variables) {
        final ProcessInstanceEvent event =
                client
                        .newCreateInstanceCommand()
                        .bpmnProcessId(bpmnProcessId)
                        .latestVersion()
                        .variables(variables)
                        .send()
                        .join();


        log.info(
                "started instance for workflowKey='{}', bpmnProcessId='{}', version='{}' with workflowInstanceKey='{}'",
                event.getProcessDefinitionKey(),
                event.getBpmnProcessId(),
                event.getVersion(),
                event.getProcessInstanceKey());
    }
}
