package capstone.bwa.demo.models;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// class crawl cac bai viet tren trang webike.vn
public class WeBikeCrawler {
    private String domainURL;
    private Map<String, String> mainCategory;
    private List<String> newsUrlList = new ArrayList<>();



    public WeBikeCrawler(String domainURL) {
        this.mainCategory = new HashMap<>();
        this.domainURL = domainURL;
        this.newsUrlList = new ArrayList<>();
    }

    public static void main(String[] args) {
        WeBikeCrawler crawler = new WeBikeCrawler("https://www.webike.vn/media/");
        try {
            crawler.getMainCategoryURL();
            for (Map.Entry category: crawler.getMainCategory().entrySet()) {
                crawler.crawlNewsURLs(category.getValue().toString());
                break;
            }

            for (String newsURL: crawler.getNewsUrlList()) {
                crawler.crawNews(newsURL);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void crawNews(String newsURL) throws IOException {
        Document document = Jsoup.connect(newsURL).get();
        Elements elements = document.select("div#single_title h1");
        for (Element title: elements) {
            System.out.println("\nTitle : " + title.text());
        }

        String description = "";

        Elements pTags = document.select("div#left_col div[class=\"post clearfix\"] > p:not([style])");
        for (Element p: pTags) {
            description += p.text();
            if(!p.text().equals(""))
                System.out.println("p: " + p.text());
        }
//        System.out.println("Description : " + description);
    }

    public void getMainCategoryURL() throws IOException {
        Document document = Jsoup.connect(domainURL).get();
        Elements categories = document.select("ul.menu li a");
        String title;
        for (Element category: categories) {
            title = category.text().trim();
            if(title.equals("Tin tức – Sự Kiện") ||
                    title.equals("Văn Hóa Giao Thông") ||
                    title.equals("Kinh Nghiệm") ||
                    title.equals("Xe Độ") ||
                    title.equals("Giải Trí")) {
                mainCategory.put(title, category.attr("abs:href"));
            }
        }
    }

    public void crawlNewsURLs(String categoryURL) throws IOException {
        newsUrlList.addAll(getCurrentNewsDetailURL(categoryURL));
        newsUrlList.addAll(getPostSmallNewsDetailURL(categoryURL));
        newsUrlList.addAll(getPostBigNewsDetailURl(categoryURL));
    }

    private List<String> getCurrentNewsDetailURL(String categoryURL) throws IOException {
        List<String> newsDetailURLs = new ArrayList<>();
        Document document = Jsoup.connect(categoryURL).get();
        Elements items = document.select("a.pickup_post_caption");
        for (Element element: items) {
            newsDetailURLs.add(element.attr("href"));
        }
        return newsDetailURLs;
    }

    private List<String> getPostSmallNewsDetailURL(String categoryURL) throws IOException {
        List<String> newsDetailURLs = new ArrayList<>();
        Document document = Jsoup.connect(categoryURL).get();
        Elements items = document.select("div.post_style_small a.image");
        for (Element element: items) {
            newsDetailURLs.add(element.attr("href"));
        }
        return newsDetailURLs;
    }

    private List<String> getPostBigNewsDetailURl(String categoryURL) throws IOException {
        List<String> newsDetailURLs = new ArrayList<>();
        Document document = Jsoup.connect(categoryURL).get();
        Elements items = document.select("div.post_style_big a.image");
        for (Element element: items) {
            newsDetailURLs.add(element.attr("href"));
        }
        return newsDetailURLs;
    }





    public Map<String, String> getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(Map<String, String> mainCategory) {
        this.mainCategory = mainCategory;
    }

    public List<String> getNewsUrlList() {
        return newsUrlList;
    }

    public void setNewsUrlList(List<String> newsUrlList) {
        this.newsUrlList = newsUrlList;
    }
}
