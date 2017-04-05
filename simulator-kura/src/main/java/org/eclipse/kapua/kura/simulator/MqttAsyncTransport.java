/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.kura.simulator;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import org.eclipse.kapua.kura.simulator.payload.Message;
import org.eclipse.kapua.kura.simulator.topic.Topic;
import org.eclipse.kapua.kura.simulator.util.Hex;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A transport implementation based on MQTT
 */
public class MqttAsyncTransport extends AbstractMqttTransport implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MqttAsyncTransport.class);

    private final MqttAsyncClient client;

    private final MqttConnectOptions connectOptions;

    private Runnable onConnected;

    private Runnable onDisconnected;

    public MqttAsyncTransport(final GatewayConfiguration configuration) throws MqttException {
        super(configuration);

        final MemoryPersistence persistence = new MemoryPersistence();
        final String plainBrokerUrl = plainUrl(configuration.getBrokerUrl());
        client = new MqttAsyncClient(plainBrokerUrl, configuration.getClientId(), persistence);
        client.setCallback(new MqttCallback() {

            @Override
            public void messageArrived(final String topic, final MqttMessage message) throws Exception {
            }

            @Override
            public void deliveryComplete(final IMqttDeliveryToken token) {
            }

            @Override
            public void connectionLost(final Throwable cause) {
                handleDisconnected();
            }
        });
        connectOptions = createConnectOptions(configuration.getBrokerUrl());
    }

    @Override
    public void connect() {
        try {
            IMqttToken token = client.connect(connectOptions, null, new IMqttActionListener() {

                @Override
                public void onSuccess(final IMqttToken asyncActionToken) {
                    handleConnected();
                }

                @Override
                public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
                    logger.warn("Failed to connect", exception);
                }
            });
            token.waitForCompletion();
        } catch (final MqttException e) {
            logger.warn("Failed to initiate connect", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            client.disconnect(null, new IMqttActionListener() {

                @Override
                public void onSuccess(final IMqttToken asyncActionToken) {
                    handleDisconnected();
                }

                @Override
                public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
                    logger.warn("Failed to disconnect", exception);
                }
            });
        } catch (final MqttException e) {
            logger.warn("Failed to initiatate disconnect", e);
        }
    }

    @Override
    public void close() throws MqttException {
        try {
            client.disconnect(5000).waitForCompletion();
        } finally {
            client.close();
        }
    }

    @Override
    public void subscribe(final Topic topic, final Consumer<Message> consumer) {
        requireNonNull(consumer);

        try {
            client.subscribe(topic.render(topicContext), 0, null, null, new IMqttMessageListener() {

                @Override
                public void messageArrived(final String topic, final MqttMessage mqttMessage) throws Exception {
                    logger.debug("Received MQTT message from {}", topic);
                    consumer.accept(new Message(Topic.fromString(topic), mqttMessage.getPayload(),
                            MqttAsyncTransport.this.topicContext));
                }
            });
        } catch (final MqttException e) {
            if (e.getReasonCode() != MqttException.REASON_CODE_CLIENT_NOT_CONNECTED) {
                logger.warn("Failed to subscribe to: {}", topic, e);
            }
        }
    }

    @Override
    public void unsubscribe(final Topic topic) {
        try {
            client.unsubscribe(topic.render(topicContext));
        } catch (final MqttException e) {
            if (e.getReasonCode() != MqttException.REASON_CODE_CLIENT_NOT_CONNECTED) {
                logger.warn("Failed to unsubscribe: {}", topic, e);
            }
        }
    }

    @Override
    public void whenConnected(final Runnable runnable) {
        onConnected = runnable;
    }

    @Override
    public void whenDisconnected(final Runnable runnable) {
        onDisconnected = runnable;
    }

    protected void handleConnected() {
        final Runnable runnable = onConnected;
        if (runnable != null) {
            runnable.run();
        }
    }

    protected void handleDisconnected() {
        final Runnable runnable = onDisconnected;
        if (runnable != null) {
            runnable.run();
        }
    }

    @Override
    public void sendMessage(final Topic topic, final byte[] payload) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending message - topic: {}, payload: {}", topic, Hex.toHex(payload, 256));
        }

        try {
            final String fullTopic = topic.render(topicContext);
            logger.debug("Full topic: {}", fullTopic);

            client.publish(fullTopic, payload, 0, false);
        } catch (final Exception e) {
//            logger.warn("Failed to send out message", e);
        }
    }

}
