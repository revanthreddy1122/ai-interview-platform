package com.interviewplatform.service;

import com.interviewplatform.dto.JobDescriptionMatchRequest;
import com.interviewplatform.dto.JobMatchResponse;
import com.interviewplatform.dto.ResumeAnalysisDTO;
import com.interviewplatform.dto.ResumeDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {

    ResumeDTO uploadResume(MultipartFile file);

    ResumeAnalysisDTO analyzeResume(Long resumeId);

    JobMatchResponse matchJobDescription(JobDescriptionMatchRequest request);

    List<ResumeDTO> getResumeHistory();

    ResumeDTO getResumeById(Long resumeId);
}
