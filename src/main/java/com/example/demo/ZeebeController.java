package com.example.demo;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("zeebe")
public class ZeebeController {

    private static Logger log = LoggerFactory.getLogger(DemoApplication.class);

    @Autowired
    private ZeebeClient client;

//  Endoint que faz deploy de um flow enviando um arquivo .bpmn.
//    @PostMapping("/deploy")
//    public void deploy(@RequestParam("file") MultipartFile multipartFile) throws IOException {
    @PostMapping("/deploy/{bpmnProcessId}")
    public void deploy(@PathVariable("bpmnProcessId") String  bpmnProcessId) throws IOException {
        File file = new File("C:\\Users\\Suporte\\Desktop\\Codigos Java\\demo\\src\\main\\resources\\" + bpmnProcessId +".bpmn");
//        FileUtils.writeByteArrayToFile(file, multipartFile.getBytes());

        BpmnModelInstance model = Bpmn.readModelFromFile(file);

        client.newDeployResourceCommand()
                .addProcessModel(model, bpmnProcessId + ".bpmn")
                .send()
                .join();
    }

//  Endpoint que inicia uma instancia de um modelo de flow que já foi feito o deploy.
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

//  Endpoint que cancela uma instancia iniciada.
    @PostMapping("/cancel/{processInstanceId}")
    public void cancelInstance(@PathVariable("processInstanceId") Long processInstanceId) {
        client.newCancelInstanceCommand(processInstanceId)
                .send();

        log.info(
                "Cancelling instance of id = {}",
                processInstanceId
        );

    }

//  Endpoint para criar os worker que sao responsaveis por finalizar as Services Tasks
    @PostMapping("/complete/{jobType}")
    public void createWorker(@PathVariable("jobType") String jobType) {
        TestHandler handler = new TestHandler();

        JobWorker worker = client.newWorker().jobType(jobType)
                .handler(handler)
                .name("worker")
                .open();

    }

//  Enpoint que deleta um modelo de flow.
    @DeleteMapping("delete/{bpmnProcessId}")
    public void deleteProcess (@PathVariable("bpmnProcessId") Long bpmnProcessId) {
        client.newDeleteResourceCommand(bpmnProcessId).send().join();
    }
}
