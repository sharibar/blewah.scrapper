package com.scrapper.blewah;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.*;
import java.io.*;
public class Scrapper {
    public static void main(String[] args){
        //scrapeTopic("anime/fairy-tail/");
        scrapeAnime("http://blewaah.com/anime/fairy-tail/");
    }

    public static String getUrl(String url) {
        URL urlObj = null;
        try {
            urlObj = new URL(url);
        } catch (MalformedURLException e) {
            System.out.println("The url was malformed!");
            return "";
        }

        URLConnection urlCon = null;
        BufferedReader in = null;
        String outputText = "";

        try {
            urlCon = urlObj.openConnection();
            in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
            String line = "";
            while ((line = in.readLine()) != null) {
                outputText += line;
            }
            in.close();
        } catch (IOException e) {
            System.out.println("There was an error connecting to the URL");
            return "";
        }
        return outputText;
    }

    public static String getLink(String url){
        String html = getUrl(url);
        Document doc = Jsoup.parse(html);
        Element link = doc.select("#page > main > section > article > div.tabgarb_content > div > div > div > div > video > source").first();

        String linkStr = "";
        linkStr = link.attr("src");
        return linkStr;

    }

    public static void scrapeAnime(String url){
        String html = getUrl(url);
        Document doc = Jsoup.parse(html);
        Elements links = doc.select("#main > section > table > tbody [class~=_col-eps]");

        for (Element linkAndTitle: links) {
            String title = linkAndTitle.getElementsByClass("eps").first().attr("title");
            String link = linkAndTitle.getElementsByClass("eps").first().attr("href") + "?tabgarb=tab1";
            String videoLink = getLink(link);
            System.out.println(title + ", " + videoLink);
        }
    }


}




