package se.martensson.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.martensson.entity.YoloObjectEntity;

@Repository
public interface YoloObjectRepository extends JpaRepository<YoloObjectEntity, Long>{

}
