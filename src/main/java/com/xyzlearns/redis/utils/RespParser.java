
package com.xyzlearns.redis.utils;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespParser {

    public List<String> respParser(InputStream in) throws IOException {

        // Read the first byte
        int firstByte = in.read();

        // End of stream: no more commands
        if (firstByte == -1) {
            return null;
        }

        if (firstByte != '*') {
            throw new IOException("Expected RESP array");
        }

        // Read array length
        int arrayLength = Integer.parseInt(readLine(in));

        List<String> command = new ArrayList<>();

        // Read each bulk string
        for (int i = 0; i < arrayLength; i++) {

            int marker = in.read();

            if (marker != '$') {
                throw new IOException("Expected bulk string");
            }

            // Read bulk string length
            int wordLength = Integer.parseInt(readLine(in));

            if (wordLength < 0) {
                throw new IOException("Invalid command argument length");
            }

            // Read exactly wordLength bytes
            byte[] data = in.readNBytes(wordLength);

            if (data.length != wordLength) {
                throw new EOFException("Incomplete bulk string");
            }

            String element = new String(data, StandardCharsets.UTF_8);

            command.add(element);

            // Consume trailing CRLF
            if (in.read() != '\r' || in.read() != '\n') {
                throw new IOException("Expected CRLF after bulk string");
            }
        }

        return command;
    }

    private String readLine(InputStream in) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        while (true) {

            int b = in.read();

            if (b == -1) {
                throw new EOFException("Unexpected EOF while reading RESP line");
            }

            if (b == '\r') {

                if (in.read() != '\n') {
                    throw new IOException("Expected LF after CR");
                }

                return buffer.toString(StandardCharsets.UTF_8);
            }

            buffer.write(b);
        }
    }
}
