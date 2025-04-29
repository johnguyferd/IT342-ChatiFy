package com.chatify.backend.repository;

import com.chatify.backend.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, String> {
    List<GroupMember> findByUserId(String userId);
    List<GroupMember> findByGroupId(String groupId);
    List<GroupMember> findByGroupIdAndUserId(String groupId, String userId);
} 