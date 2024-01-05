package com.example.demo_ibm_mq;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.JmsTemplate;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

@Configuration
public class MQConfiguration {
	@Value("${project.mq.host}")
    private String host;
    @Value("${project.mq.port}")
    private Integer port;
    @Value("${project.mq.queue-manager}")
    private String queueManager;
    @Value("${project.mq.channel}")
    private String channel;
    @Value("${project.mq.receive-timeout}")
    private long receiveTimeout;
    @Value("${project.mq.username}")
    private String username;
    @Value("${project.mq.password}")
    private String password;
    @Value("${project.mq.ssl-cipher-suite}")
    private String sslCipherSuite;
    @Value("${project.mq.clientStorePath}")
    private String clientStorePath;
    @Value("${project.mq.clientStorePassword}")
    private String clientStorePassword;
    
    @Bean
    public SSLSocketFactory sslSocketFactory() throws Exception {
    	String storeType = System.getProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType());
    	// create manager factory
    	TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    	KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    	char[] password = null;
    	if(clientStorePassword.length() > 0) password = clientStorePassword.toCharArray();
    	// create trust store
    	KeyStore trustStore = KeyStore.getInstance(storeType);
    	trustStore.load(new FileInputStream(clientStorePath), password);
    	tmf.init(trustStore);
    	// create key store
    	KeyStore keyStore = KeyStore.getInstance(storeType);
    	keyStore.load(new FileInputStream(clientStorePath), password);
    	kmf.init(keyStore, password);
    	// init SSL Context
    	SSLContext sslContext = SSLContext.getInstance("TLS");
    	sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    	return sslContext.getSocketFactory();
    }
    
    @Bean
    public MQQueueConnectionFactory mqQueueConnectionFactory(SSLSocketFactory sslSocketFactory) {
    	System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
//    	System.setProperty("javax.net.ssl.trustStore", clientStorePath);
//    	System.setProperty("javax.net.ssl.trustStorePassword", clientStorePassword);
//    	System.setProperty("javax.net.ssl.keyStore", clientStorePath);
//    	System.setProperty("javax.net.ssl.keyStorePassword", clientStorePassword);
    	
        MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
        mqQueueConnectionFactory.setHostName(host);
        try {
        	mqQueueConnectionFactory.setSSLCipherSuite(sslCipherSuite);
        	mqQueueConnectionFactory.setSSLSocketFactory(sslSocketFactory);
            mqQueueConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
            mqQueueConnectionFactory.setCCSID(1208);
            mqQueueConnectionFactory.setChannel(channel);
            mqQueueConnectionFactory.setPort(port);
            mqQueueConnectionFactory.setQueueManager(queueManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mqQueueConnectionFactory;
    }
    
    @Bean
    UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter(MQQueueConnectionFactory mqQueueConnectionFactory) {
        UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
        userCredentialsConnectionFactoryAdapter.setUsername(username);
        userCredentialsConnectionFactoryAdapter.setPassword(password);
        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqQueueConnectionFactory);
        return userCredentialsConnectionFactoryAdapter;
    }
    
    @Bean
    @Primary
    public CachingConnectionFactory cachingConnectionFactory(UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(userCredentialsConnectionFactoryAdapter);
        cachingConnectionFactory.setSessionCacheSize(500);
        cachingConnectionFactory.setReconnectOnException(true);
        return cachingConnectionFactory;
    }
    
    @Bean
    public JmsOperations jmsOperations(CachingConnectionFactory cachingConnectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory);
        jmsTemplate.setReceiveTimeout(receiveTimeout);
        return jmsTemplate;
    }
}
