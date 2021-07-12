/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.validation.body;

import org.mule.validation.exception.BadRequestException;
import org.mule.validation.helpers.RewindableInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;

public abstract class BodyAsStringValidator {

  protected String getPayloadAsString(Object input, String charset) throws BadRequestException {

    try {
      byte[] bytes = getPayloadAsByteArray(input);
      return IOUtils.toString(trimBom(bytes), charset);
    } catch (IOException e) {
      throw new BadRequestException("Error processing request: " + e.getMessage());
    }
  }

  protected byte[] getPayloadAsByteArray(Object input) throws IOException {
    if (input instanceof CursorStreamProvider)
      return IOUtils.toByteArray(((CursorStreamProvider) input).openCursor());
    else if (input instanceof RewindableInputStream) {
      final RewindableInputStream rewindable = (RewindableInputStream) input;
      final byte[] bytes = IOUtils.toByteArray(rewindable);
      rewindable.rewind();
      return bytes;
    } else if (input instanceof InputStream)
      return IOUtils.toByteArray((InputStream) input);
    else if (input instanceof String)
      return ((String) input).getBytes();
    else if (input instanceof byte[])
      return (byte[]) input;
    else if (input != null)
      throw new IOException("Don't know how to get " + input.getClass().getName());
    else
      throw new IOException("Don't know how to get payload");
  }

  protected byte[] trimBom(byte[] content) {
    int bomSize = 0;
    if (content.length > 4) {
      // check for UTF_32BE and UTF_32LE BOMs
      if (content[0] == 0x00 && content[1] == 0x00 && content[2] == (byte) 0xFE && content[3] == (byte) 0xFF ||
          content[0] == (byte) 0xFF && content[1] == (byte) 0xFE && content[2] == 0x00 && content[3] == 0x00) {
        bomSize = 4;
      }
    }
    if (content.length > 3 && bomSize == 0) {
      // check for UTF-8 BOM
      if (content[0] == (byte) 0xEF && content[1] == (byte) 0xBB && content[2] == (byte) 0xBF) {
        bomSize = 3;
      }
    }
    if (content.length > 2 && bomSize == 0) {
      // check for UTF_16BE and UTF_16LE BOMs
      if (content[0] == (byte) 0xFE && content[1] == (byte) 0xFF || content[0] == (byte) 0xFF && content[1] == (byte) 0xFE) {
        bomSize = 2;
      }
    }

    if (bomSize > 0) {
      int trimmedSize = content.length - bomSize;
      byte[] trimmedArray = new byte[trimmedSize];
      System.arraycopy(content, bomSize, trimmedArray, 0, trimmedSize);
      return trimmedArray;
    }
    return content;
  }

}
