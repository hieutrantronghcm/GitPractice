package capstone.bwa.demo.controllers;

import capstone.bwa.demo.entities.ImageEntity;
import capstone.bwa.demo.entities.NewsEntity;
import capstone.bwa.demo.models.crawler.HondaxemayCrawler;
import capstone.bwa.demo.repositories.ImageRepository;
import capstone.bwa.demo.repositories.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/*******************************************************************************
 * ::STATUS::
 * PUBLIC
 * HIDDEN
 *******************************************************************************/

@RestController
public class NewsController {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping("news/crawl")
    public ResponseEntity crawlNews() {
        HondaxemayCrawler crawler =
                new HondaxemayCrawler();
        crawler.setDomain("https://hondaxemay.com.vn/danh-muc-tin-tuc/tin-xe-may/");
        List<NewsEntity> newsEntityList = null;
        try {
            newsEntityList = crawler.crawl();
        } catch (IOException e) {
            e.printStackTrace();
        }
        addNewsToDB(newsEntityList);
        addImageToDB(crawler.getImageEntities());
        //problem: them image co news id dang chua no

        if(newsEntityList == null || newsEntityList.isEmpty())
            return new ResponseEntity(HttpStatus.NO_CONTENT);

        return new ResponseEntity(newsEntityList, HttpStatus.OK);
    }

    private boolean isCrawled(NewsEntity news) {
        return newsRepository.existsByTitle(news.getTitle());
    }

    private void addNewsToDB(List<NewsEntity> newsEntities) {
        newsEntities.forEach(newsEntity -> {
            if (!isCrawled(newsEntity)) {
                newsRepository.saveAndFlush(newsEntity);
                System.out.println("News " + newsEntity.getTitle() + " is New.");
            }
        });
    }

    private void addImageToDB(List<ImageEntity> imageEntities) {
        imageEntities.forEach(imageEntity -> {
            imageRepository.saveAndFlush(imageEntity);
        });
    }
    /**
     * Returns News object
     *
     * @param id of news
     * @return 404 if not found in db
     * 200 if found
     * @apiNote example format
     * {
     * }
     */
    @GetMapping("news/{id}")
    public ResponseEntity getANews(@PathVariable int id) {

        return null;
    }

    /**
     *  Returns list news object sort last by quantity (status public)
     * @param quantity
     * @return 404 if not found in db
     * 200 if found
     */
    @GetMapping("news/limit/{quantity}")
    public ResponseEntity getListNews(@PathVariable int quantity) {

        return null;
    }

    /**
     * Return list news sort last by quantity, status
     * if status = All -> get by quantity
     * @param id
     * @param quantity
     * @param status
     * @return
     * 404 if not found in db
     * 403 if not admin
     * 200 if found
     */
    @GetMapping("admin/{id}/news/limit/{quantity}/status")
    public ResponseEntity getListNewsByAdmin(@PathVariable int id, @PathVariable int quantity, @RequestBody Map<String, String> status) {

        return null;
    }

    /**
     * Returns new event object with status PUBLIC
     *
     * @param id
     * @param body
     * @return 403 if not admin
     * 200 if create success
     */

    @PostMapping("admin/{id}/news")
    public ResponseEntity createNews(@PathVariable int id, @RequestBody Map<String, String> body) {

        return null;
    }

    /**
     * Returns update news object
     * Admin can update event of other admin
     * Mem can only update event of him/herself
     *
     * @param id
     * @param userId
     * @param body
     * @return 403 if not admin
     * 404 if not found news
     * 200 if update success
     */

    @PutMapping("admin/{userId}/news/{id}")
    public ResponseEntity updateNews(@PathVariable int id, @PathVariable int userId, @RequestBody Map<String, String> body) {

        return null;
    }

    /**
     * Returns update status news object
     * @param userId
     * @param id
     * @param status
     * @return 403 if not admin
     * 200 if update status hidden or public
     */
    @PutMapping("admin/{userId}/news/{id}/status")
    public ResponseEntity changeStatusNews(@PathVariable int userId, @PathVariable int id, @RequestBody Map<String, String> status){
        return null;
    }

}
