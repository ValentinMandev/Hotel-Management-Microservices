package com.hotel.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserProfileRepository implements JpaRepository<User, Long> {
}
