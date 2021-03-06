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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.kapua.kura.simulator.app.Application;
import org.eclipse.kapua.kura.simulator.app.ApplicationController;
import org.eclipse.kapua.kura.simulator.birth.BirthCertificateModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default Kura simulator
 */
public class Simulator implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

    protected final Transport transport;

    private boolean isConnected = false;

    protected List<Module> modules = new LinkedList<>();

    public Simulator(final GatewayConfiguration configuration, final Transport transport,
            final Set<Application> applications) {

        this.transport = transport;

        // set up callbacks

        this.transport.whenConnected(this::connected);
        this.transport.whenDisconnected(this::disconnected);

        // set up application controller

        final ApplicationController applicationController = new ApplicationController(transport, applications);
        modules.add(applicationController);

        // set up builder

        modules.add(new BirthCertificateModule(configuration, applicationController::getApplicationIds));

        // finally connect

        this.transport.connect();
    }

    @Override
    public void close() {
        // we don't close the transport here
    }

    protected void connected() {
        isConnected = true;
        logger.info("Connected ... sending birth certificate ...");
        for (final Module module : modules) {
            try {
                module.connected(transport);
            } catch (final Exception e) {
                logger.warn("Failed to call module: {}", module, e);
            }
        }
    }

    protected void disconnected() {
        isConnected = false;
        logger.info("Disconnected... Should retry connection...");
        for (final Module module : modules) {
            try {
                module.disconnected(transport);
            } catch (final Exception e) {
                logger.warn("Failed to call module: {}", module, e);
            }
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}