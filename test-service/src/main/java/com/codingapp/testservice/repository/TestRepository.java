package com.codingapp.testservice.repository;

import com.codingapp.testservice.model.Test;
import com.codingapp.testservice.model.TestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    // For the test creator (interviewer/teacher) to see all tests they have assigned
    List<Test> findByCreatorId(String creatorId);

    // Change findByInviteeEmail to:
    List<Test> findByInviteeId(String inviteeId);

    // For the candidate to filter their dashboard (e.g., "Show me only PENDING tests")
    List<Test> findByInviteeIdAndStatus(String inviteeId, TestStatus status);

}