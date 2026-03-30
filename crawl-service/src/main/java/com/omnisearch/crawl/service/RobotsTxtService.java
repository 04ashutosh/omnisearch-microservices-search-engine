package com.omnisearch.crawl.service;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RobotsTxtService {
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    public boolean isAllowed(String url){
        try{
            URI uri = new URI(url);
            String domain = uri.getScheme() + "://" + uri.getHost();
            List<String> disallowed = cache.computeIfAbsent(domain,this::fetchRobots);
            String path = uri.getPath()!=null? uri.getPath() : "/";
            return disallowed.stream().noneMatch(path::startsWith);
        }catch (Exception e){
            return true;
        }
    }

    private List<String> fetchRobots(String domain){
        List<String> rules = new ArrayList<>();
        try{
            String txt = Jsoup.connect(domain+"/robots.txt").get().text();
            Scanner sc = new Scanner(txt);
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("Disallow:")) rules.add(line.split(":")[1].trim());
            }
        }catch (Exception e){}
        return rules;
    }
}
