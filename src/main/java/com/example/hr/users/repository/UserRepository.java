package com.example.hr.users.repository;

import com.example.hr.shared.repository.BaseRepository;
import com.example.hr.users.entity.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
public interface UserRepository extends BaseRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

     Optional<User> findByEmailAndIsDeletedFalse(String email);

    List<User> findByIsDeletedFalse();

    @Query("""
        select u from User u
        where u.isDeleted = false
          and (
                lower(u.name) like lower(concat('%', :q, '%'))
             or lower(u.email) like lower(concat('%', :q, '%'))
          )
        order by u.name asc
    """)
    List<User> searchActiveByNameOrEmail(@Param("q") String q);
}
