package se.martenssonborg.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import se.martenssonborg.entity.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByNameContainingIgnoreCase(String name);

}
