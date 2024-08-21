package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class CochraneCrawler {
    private final String domain;
    private final List<Topic> topics;
    private final WebDriver driver;
    private int current;

    public CochraneCrawler(String domain) throws IOException {
        this.domain = domain;
        this.topics = new ArrayList<>();
        this.driver = new ChromeDriver();
        this.current = 0;
    }

    private boolean shouldCrawl(String url, String sub) {
        return url.startsWith(domain) && url.contains(sub);
    }

    public void crawl(String url, FileWriter myWriter) throws IOException {
        String keyWord = "cdsr/doi";
        try {
            driver.get(url);
            Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("pagination-page-list-item")));
            List<WebElement> titles = driver.findElements(By.className("result-title"));
            List<WebElement> authors = driver.findElements(By.className("search-result-authors"));
            SimpleDateFormat inputFormatter = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
            SimpleDateFormat outputFormatter = new SimpleDateFormat("MM-dd-yyyy");
            List<WebElement> dates = driver.findElements(By.className("search-result-date"));

            // Get formatted date
            List<String> formattedDates = dates.stream().map(
                    date -> {
                        Date newDate = null;
                        try {
                            newDate = inputFormatter.parse(date.getText());
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        return outputFormatter.format(newDate);
                    }
            ).collect(Collectors.toList());

            // Get filtered pagination url
            List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
            List<String> filteredLinks = new ArrayList<>();
            for (WebElement link : links) {
                String nextUrl = link.getAttribute("href");
                if (shouldCrawl(nextUrl, keyWord)) {
                    filteredLinks.add(nextUrl.toString());
                }
            }

            // output to the text file
            for (int i = 0; i < titles.size(); i++) {
                System.out.println(titles.get(i).getText());
                myWriter.write(titles.get(i).getText() + " | ");
                myWriter.write(filteredLinks.get(i) + " | ");
                myWriter.write(authors.get(i).getText() + " | ");
                myWriter.write(formattedDates.get(i) + " | ");
            }
            
            System.out.println("current page end===============");

            WebElement nextButton = driver.findElement(By.xpath("//a[text()='Next']"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].focus();", nextButton);
            nextButton.click();

            boolean hasNext = nextButton.isDisplayed();

            if (hasNext) {
                crawl(nextButton.getAttribute("href"), myWriter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            current+=1;
            if(current < topics.size()){
                String nextUrl = topics.get(current).getUrl();
                String nextText = topics.get(current).getText();
                System.out.println("=========== nextText is: "+nextText);
                System.out.println("Current index " + current);
                myWriter.write(nextText + " | ");
                crawl(nextUrl, myWriter);
            } else {
                System.out.println("Quit driver");
                driver.quit();
            }
        }
    }

    public void runReviewList(FileWriter myWriter) {
        String defaultUrl = "https://www.cochranelibrary.com/cdsr/reviews/topics";
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Dady\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
        String keyword = "/search";
        driver.get(defaultUrl);

        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        List<Topic> filteredLinks = new ArrayList<>();

        try {
            for (WebElement link : links) {
                String nextUrl = link.getAttribute("href");
                if (shouldCrawl(nextUrl, keyword)) {
                    Topic topic = new Topic(nextUrl, link.getText());
                    filteredLinks.add(topic);
                    System.out.println(topic.getText());
                    topics.add(topic);
                }
            }
            System.out.println("11111");
            System.out.println("Topic is "+ filteredLinks.get(0).getText());

            myWriter.write(topics.get(0).getText() + " | ");
            crawl(topics.get(0).getUrl(), myWriter);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        String domain = "https://www.cochranelibrary.com";

        try {
            CochraneCrawler crawler = new CochraneCrawler(domain);
            FileWriter myWriter = new FileWriter("cochrane_reviews.txt", true);
            crawler.runReviewList(myWriter);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

//    public static void writeFile(FileWriter writer, String content) {
//        try {
//            writer.write(content);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
