package capstone.bwa.demo.repositories;

import capstone.bwa.demo.entities.NewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<NewsEntity, Integer>  {
//    boolean findByTitleExists(String title);
    boolean existsByTitle(String title);
}
