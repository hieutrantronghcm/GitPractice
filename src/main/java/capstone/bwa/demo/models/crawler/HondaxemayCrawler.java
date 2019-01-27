package capstone.bwa.demo.models.crawler;

import capstone.bwa.demo.constants.NewsStatus;
import capstone.bwa.demo.entities.CategoryEntity;
import capstone.bwa.demo.entities.ImageEntity;
import capstone.bwa.demo.entities.NewsEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HondaxemayCrawler {
    private String domain;
    private Map<String, String> newsCategories;
    private List<String> pages;
    private List<String> newsUrls;
    private final String END_LINE_CHAR = "\n";
    private List<ImageEntity> imageEntities;

    public HondaxemayCrawler() {
        this.newsCategories = new HashMap<>();
        this.newsUrls = new ArrayList<>();
        this.pages = new ArrayList<>();
        this.imageEntities = new ArrayList<>();
    }

    public List<NewsEntity> crawl() throws IOException {
        List<NewsEntity> newsEntityList = new ArrayList<>();

        getPagesLink();

        getAllNewsLink();

        NewsEntity newsEntity;
        for (String newsURL: newsUrls) {
            newsEntity = crawlNews(newsURL);
            if(newsEntity.getImgThumbnailUrl() != null)
                newsEntityList.add(newsEntity);
        }

        return newsEntityList;
    }

    public NewsEntity crawlNews(String newsURL) throws IOException {
        String title = crawlTitle(newsURL);
        List<ImageEntity> imageEntities = new ArrayList<>();
        String description = crawlContent(newsURL, imageEntities);

        NewsEntity news = new NewsEntity();
        news.setTitle(title);
        news.setDescription(description);

        if(!imageEntities.isEmpty()) {
            news.setImgThumbnailUrl(imageEntities.get(0).getUrl());
            for (ImageEntity imageEntity: imageEntities) {
                imageEntity.setNewsByOwnId(news);
                this.imageEntities.add(imageEntity);
            }
//            news.setImagesById(imageEntities);
        }
//        news.setCreatorId(null);
//        news.setCategoryId(null);
//        news.setEditorId(null);
//        news.setCreatedTime(null);
//        news.setEditedTime(null);
        news.setStatus(NewsStatus.PENDING.toString());
//        news.setCommentsById(null);
//        news.setAccountByCreatorId(null);
//        news.setCategoryByCategoryId(null);
//        news.setAccountByEditorId(null);

        return news;
    }

    private String crawlTitle(String newsURL) throws IOException {
        Document document = Jsoup.connect(newsURL).get();
        Elements elements = document.select("div#content-print h1.title");
        for (Element element: elements) {
            if(!element.text().equals("")) return element.text();
        }
        return null;
    }

    private String crawlContent(String newsURL, List<ImageEntity> imageEntities) throws IOException {
        //format cua content:
        //cuoi moi doan se ket thuc bang ky tu \n
        //image se de truc tiep chuoi url
        //cac noi dung kieu liet ke trong <ul> <li> se co dang - noidung, cuoi moi dong van co ky tu \n
        //doan cuoi cung se ko co ky tu \n
        Document document = Jsoup.connect(newsURL).get();
        Elements divElements = document.select("div#content-print div.editable");
        String content = "";
        for (Element divElement: divElements) {

            for (Element childEl: divElement.children()) {
                if(childEl.tagName().equals("p")) {
                    content += handlePElement(childEl, imageEntities);
                } else if(childEl.tagName().equals("ul") || childEl.tagName().equals("ol")){
                    content += handleUlLiElement(childEl, imageEntities);
                } else {
                    //ko xu ly truong hop the <table>
                    content += handlePElement(childEl, imageEntities);
                }
            }
        }
        if(!content.equals("")) {
            int index = content.lastIndexOf(END_LINE_CHAR);
            content = content.substring(0, index);
        }
        return content;
    }

    private String handlePElement(Element pElement, List<ImageEntity> imageEntities) {
        //cac TH khi gap the <p>
        //1. chi chua text
        //2. chi chua img
        //3. chua text + img

        ImageEntity imageEntity;
        //case 3
        if (pElement.hasText() && !pElement.getElementsByAttribute("src").isEmpty()) {
            String imgs = "";
            for (Element img: pElement.getElementsByAttribute("src")) {
                imageEntity = handleImgElement(img);
                imageEntities.add(imageEntity);
                imgs += imageEntity.getUrl() + END_LINE_CHAR;
            }
            return pElement.text() + END_LINE_CHAR + imgs;
        } else if(pElement.hasText() && pElement.getElementsByAttribute("src").isEmpty()) {
            return pElement.text() + END_LINE_CHAR;
        } else {
            //case 2
            String imgs = "";
            for (Element element: pElement.getElementsByAttribute("src")) {
                imageEntity = handleImgElement(element);
                imageEntities.add(imageEntity);
                imgs += imageEntity.getUrl() + END_LINE_CHAR;
            }
            return imgs;
        }
    }

    private String handleUlLiElement(Element ulElement, List<ImageEntity> imageEntities) {
        //cac TH khi gap the <ul><li>
        //1. li chi chua img
        //2. li chi chua text
        //3. li chua text long trong nhieu element
        //4. li chua text long trong nhieu element, chua <div> <img/> </div>
        //5. li chua text long trong nhieu element, sau có <img>
        String content = "";
        ImageEntity imageEntity;
        for (Element liElement: ulElement.children()) {
            //case 2
            if (liElement.children().isEmpty()) {
                if(!liElement.text().equals("")) {
                    content += "- " + liElement.text() + END_LINE_CHAR;
                }
                continue;
            }

            //case 1
            if (liElement.children().get(0).tagName().equals("img")) {
                imageEntity = handleImgElement(liElement.children().get(0));
                imageEntities.add(imageEntity);
                content += imageEntity.getUrl() + END_LINE_CHAR;
                continue;
            }

            //case 4 case 5
            if (liElement.hasText() && !liElement.getElementsByAttribute("src").isEmpty()) {
                content += "- " + liElement.text() + END_LINE_CHAR;
                imageEntity = handleImgElement(liElement.getElementsByAttribute("src").last());
                imageEntities.add(imageEntity);
                content += imageEntity.getUrl() + END_LINE_CHAR;
            } else {
                //case 3
                content += "- " + liElement.text() + END_LINE_CHAR;
            }
        }
        return content;
    }

    private ImageEntity handleImgElement(Element imgElement) {
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setUrl(imgElement.attr("src"));
//        imageEntity.setStatus("");
//        imageEntity.setType("");
        return imageEntity;
    }

    //add all news link to list pages
    private void getAllNewsLink() throws IOException {
        //get link large-left news
        //all page will have same this news, so we just crawl 1 time at first page
        crawlNewsLink(this.pages.get(0), "div.large-left > a");

        //get link small-box news
        //all page will have same this news, so we just crawl 1 time at first page
        crawlNewsLink(this.pages.get(0), "div.small-box > a");

        for (String page: this.pages) {
            //get link row-list news
            crawlNewsLink(page, "div.row-list > div.news-item > div.inner > a");
        }
    }

    public void getPagesLink() throws IOException {
        Document document = Jsoup.connect(domain).get();
        Elements pageNumbers = document.select("ul.pagination li a[class^='page-numbers']");

        //cat bo so trang da co
        //vd: /page/24/ -> /page/
        int lastPageNumber = Integer.parseInt(pageNumbers.last().text());
        String url = pageNumbers.last().attr("href");
        int secondIndex = nthLastIndexOf(2, "/", url);
        String temp = url.substring(0, secondIndex + 1);

        //thay so vao de ra tung link
        for (int i = lastPageNumber; i >= 1 ; i--) {
            pages.add(temp + i + "/");
        }
    }

    //get n th last character
    private int nthLastIndexOf(int nth, String ch, String string) {
        if (nth <= 0) return string.length();
        return nthLastIndexOf(--nth, ch, string.substring(0, string.lastIndexOf(ch)));
    }

    //get 1 news link
    private void crawlNewsLink(String pageLink, String cssQuery) throws IOException {
        Document document = Jsoup.connect(pageLink).get();
        Elements newsLinks = document.select(cssQuery);
        for (Element link: newsLinks) {
            newsUrls.add(link.attr("href"));
        }
    }






















    public List<ImageEntity> getImageEntities() {
        return imageEntities;
    }

    public void setImageEntities(List<ImageEntity> imageEntities) {
        this.imageEntities = imageEntities;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Map<String, String> getNewsCategories() {
        return newsCategories;
    }

    public void setNewsCategories(Map<String, String> newsCategories) {
        this.newsCategories = newsCategories;
    }

    public List<String> getNewsUrls() {
        return newsUrls;
    }

    public void setNewsUrls(List<String> newsUrls) {
        this.newsUrls = newsUrls;
    }

    public List<String> getPages() {
        return pages;
    }

    public void setPages(List<String> pages) {
        this.pages = pages;
    }
}
