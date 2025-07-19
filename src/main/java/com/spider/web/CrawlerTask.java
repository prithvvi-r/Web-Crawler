package com.spider.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class CrawlerTask implements Runnable{

    private final String url;
    private final BlockingQueue<String> urlQueue;
    private final Set<String> visitedUrls;
    private final int maxDepth;
    private final int currentDepth;

    public CrawlerTask{
        this.url = url;
        this.urlQueue = urlQueue;
        this.visitedUrls = visitedUrls;
        this.maxDepth = maxDepth;
        this.currentDepth = currentDepth;
    }

    @Override
    public void run(){
        //stop crawling if  we've reached the maximum depth
        if(currentDepth >= maxDepth){
            return; // no more crawling at this depth
        }

        try {
            //use Jsoup to fetch the html content of the URL
            Document document = Jsoup.connect(url).get();
            System.out.println("Crawling(" + currentDepth + "):" + url);

            //find all links <a> elements with an "href" attribute
            Elements linksOnpage = document.select("a[href]");
            //for each linkk, add it to the queue if it's a new and valid
            for (Element page : linksOnpage) {
                String newUrl = page.attr("abs:href");

                //The visitedUrls.add() method is atomic and Thread-safe.
                // it returns 'true' if the element was added(i,e. , it was not already in the set).
                if (!newUrl.isEmpty() && visitedUrls.add(newUrl)) {
                    //add the new Url to the queue for other threads to process
                    urlQueue.offer(newUrl);
                }
            }

        }catch (IOException e){
            //Handle errors like 404s or netwrok issues gracefully
            System.out.println("Error Crawling '" + url + " ': " + e.getMessage());
        }
    }
}
