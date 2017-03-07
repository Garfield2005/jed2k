package org.dkf.jed2k.kad.server;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.PacketCombiner;
import org.dkf.jed2k.protocol.PacketHeader;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.KadPacketHeader;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;

/**
 * Created by apavlov on 06.03.17.
 */
@Slf4j
public class SynDhtTracker {
    private int timeout;
    private int port;
    private DatagramSocket serverSocket = null;
    private ExecutorService executor;
    private PGPoolingDataSource ds;
    byte[] data = new byte[8096];
    private PacketCombiner combiner = new org.dkf.jed2k.protocol.kad.PacketCombiner();

    public SynDhtTracker(int port, int timeout, ExecutorService executor, final PGPoolingDataSource ds) throws JED2KException {
        this.port = port;
        this.timeout = timeout;
        this.executor = executor;
        this.ds = ds;

        try {
            serverSocket = new DatagramSocket(port);
            serverSocket.setSoTimeout(timeout);
        } catch(SocketException e) {
            log.error("unable to create udp server socket {}", e);
            throw new JED2KException(ErrorCode.DHT_TRACKER_SOCKET_EXCEPTION);
        }
    }

    public void processPackets() throws JED2KException {
        try {
            DatagramPacket receivePacket = new DatagramPacket(data, data.length);
            serverSocket.receive(receivePacket);
            ByteBuffer buffer = ByteBuffer.wrap(data, 0, receivePacket.getLength());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            PacketHeader header = new KadPacketHeader();
            header.get(buffer);
            if (!header.isDefined()) throw new JED2KException(ErrorCode.PACKET_HEADER_UNDEFINED);
            Serializable s = combiner.unpack(header, buffer);
            log.trace("incoming packet {}", s);

            executor.submit(new DhtRequestHandler(s
                        , new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort())
                        , ds));
        } catch(SocketTimeoutException e) {
            log.trace("socket timeout");
        } catch (IOException e) {

        }
    }

    public void close() {
        if (serverSocket != null) serverSocket.close();
    }
}
