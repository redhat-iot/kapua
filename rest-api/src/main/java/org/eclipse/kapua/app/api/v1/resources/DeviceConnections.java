/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.app.api.v1.resources;

import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.eclipse.kapua.app.api.v1.resources.model.CountResult;
import org.eclipse.kapua.app.api.v1.resources.model.EntityId;
import org.eclipse.kapua.app.api.v1.resources.model.ScopeId;
import org.eclipse.kapua.commons.model.query.predicate.AndPredicate;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicate;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.service.device.registry.Device;
import org.eclipse.kapua.service.device.registry.connection.DeviceConnection;
import org.eclipse.kapua.service.device.registry.connection.DeviceConnectionFactory;
import org.eclipse.kapua.service.device.registry.connection.DeviceConnectionListResult;
import org.eclipse.kapua.service.device.registry.connection.DeviceConnectionPredicates;
import org.eclipse.kapua.service.device.registry.connection.DeviceConnectionQuery;
import org.eclipse.kapua.service.device.registry.connection.DeviceConnectionService;
import org.eclipse.kapua.service.device.registry.connection.DeviceConnectionStatus;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Api("Device Connections")
@Path("{scopeId}/deviceconnections")
public class DeviceConnections extends AbstractKapuaResource {

    private final KapuaLocator locator = KapuaLocator.getInstance();
    private final DeviceConnectionFactory deviceConnectionFactory = locator.getFactory(DeviceConnectionFactory.class);
    private final DeviceConnectionService deviceConnectionService = locator.getService(DeviceConnectionService.class);

    /**
     * Gets the {@link DeviceConnection} list in the scope.
     *
     * @param scopeId
     *            The {@link ScopeId} in which to search results.
     * @param clientId
     *            The id of the {@link Device} in which to search results
     * @param status
     *            The {@link DeviceConnectionStatus} in which to search results
     * @param offset
     *            The result set offset.
     * @param limit
     *            The result set limit.
     * @return The {@link DeviceConnectionListResult} of all the deviceConnections associated to the current selected scope.
     * @since 1.0.0
     */
    @ApiOperation(value = "Gets the DeviceConnection list in the scope",
            notes = "Returns the list of all the deviceConnections associated to the current selected scope.",
            response = DeviceConnection.class,
            responseContainer = "DeviceConnectionListResult")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public DeviceConnectionListResult simpleQuery(
            @ApiParam(value = "The ScopeId in which to search results.", required = true, defaultValue = DEFAULT_SCOPE_ID) @PathParam("scopeId") ScopeId scopeId,
            @ApiParam(value = "The client id to filter results.") String clientId,
            @ApiParam(value = "The connection status to filter results.")@QueryParam("status") DeviceConnectionStatus status,
            @ApiParam(value = "The result set offset.", defaultValue = "0") @QueryParam("offset") @DefaultValue("0") int offset,
            @ApiParam(value = "The result set limit.", defaultValue = "50") @QueryParam("limit") @DefaultValue("50") int limit)
    {
        DeviceConnectionListResult deviceConnectionListResult = deviceConnectionFactory.newListResult();
        try {
            DeviceConnectionQuery query = deviceConnectionFactory.newQuery(scopeId);

            AndPredicate andPredicate = new AndPredicate();
            if (!Strings.isNullOrEmpty(clientId)) {
                andPredicate.and(new AttributePredicate<>(DeviceConnectionPredicates.CLIENT_ID, clientId));
            }
            if (status != null) {
                andPredicate.and(new AttributePredicate<>(DeviceConnectionPredicates.STATUS, status));
            }
            query.setPredicate(andPredicate);

            query.setOffset(offset);
            query.setLimit(limit);

            deviceConnectionListResult = query(scopeId, query);
        } catch (Throwable t) {
            handleException(t);
        }
        return deviceConnectionListResult;
    }

    /**
     * Queries the results with the given {@link DeviceConnectionQuery} parameter.
     * 
     * @param scopeId
     *            The {@link ScopeId} in which to search results.
     * @param query
     *            The {@link DeviceConnectionQuery} to use to filter results.
     * @return The {@link DeviceConnectionListResult} of all the result matching the given {@link DeviceConnectionQuery} parameter.
     * @since 1.0.0
     */
    @ApiOperation(value = "Queries the DeviceConnections",
            notes = "Queries the DeviceConnections with the given DeviceConnections parameter returning all matching DeviceConnections",
            response = DeviceConnection.class,
            responseContainer = "DeviceConnectionListResult")
    @POST
    @Path("_query")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public DeviceConnectionListResult query(
            @ApiParam(value = "The ScopeId in which to search results.", required = true, defaultValue = DEFAULT_SCOPE_ID) @PathParam("scopeId") ScopeId scopeId,
            @ApiParam(value = "The DeviceConnectionQuery to use to filter results.", required = true) DeviceConnectionQuery query) {
        DeviceConnectionListResult deviceConnectionListResult = null;
        try {
            query.setScopeId(scopeId);
            deviceConnectionListResult = deviceConnectionService.query(query);
        } catch (Throwable t) {
            handleException(t);
        }
        return returnNotNullEntity(deviceConnectionListResult);
    }

    /**
     * Counts the results with the given {@link DeviceConnectionQuery} parameter.
     * 
     * @param scopeId
     *            The {@link ScopeId} in which to search results.
     * @param query
     *            The {@link DeviceConnectionQuery} to use to filter results.
     * @return The count of all the result matching the given {@link DeviceConnectionQuery} parameter.
     * @since 1.0.0
     */
    @ApiOperation(value = "Counts the DeviceConnections",
            notes = "Counts the DeviceConnections with the given DeviceConnectionQuery parameter returning the number of matching DeviceConnections",
            response = CountResult.class)
    @POST
    @Path("_count")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CountResult count(
            @ApiParam(value = "The ScopeId in which to count results", required = true, defaultValue = DEFAULT_SCOPE_ID) @PathParam("scopeId") ScopeId scopeId,
            @ApiParam(value = "The DeviceConnectionQuery to use to filter count results", required = true) DeviceConnectionQuery query) {
        CountResult countResult = null;
        try {
            query.setScopeId(scopeId);
            countResult = new CountResult(deviceConnectionService.count(query));
        } catch (Throwable t) {
            handleException(t);
        }
        return returnNotNullEntity(countResult);
    }

    /**
     * Returns the DeviceConnection specified by the "deviceConnectionId" path parameter.
     *
     * @param scopeId
     *            The {@link ScopeId} of the requested {@link DeviceConnection}.
     * @param deviceConnectionId
     *            The id of the requested DeviceConnection.
     * @return The requested DeviceConnection object.
     */
    @ApiOperation(value = "Get an DeviceConnection", notes = "Returns the DeviceConnection specified by the \"deviceConnectionId\" path parameter.", response = DeviceConnection.class)
    @GET
    @Path("{deviceConnectionId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public DeviceConnection find(
            @ApiParam(value = "The ScopeId of the requested DeviceConnection.", required = true, defaultValue = DEFAULT_SCOPE_ID) @PathParam("scopeId") ScopeId scopeId,
            @ApiParam(value = "The id of the requested DeviceConnection", required = true) @PathParam("deviceConnectionId") EntityId deviceConnectionId) {
        DeviceConnection deviceConnection = null;
        try {
            deviceConnection = deviceConnectionService.find(scopeId, deviceConnectionId);
        } catch (Throwable t) {
            handleException(t);
        }
        return returnNotNullEntity(deviceConnection);
    }

}
