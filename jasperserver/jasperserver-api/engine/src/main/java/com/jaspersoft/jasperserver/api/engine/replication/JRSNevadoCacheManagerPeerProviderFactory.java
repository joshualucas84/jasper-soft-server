package com.jaspersoft.jasperserver.api.engine.replication;

import java.util.Properties;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.skyscreamer.nevado.jms.NevadoConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.model.CreateQueueResult;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CacheManagerPeerProviderFactory;
import net.sf.ehcache.distribution.jms.AcknowledgementMode;
import net.sf.ehcache.distribution.jms.JMSCacheManagerPeerProvider;
 
public class JRSNevadoCacheManagerPeerProviderFactory extends CacheManagerPeerProviderFactory {
	static final Log log = LogFactory.getLog(JRSNevadoCacheManagerPeerProviderFactory.class);

	private static synchronized JMSCacheManagerPeerProvider getProvider(CacheManager cacheManager, Properties props){
		JMSCacheManagerPeerProvider provider = null;
		try {
			
			ApplicationContext context = new ClassPathXmlApplicationContext("nevado.xml");
			NevadoConnectionFactory nevadoConnectionFactory = (NevadoConnectionFactory)context.getBean("connectionFactory");
			nevadoConnectionFactory.setOverrideJMSTTL(new Long(0));

//			SQSConnectionFactory connectionFactory =
//				    SQSConnectionFactory.builder()
//				        .withRegion(Region.getRegion(Regions.US_EAST_1))
//				        .withAWSCredentialsProvider(new ClasspathPropertiesFileCredentialsProvider("aws.properties"))
//				        .build();
//				 
//			// Create the connection.
//			SQSConnection connection = connectionFactory.createConnection();
//			
//			// Get the wrapped client
//			
//			
//			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//			TopicConnection topicConnection = nevadoConnectionFactory.createTopicConnection(nevadoConnectionFactory.getAwsAccessKey(), nevadoConnectionFactory.getAwsSecretKey());
//			Topic nevadoTopic = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE).createTopic(nevadoConnectionFactory.getEhcacheTopic().getName());
////			Topic amazonTopic = session.createTopic(nevadoConnectionFactory.getEhcacheTopic().getName());
//			Queue amazonQueue = session.createQueue(nevadoConnectionFactory.getEhcacheQueue().getName());
//			
//			log.debug("NEVADO: JRSNevadoCacheManagerPeerProviderFactory: creating Provider *********************");
//            provider = new JMSCacheManagerPeerProvider(cacheManager,
//				topicConnection,
//				nevadoTopic,
//				connection,
//				amazonQueue,
//				AcknowledgementMode.AUTO_ACKNOWLEDGE,
//				true);

            TopicConnection topicConnection = nevadoConnectionFactory.createTopicConnection(nevadoConnectionFactory.getAwsAccessKey(), nevadoConnectionFactory.getAwsSecretKey());
			QueueConnection queueConnection = nevadoConnectionFactory.createQueueConnection(nevadoConnectionFactory.getAwsAccessKey(), nevadoConnectionFactory.getAwsSecretKey());
			Topic nevadoTopic = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE).createTopic(nevadoConnectionFactory.getEhcacheTopic().getName());
			Queue nevadoQueue = queueConnection.createSession(false,  Session.AUTO_ACKNOWLEDGE).createQueue(nevadoConnectionFactory.getEhcacheQueue().getName());
			log.debug("NEVADO: JRSNevadoCacheManagerPeerProviderFactory: creating Provider *********************");
            provider = new JMSCacheManagerPeerProvider(cacheManager,
				topicConnection,
				nevadoTopic,
				queueConnection,
				nevadoQueue,
				AcknowledgementMode.AUTO_ACKNOWLEDGE,
				true);
            
	    } catch(Exception e) {
			log.error("NEVADO: JRSNevadoCacheManagerPeerProviderFactory returned error", e);
        } finally {
			log.debug("NEVADO: JRSNevadoCacheManagerPeerProviderFactory: done creating provider *********************");
        }
		return provider;
	}
	
	@Override
    public CacheManagerPeerProvider createCachePeerProvider(CacheManager cacheManager, Properties props) {
		return getProvider(cacheManager, props);
	} 
}
