/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.rs232;

import com.fazecast.jSerialComm.SerialPort;
import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.protocol.TcProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the R232 connector that can talk to a tcMenu library application running
 * on an embedded Arduino. Normally one uses the Rs232ControllerBuilder to construct
 * the whole remote stack instead of creating this directly.
 */
public class Rs232RemoteConnector extends StreamRemoteConnector {

    private final String portName;
    private final SerialPort serialPort;
    private final int baud;

    public Rs232RemoteConnector(String portName, int baud, MenuCommandProtocol protocol,
                                ScheduledExecutorService executor) {
        super(protocol, executor);
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(baud);
        this.portName = portName;
        this.baud = baud;
    }

    public void start() {
        executor.execute(this::threadedReader);
    }

    public void stop() {
        executor.shutdownNow();
    }

    private void threadedReader() {
        logger.info("RS232 Reading thread started");
        while (!Thread.currentThread().isInterrupted()) {
            if(reconnectWithWait()) {
                processMessagesOnConnection();
            }
        }
        logger.info("RS232 Reading thread ended");
    }

    private boolean reconnectWithWait() {
        try {
            Thread.sleep(500); // we need a short break before attempting the first reconnect
            logger.info("Attempting to connect over rs232 to " + getConnectionName());
            serialPort.openPort();
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 30000, 30000);
            if(serialPort.isOpen()) {
                notifyConnection();
            }
            else {
                Thread.sleep(5000); // then re-try about every 5 seconds.
            }
            return serialPort.isOpen();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public String getConnectionName() {
        return "Serial " + portName + "@" + baud;
    }

    @Override
    protected void sendInternal(ByteBuffer outputBuffer) throws IOException {
        byte[] data = new byte[outputBuffer.remaining()];
        outputBuffer.get(data, 0, data.length);
        serialPort.getOutputStream().write(data, 0, data.length);
    }

    @Override
    protected void getAtLeastBytes(ByteBuffer inputBuffer, int len) throws IOException {
        do {
            inputBuffer.compact();

            while(serialPort.bytesAvailable() > 0) {
                inputBuffer.put((byte)serialPort.getInputStream().read());
            }

            inputBuffer.flip();

        } while(inputBuffer.remaining()<len);
    }
}
