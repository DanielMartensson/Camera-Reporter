package se.martenssonborg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.martenssonborg.entity.YoloObjectEntity;

@Repository
public interface YoloObjectRepository extends JpaRepository<YoloObjectEntity, Long>{

}
