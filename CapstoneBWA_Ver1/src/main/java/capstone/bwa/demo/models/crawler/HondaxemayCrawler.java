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
import java.util.*;

public class HondaxemayCrawler {
    private String domain;
    private List<String> pages;
    private List<String> newsUrls;
    private final String END_LINE_CHAR = "\n";
//    private List<ImageEntity> imageEntities;
    private HashMap<NewsEntity, ImageEntity> results;

    public HondaxemayCrawler() {
//        this.newsCategories = new HashMap<>();
        this.newsUrls = new ArrayList<>();
        this.pages = new ArrayList<>();
//        this.imageEntities = new ArrayList<>();
        results = new HashMap<>();
    }

    public static void main(String[] args) {
        HondaxemayCrawler crawler = new HondaxemayCrawler();
        crawler.setDomain("https://hondaxemay.com.vn/danh-muc-tin-tuc/tin-xe-may/");
        try {
            crawler.crawl();
            for (Map.Entry entry : crawler.getResults().entrySet()) {
                NewsEntity newsEntity = (NewsEntity) entry.getKey();
                System.out.println("===== Title =====");
                System.out.println(newsEntity.getTitle());
                System.out.println("===== Description =====");
                System.out.println(newsEntity.getDescription());

                ImageEntity imageEntity = (ImageEntity) entry.getValue();
                StringTokenizer stringTokenizer = new StringTokenizer(imageEntity.getUrl(), ",");
                while (stringTokenizer.hasMoreElements()) {
                    System.out.println(stringTokenizer.nextToken());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void crawl() throws IOException {
//        List<NewsEntity> newsEntityList = new ArrayList<>();

        getPagesLink();

        getAllNewsLink();
        for (String url: newsUrls) {
//            System.out.println("vo for");
//            System.out.println(url);
            if (url.equals("https://hondaxemay.com.vn/tin-tuc/honda-winner-150-phoi-mau-moi-phong-cach-cung-tem-xe-rieng-biet-doi-dien-mao-them-tao-bao-2/")){

//                System.out.println("Remove");
                newsUrls.remove(url);
                break;
            }


        }

//        NewsEntity newsEntity;
//        Map.Entry<NewsEntity, ImageEntity> newsEntry;
        String url =
"https://hondaxemay.com.vn/tin-tuc/honda-winner-150-phoi-mau-moi-phong-cach-cung-tem-xe-rieng-biet-doi-dien-mao-them-tao-bao/";
//        System.out.println(url);
//        crawlNews(url);
//        url = "https://hondaxemay.com.vn/tin-tuc/honda-winner-150-phoi-mau-moi-phong-cach-cung-tem-xe-rieng-biet-doi-dien-mao-them-tao-bao-2/";
//        System.out.println(url);
//        crawlNews(url);
//        System.out.println(this.results.size());
        for (String newsURL: newsUrls) {
//            crawlNews(newsURL);
//            System.out.println(newsURL);
            crawlNews(newsURL);
//            if(newsEntity.getImgThumbnailUrl() != null)
//                newsEntityList.add(newsEntity);
//            break;
        }

//        this.results.forEach((newsEntity, imageEntity) -> {
//            newsEntity;
//            imageEntity;
//        });
//        return this.results;
    }

    public void crawlNews(String newsURL) throws IOException {
        ImageEntity imageEntity = new ImageEntity();
        String url = crawlImages(newsURL);
        imageEntity.setUrl(url);
        if(imageEntity.getUrl().equals(""))
            return;
        String title = crawlTitle(newsURL);
        String description = crawlText(newsURL);
        NewsEntity newsEntity = new NewsEntity();
        newsEntity.setTitle(title);
        newsEntity.setDescription(description);
        imageEntity.setNewsByOwnId(newsEntity);
//        if(!imageEntities.isEmpty()) {
        newsEntity.setImgThumbnailUrl(getFirstImageURL(imageEntity));
//            for (ImageEntity imageEntity: imageEntities) {
//                imageEntity.setNewsByOwnId(news);
//                this.imageEntities.add(imageEntity);
//            }
        List<ImageEntity> imageEntities = new ArrayList<>();
        imageEntities.add(imageEntity);
            newsEntity.setImagesById(imageEntities);
//        }
//        news.setCreatorId(null);
//        newsEntity.setCategoryId(null);
//        news.setEditorId(null);
        newsEntity.setCreatedTime(java.time.LocalDate.now().toString());
//        news.setEditedTime(null);
        newsEntity.setStatus(NewsStatus.PENDING.toString());
//        news.setCommentsById(null);
//        news.setAccountByCreatorId(null);
//        news.setCategoryByCategoryId(null);
//        news.setAccountByEditorId(null);
//        System.out.println(newsEntity);
        this.results.put(newsEntity, imageEntity);
//        return news;
    }

    private String getFirstImageURL(ImageEntity imageEntity) {
        if(imageEntity != null && !imageEntity.getUrl().equals("")) {
            StringTokenizer tokenizer = new StringTokenizer(imageEntity.getUrl(), ",");
            return tokenizer.nextToken();
        }
        return null;
    }

    private String crawlTitle(String newsURL) throws IOException {
        Document document = Jsoup.connect(newsURL).get();
        Elements elements = document.select("div#content-print h1.title");
        for (Element element: elements) {
            if(!element.text().equals("")) return element.text();
        }
        return null;
    }

    private String crawlText(String newsURL) throws IOException {
        //format cua content:
        //cuoi moi doan se ket thuc bang ky tu \n
        //image se de truc tiep chuoi url
        //cac noi dung kieu liet ke trong <ul> <li> se co dang - noidung, cuoi moi dong van co ky tu \n
        //doan cuoi cung se ko co ky tu \n
        Document document = Jsoup.connect(newsURL).get();
        Elements divElements = document.select("div#content-print div.editable");
        String content = "";
//        ImageEntity imageEntity = new ImageEntity();
        for (Element divElement: divElements) {

            for (Element childEl: divElement.children()) {
                if(childEl.tagName().equals("p")) {
//                    content += handlePElement(childEl, imageEntity);
                    content += childEl.text() + END_LINE_CHAR;
                } else if(childEl.tagName().equals("ul") || childEl.tagName().equals("ol")){
                    content += handleUlLiElement(childEl);
                } else {
                    //ko xu ly truong hop the <table>
//                    content += handlePElement(childEl, imageEntity);
                    content += childEl.text() + END_LINE_CHAR;
                }
            }
        }
        if(!content.equals("")) {
            int index = content.lastIndexOf(END_LINE_CHAR);
            content = content.substring(0, index);
        }
        return content;
    }

    private String crawlImages(String newsUrl) throws IOException {
        Document document = Jsoup.connect(newsUrl).get();
        Elements imgElements = document.select("div#content-print div.editable img[src]");
        String imgUrls = "";
        for (int i = 0; i < imgElements.size(); i++) {
            if(i == (imgElements.size() - 1)) //last element
                imgUrls += imgElements.get(i).attr("src");
            else imgUrls += imgElements.get(i).attr("src") + ",";
        }
        return imgUrls;
    }

//    private String handlePElement(Element pElement) {
//        //cac TH khi gap the <p>
//        //1. chi chua text
//        //2. chi chua img
//        //3. chua text + img
//
//        //case 3
//        if (pElement.hasText() && !pElement.getElementsByAttribute("src").isEmpty()) {
//            String imgs = "";
//            for (Element img: pElement.getElementsByAttribute("src")) {
////                imageEntity = handleImgElement(img);
////                imageEntities.add(imageEntity);
////                imgs += imageEntity.getUrl() + END_LINE_CHAR;
//            }
////            return pElement.text() + END_LINE_CHAR + imgs;
//            return pElement.text() + END_LINE_CHAR;
//        } else if(pElement.hasText() && pElement.getElementsByAttribute("src").isEmpty()) {
//            return pElement.text() + END_LINE_CHAR;
//        } else {
//            //case 2
//            String imgs = "";
//            for (Element element: pElement.getElementsByAttribute("src")) {
////                imageEntity = handleImgElement(element);
//                imageEntities.add(imageEntity);
//                imgs += imageEntity.getUrl() + END_LINE_CHAR;
//            }
//            return imgs;
//        }
//    }

    private String handleUlLiElement(Element ulElement) {
        //cac TH khi gap the <ul><li>
        //1. li chi chua img
        //2. li chi chua text
        //3. li chua text long trong nhieu element
        //4. li chua text long trong nhieu element, chua <div> <img/> </div>
        //5. li chua text long trong nhieu element, sau có <img>
        String content = "";
        for (Element liElement: ulElement.children()) {
            //case 2
//            if (liElement.children().isEmpty()) {
//                if(!liElement.text().equals("")) {
//                    content += "- " + liElement.text() + END_LINE_CHAR;
//                }
//                continue;
//            }

            content += "- " + liElement.text() + END_LINE_CHAR;
            //case 1
//            if (liElement.children().get(0).tagName().equals("img")) {
////                imageEntity = handleImgElement(liElement.children().get(0));
//                imageEntities.add(imageEntity);
//                content += imageEntity.getUrl() + END_LINE_CHAR;
//                continue;
//            }

            //case 4 case 5
//            if (liElement.hasText() && !liElement.getElementsByAttribute("src").isEmpty()) {
//                content += "- " + liElement.text() + END_LINE_CHAR;
////                imageEntity = handleImgElement(liElement.getElementsByAttribute("src").last());
////                imageEntities.add(imageEntity);
////                content += imageEntity.getUrl() + END_LINE_CHAR;
//            } else {
//                //case 3
//                content += "- " + liElement.text() + END_LINE_CHAR;
//            }
        }
        return content;
    }

    private String handleImgElement(Element imgElement) {
//        ImageEntity imageEntity = new ImageEntity();
//        imageEntity.setUrl(imgElement.attr("src"));
//        imageEntity.setStatus("");
//        imageEntity.setType("");
        return imgElement.attr("src");
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























    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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

    public HashMap<NewsEntity, ImageEntity> getResults() {
        return results;
    }

    public void setResults(HashMap<NewsEntity, ImageEntity> results) {
        this.results = results;
    }
}
