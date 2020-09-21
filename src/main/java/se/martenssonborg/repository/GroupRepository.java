package se.martenssonborg.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import se.martenssonborg.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {

}
