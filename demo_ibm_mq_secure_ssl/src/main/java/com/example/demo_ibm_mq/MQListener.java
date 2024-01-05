package com.example.demo_ibm_mq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.owasp.security.logging.util.SecurityUtil;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MQListener {
	@JmsListener(destination = "${project.mq.queue-name}")
	public void receiveMessage(final Message jsonMessage) throws JMSException{
		String messageData = null;
		if(jsonMessage instanceof TextMessage) {
			TextMessage textMessage = (TextMessage)jsonMessage;
			messageData = textMessage.getText();
			SecurityUtil.logMessage("Received:" + messageData);
		}
	}
}
