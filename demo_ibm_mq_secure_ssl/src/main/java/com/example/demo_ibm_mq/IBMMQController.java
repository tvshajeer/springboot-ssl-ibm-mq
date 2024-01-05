package com.example.demo_ibm_mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IBMMQController {
	
	@Value("${project.mq.queue-name}")
    private String queueName;
	
	@Autowired
	private JmsOperations jmsOperations;
	
	@GetMapping("/send")
    public String send(){
		jmsOperations.convertAndSend(queueName, "Helluuuuuu world!!!");
        return "Send message successfully";
    }
}
