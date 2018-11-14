package com.example.hoauy.carrottv.item;

/**
 * Created by hoauy on 2018-05-25.
 */

public class Crawling_item {


    private String crawling_image;

    private String crawling_title;
    private String crawling_content;
    private String crawling_company;

    private String hidden_url;
    //기사 링크

    public Crawling_item(String crawling_image, String crawling_title,
                         String crawling_content, String crawling_company, String hidden_url){

        this.crawling_image = crawling_image;
        this.crawling_title = crawling_title;
        this.crawling_content = crawling_content;
        this.crawling_company = crawling_company;
        this.hidden_url = hidden_url;

    }

    public String getCrawling_company() {
        return crawling_company;
    }

    public String getCrawling_content() {
        return crawling_content;
    }

    public String getCrawling_image() {
        return crawling_image;
    }

    public String getCrawling_title() {
        return crawling_title;
    }

    public String getHidden_url() {
        return hidden_url;
    }

}
