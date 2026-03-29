package com.omnisearch.crawl.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CrawlUtils {
    /**
     * 1. URL Normalization
     * Converts http://example.com/, https://example.com?session=123, and https://example.com
     * into a single unified Canonical URL: "https://example.com"
     */
    public static String normalizeUrl(String rawUrl){
        if (rawUrl==null || rawUrl.isBlank()) return null;
        try{
            //Strip off anchor tags (ex: #section-1)
            int fragmentIndex = rawUrl.indexOf('#');
            if (fragmentIndex>-1){
                rawUrl = rawUrl.substring(0,fragmentIndex);
            }

            URI uri = new URI(rawUrl);
            String scheme = uri.getScheme()!=null ? uri.getScheme().toLowerCase(): "http";
            String host = uri.getHost()!=null ?  uri.getHost().toLowerCase(): "";
            String port = uri.getPort() != -1 ? ":" + uri.getPort() : "";
            String path = uri.getPath()!=null ? uri.getPath() : "";

            //Standardize paths by removing trailing slashes
            if (path.endsWith("/")){
                path = path.substring(0,path.length()-1);
            }
            if (path.isBlank()){
                path="/";
            }

            //We explicitly ignore volatile query parameters (?session=123) to establish a truly unique URL footprint
            return scheme+"://"+host+port+path;
        }catch(URISyntaxException e){
            return rawUrl; //Return original if parsing completely fails
        }
    }

    /**
     * 2. Content Deduplication (MD5 Hashing)
     * If two URLs serve identical text, we must not index it twice.
     * We hash the text down to a tiny 32-character string to compare them instantly in memory!
     */
    public static String generateMd5Hash(String content){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(content.getBytes());
            StringBuilder sb = new StringBuilder();

            //Convert to a clean hex-string
            for (byte b : hashBytes){
                sb.append(String.format("%02x",b));
            }
            return sb.toString();
        }catch(NoSuchAlgorithmException e){
            return String.valueOf(content.hashCode()); //Fallback mechanism
        }
    }
}
