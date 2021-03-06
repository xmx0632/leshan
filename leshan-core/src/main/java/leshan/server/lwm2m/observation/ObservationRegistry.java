/*
 * Copyright (c) 2013, Sierra Wireless
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of {{ project }} nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leshan.server.lwm2m.observation;

import java.util.Set;

import leshan.server.lwm2m.client.Client;

/**
 * A registry for keeping track of observed resources implemented by LWM2M Clients.
 * 
 */
public interface ObservationRegistry {

    /**
     * Adds an observation of resource(s) to the registry.
     * 
     * @param observation the observation
     * @return the ID under which the observation is registered. This ID can be used to cancel the observation (see
     *         {@link #cancelObservation(String))
     */
    void addObservation(Observation observation);

    /**
     * Cancels all active observations of resource(s) implemented by a particular LWM2M Client.
     * 
     * As a consequence the LWM2M Client will stop sending notifications about updated values of resources in scope of
     * the canceled observation.
     * 
     * @param client the LWM2M Client to cancel observations for
     * @return the number of canceled observations
     */
    int cancelObservations(Client client);

    /**
     * Cancels the active observations for the given resource.
     * 
     * As a consequence the LWM2M Client will stop sending notifications about updated values of resources in scope of
     * the canceled observation.
     * 
     * @param client the LWM2M Client to cancel observation for
     * @param resourcepath resource to cancel observation for
     * @return the number of canceled observations
     */
    void cancelObservation(Client client, String resourcepath);

    /**
     * Get all running observation for a given client
     * 
     * @return an unmodifiable set of observation
     */
    Set<Observation> getObservations(Client client);

    void addListener(ObservationRegistryListener listener);

    void removeListener(ObservationRegistryListener listener);
}
