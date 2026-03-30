package com.omnisearch.crawl.util;
import java.net.URI;
import java.security.MessageDigest;
public class CrawlUtils {
    public static String normalizeUrl(String rawUrl) {
        try {
            // 1. Strip off anchors and query params for a clean ID
            String base = rawUrl.split("#")[0].split("\\?")[0];
            URI uri = new URI(base);

            String scheme = uri.getScheme().toLowerCase();
            String host = uri.getHost().toLowerCase();
            String path = uri.getPath() != null ? uri.getPath() : "/";

            // 2. Standardize trailing slashes
            if (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }
            return scheme + "://" + host + (path.isEmpty() ? "/" : path);
        } catch (Exception e) {
            return rawUrl;
        }
    }

    public static String generateMd5Hash(String text){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x",b));
            return sb.toString();
        }catch (Exception e){
            return String.valueOf(text.hashCode());
        }
    }
}