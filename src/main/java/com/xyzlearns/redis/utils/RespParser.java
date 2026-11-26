package com.xyzlearns.redis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RespParser {

    public RespParser(){

    }

    //*2 \r\n $4 \r\n ECHO \r\n $3 \r\n hey \r\n
    //01 2345 67 891011


    public List<String> respParser(InputStream in) {

        List<String> command = new ArrayList<>();

        try {
            byte[] buffer = new byte[1024];

            int read = in.read(buffer);

            if (read == -1) {
                return null;
            }

            String message = new String(buffer, 0, read);

            int i = 0;

            // Read array length
            if (message.charAt(i) == '*') {
                i++;

                int end = message.indexOf("\r\n", i);

                int arrayLength = Integer.parseInt(
                        message.substring(i, end)
                );

                i = end + 2;

                // Read array elements
                for (int j = 0; j < arrayLength; j++) {

                    if (message.charAt(i) != '$') {
                        throw new IllegalArgumentException(
                                "Expected bulk string"
                        );
                    }

                    i++;

                    // Read bulk string length
                    end = message.indexOf("\r\n", i);

                    int wordLength = Integer.parseInt(
                            message.substring(i, end)
                    );

                    i = end + 2;

                    // Extract actual string
                    String element = message.substring(
                            i, i + wordLength
                    );

                    command.add(element);

                    // Skip payload and trailing CRLF
                    i += wordLength + 2;
                }
            }

            return command;

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return command;
        }
    }

}
