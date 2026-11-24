package com.pfe.management.service;

import com.pfe.management.dto.request.ProjectMemberRequest;
import com.pfe.management.dto.response.ProjectMemberResponse;

import java.util.List;

public interface ProjectMemberService {

    ProjectMemberResponse addMember(Long projectId, ProjectMemberRequest request);

    void removeMember(Long projectId, Long userId);

    List<ProjectMemberResponse> getProjectMembers(Long projectId);
}
