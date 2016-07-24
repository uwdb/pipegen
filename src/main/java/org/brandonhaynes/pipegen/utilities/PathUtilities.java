package org.brandonhaynes.pipegen.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PathUtilities {
    public static String resolveFilename(String filename) {
        try {
            URI uri = new URI(URLEncoder.encode(filename, StandardCharsets.UTF_8.name()));
            if (uri.getScheme() == null || uri.getScheme().equals("file"))
                return URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8.name());
            else
                throw new RuntimeException("Scheme not supported: " + uri.getScheme());
        } catch(URISyntaxException |UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
