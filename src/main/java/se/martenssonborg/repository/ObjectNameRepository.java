package se.martenssonborg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.martenssonborg.entity.ObjectNameEntity;

@Repository
public interface ObjectNameRepository extends JpaRepository<ObjectNameEntity, Long>{

}
