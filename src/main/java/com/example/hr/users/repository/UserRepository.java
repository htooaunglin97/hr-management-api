package com.example.hr.users.repository;

import com.example.hr.shared.repository.BaseRepository;
import com.example.hr.users.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<User, Long> {



}
