package com.jaspersoft.jasperserver.api.engine.replication;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.NamingException;
import java.net.URISyntaxException;

import net.sf.ehcache.distribution.jms.JMSUtil;
import org.apache.activemq.jndi.ActiveMQInitialContextFactory;


public class JRSActiveMQInitialContextFactory extends ActiveMQInitialContextFactory {
    @Override
    @SuppressWarnings("unchecked")
    public Context getInitialContext(Hashtable environment) throws NamingException {

        Map<String, Object> data = new ConcurrentHashMap<String, Object>();

        String replicationTopicConnectionFactoryBindingName = (String) environment.get(JMSUtil.TOPIC_CONNECTION_FACTORY_BINDING_NAME);

        if (replicationTopicConnectionFactoryBindingName != null) {
            try {
                data.put(replicationTopicConnectionFactoryBindingName, createConnectionFactory(environment));
            } catch (URISyntaxException e) {
                throw new NamingException("Error initialisating TopicConnectionFactory with message " + e.getMessage());
            }
        }

        String getQueueConnectionfactoryBindingName = (String) environment.get(JMSUtil.GET_QUEUE_CONNECTION_FACTORY_BINDING_NAME);

        if (getQueueConnectionfactoryBindingName != null) {
            try {
                data.put(getQueueConnectionfactoryBindingName, createConnectionFactory(environment));
            } catch (URISyntaxException e) {
                throw new NamingException("Error initialisating TopicConnectionFactory with message " + e.getMessage());
            }
        }

        String replicationTopicBindingName = (String) environment.get(JMSUtil.REPLICATION_TOPIC_BINDING_NAME);
        if (replicationTopicBindingName != null) {
            data.put(replicationTopicBindingName, createTopic(replicationTopicBindingName));
        }


        String getQueueBindingName = (String) environment.get(JMSUtil.GET_QUEUE_BINDING_NAME);
        if (getQueueBindingName != null) {
            data.put(getQueueBindingName, createQueue(getQueueBindingName));
        }

        return createContext(environment, data);
    }
}
