package com.diversestudio.unityapi.repository;

import com.diversestudio.unityapi.entities.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> { }
