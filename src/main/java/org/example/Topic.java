package org.example;

public class Topic {
    private String url;
    private String text;

    public Topic(){}

    public Topic(String url, String text){
        this.url = url;
        this.text = text;
    }
    public void setTopic(String url, String text){
        this.url = url;
        this.text = text;
    }
    public String getUrl(){
        return this.url;
    }
    public String getText(){
        return this.text;
    }
}
