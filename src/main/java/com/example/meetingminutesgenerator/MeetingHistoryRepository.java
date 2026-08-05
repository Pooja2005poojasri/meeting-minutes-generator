package com.example.meetingminutesgenerator;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MeetingHistoryRepository extends JpaRepository<MeetingHistory, Long> {

    List<MeetingHistory> findByUser(User user);
}