/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 *
 */

package org.qubership.automation.diameter.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.qubership.automation.diameter.data.Decoder;
import org.qubership.automation.diameter.data.Encoder;
import org.qubership.automation.diameter.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import lombok.Setter;

public class ConnectionFactory {
    private static final ScheduledExecutorService SERVICE = Executors.newSingleThreadScheduledExecutor();
    private static final int CACHE_LIFETIME = Integer.parseInt(System.getProperty("diameter.cacheLifetime", "12"));
    @Setter
    private static Cache<Object, DiameterConnection> connectionCache = CacheBuilder.newBuilder()
            .expireAfterAccess(CACHE_LIFETIME, TimeUnit.HOURS)
            .removalListener((RemovalListener<Object, DiameterConnection>) removalNotification -> {
                        DiameterConnection connection = removalNotification.getValue();
                        if (connection != null) {
                            connection.stopListening();
                            closeConnection(connection);
                        }
                    }
            )
            .build();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionFactory.class);

    /**
     * Get existing DiameterConnection by key from cache.
     *
     * @param key - key to search in cache,
     * @return DiameterConnection if found and not null and open; otherwise return null.
     */
    public static synchronized DiameterConnection getExisting(final Object key) {
        DiameterConnection connection = connectionCache.getIfPresent(key);
        if (connection != null && connection.isOpen()) {
            return connection;
        }
        connectionCache.invalidate(key);
        return null;
    }

    static {
        SERVICE.scheduleWithFixedDelay(() -> {
            try {
                connectionCache.cleanUp();
            } catch (Throwable t) {
                LOGGER.error("Error while Cache cleaning up", t);
            }
        }, 10, 30, TimeUnit.MINUTES);
    }

    public static synchronized Cache<Object, DiameterConnection> getAll() {
        return connectionCache;
    }

    private static DiameterConnection diameterConnection;

    public static synchronized DiameterConnection createConnection() {
        diameterConnection = new DiameterConnection();
        return diameterConnection;
    }

    public static synchronized DiameterConnection createConnection(final String dpr) {
        diameterConnection = new DiameterConnection(dpr);
        return diameterConnection;
    }

    /**
     * Create Diameter Connection.
     *
     * @param host - hostname or ip address to connect,
     * @param port - port number to connect,
     * @param defaultConnectionTemplate - template to send while establishing a connection,
     * @param encoder - Encoder instance link,
     * @param decoder - Decoder instance link,
     * @param interceptors - interceptors list,
     * @param transportType - transport type (SCTP/TCP),
     * @param connectionKey - connection key (for cache),
     * @return DiameterConnection established.
     * @throws Exception in case connection is not established successfully.
     */
    public static synchronized DiameterConnection createConnection(
            final String host, final int port, final String defaultConnectionTemplate, final Encoder encoder,
            final Decoder decoder, final List<Interceptor> interceptors, final TransportType transportType,
            final Object connectionKey) throws Exception {
        diameterConnection = new DiameterConnection();
        connecting(host, port, defaultConnectionTemplate, encoder, decoder, interceptors, transportType, connectionKey);
        return diameterConnection;
    }

    /**
     * Create Diameter Connection.
     *
     * @param host - hostname or ip address to connect,
     * @param port - port number to connect,
     * @param defaultConnectionTemplate - template to send while establishing a connection,
     * @param dpr - DPR template,
     * @param encoder - Encoder instance link,
     * @param decoder - Decoder instance link,
     * @param interceptors - interceptors list,
     * @param transportType - transport type (SCTP/TCP),
     * @param connectionKey - connection key (for cache),
     * @return DiameterConnection established.
     * @throws Exception in case connection is not established successfully.
     */
    public static synchronized DiameterConnection createConnection(
            final String host, final int port, final String defaultConnectionTemplate, final String dpr,
            final Encoder encoder, final Decoder decoder, final List<Interceptor> interceptors,
            final TransportType transportType, final Object connectionKey) throws Exception {
        diameterConnection = new DiameterConnection(dpr);
        connecting(host, port, defaultConnectionTemplate, encoder, decoder, interceptors, transportType, connectionKey);
        return diameterConnection;
    }

    private static void connecting(final String host,
                                   final int port,
                                   final String defaultConnectionTemplate,
                                   final Encoder encoder,
                                   final Decoder decoder,
                                   final List<Interceptor> interceptors,
                                   final TransportType transportType,
                                   final Object connectionKey) throws Exception {
        LOGGER.info("Create connection to {}:{}. Connection key: {}. CER: {}", host, port, connectionKey,
                defaultConnectionTemplate);
        diameterConnection.setEncoder(encoder);
        diameterConnection.setDecoder(decoder);
        diameterConnection.addInterceptors(interceptors);
        ExtraChannel channel = connectToRemoteServer(host, port, transportType);
        initDefaultConnection(defaultConnectionTemplate, encoder, decoder, channel);
        diameterConnection.setSocketChannel(channel);
        diameterConnection.startListening(connectionKey);
        connectionCache.put(connectionKey, diameterConnection);
    }

    public static void cache(final Object key, final DiameterConnection connection) {
        connectionCache.put(key, connection);
    }

    public static void destroy(final Object key) {
        LOGGER.info("Destroy connection for key: {}", key);
        connectionCache.invalidate(key);
    }

    /**
     * Build cache with expireAfterAccess timeout set and removal listener.
     *
     * @param timeout - timeout for expireAfterAccess property,
     * @param timeUnit - TimeUnit to measure timeout.
     */
    public static void buildConnectionCacheLifeTime(final int timeout, final TimeUnit timeUnit) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        if (timeout > 0) {
            cacheBuilder.expireAfterAccess(timeout, timeUnit);
        }
        connectionCache = cacheBuilder.removalListener((RemovalListener<Object, DiameterConnection>) removalNotification -> {
                    DiameterConnection connection = removalNotification.getValue();
                    if (connection != null) {
                        connection.stopListening();
                        closeConnection(connection);
                    }
                }
        ).build();
    }

    private static void closeConnection(final DiameterConnection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            LOGGER.error("Can't close connection: {}", connection.getChannel());
        }
    }

    private static ExtraChannel connectToRemoteServer(final String host,
                                                      final int port,
                                                      final TransportType transportType) throws IOException {
        ExtraChannel channel = ExtraChannel.open(transportType);
        InetSocketAddress address = new InetSocketAddress(host, port);
        LOGGER.info("Establish connection to: {}", address);
        boolean connect = channel.connect(address);
        if (!connect) {
            throw new IllegalStateException("Socket isn't connected to remote host: " + host + ':' + port);
        }
        return channel;
    }

    private static void initDefaultConnection(final String defaultConnectionTemplate,
                                              final Encoder encoder,
                                              final Decoder decoder,
                                              final ExtraChannel channel) throws Exception {
        LOGGER.info("Send default CER message: {}", defaultConnectionTemplate);
        channel.write(encoder.encode(defaultConnectionTemplate));
        ByteBuffer allocate = ByteBuffer.allocate(4096);
        int read = channel.read(allocate);
        if (read < 1) {
            throw new IllegalStateException("Diameter connection is not established. Data from socket is empty.");
        }
        byte[] bytes = Arrays.copyOfRange(allocate.array(), 0, read);
        LOGGER.info("Received data:\n{}", Arrays.toString(bytes));
        String decode = decoder.decode(ByteBuffer.wrap(bytes));
        LOGGER.info("Decoded message:\n{}", decode);
        if (!decode.startsWith("<CEA>")) {
            throw new IllegalStateException("Diameter connection is not established. Response data is not CEA message");
        }
    }
}
