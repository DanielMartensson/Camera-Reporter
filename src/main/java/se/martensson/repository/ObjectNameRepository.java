package se.martensson.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.martensson.entity.ObjectNameEntity;

@Repository
public interface ObjectNameRepository extends JpaRepository<ObjectNameEntity, Long>{

}
