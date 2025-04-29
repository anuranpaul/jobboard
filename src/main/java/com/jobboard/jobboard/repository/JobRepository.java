package com.jobboard.jobboard.repository;

import com.jobboard.jobboard.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.repository.query.Param;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByLocationContainingIgnoreCase(String location);
    List<Job> findByTagsContainingIgnoreCase(String tag);
    @Query(value = "SELECT * FROM job ORDER BY id ASC LIMIT :limit", nativeQuery = true)
    List<Job> findOldestJobs(@Param("limit") int limit);

    /**
     * Full-text search using Postgres tsvector column `search_index`.
     */
    @Query(value = """
        SELECT DISTINCT j.*
          FROM job j
         WHERE (:keyword   IS NULL
                OR j.search_index @@ plainto_tsquery('english', :keyword))
           AND (:location  IS NULL
                OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
           AND (:tag       IS NULL
                OR LOWER(j.tags)     LIKE LOWER(CONCAT('%', :tag, '%')))
           AND (:minSalary IS NULL
                OR j.min_salary >= :minSalary)
         ORDER BY j.id DESC
        """,
        nativeQuery = true)
    List<Job> searchJobs(
        @Param("keyword")   String keyword,
        @Param("location")  String location,
        @Param("tag")       String tag,
        @Param("minSalary") Double minSalary
    );

}
