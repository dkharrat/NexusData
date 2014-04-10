package com.github.dkharrat.nexusdata.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {
    public static String getStringFromStream(InputStream in, int maxLength) throws IOException {
        if (maxLength == 0) {
            maxLength = Integer.MAX_VALUE;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int lengthSoFar = 0;
        int curLength = 0;
        while ((curLength = in.read(buffer)) != -1 && lengthSoFar <= maxLength) {
            baos.write(buffer, 0, Math.min(curLength, maxLength - lengthSoFar));
            lengthSoFar += curLength;
        }
        return new String(baos.toByteArray());
    }
}
