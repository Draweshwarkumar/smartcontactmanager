package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smart.entities.Contact;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

    Optional<Contact> findByEmail(String email);
    Optional<Contact> findByPhone(String phone);
}
