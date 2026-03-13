package com.ketrams.v2.service.student;

import com.ketrams.v2.dto.request.StudentProfileDto;
import com.ketrams.v2.entity.AppUser;
import com.ketrams.v2.entity.StudentProfile;
import com.ketrams.v2.repository.AppUserRepository;
import com.ketrams.v2.repository.StudentProfileRepository;
import com.ketrams.v2.service.file.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {

    private final StudentProfileRepository profileRepository;
    private final AppUserRepository appUserRepository;
    private final FileStorageService fileStorageService;

    // Constructor injection
    public StudentService(StudentProfileRepository profileRepository,
                          AppUserRepository appUserRepository,
                          FileStorageService fileStorageService) {
        this.profileRepository = profileRepository;
        this.appUserRepository = appUserRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public StudentProfile createOrUpdateProfile(AppUser user, StudentProfileDto dto) throws IOException {
        AppUser managedUser = appUserRepository.getReferenceById(user.getId());

        StudentProfile profile = profileRepository.findById(managedUser.getId())
                .orElse(new StudentProfile());

        profile.setUser(managedUser);
        profile.setFullName(dto.getFullName());
        profile.setGender(dto.getGender());
        profile.setDisabilityStatus(dto.getDisabilityStatus());
        profile.setDisabilityType(dto.getDisabilityType());
        profile.setIdNumber(dto.getIdNumber());
        profile.setBirthCertNumber(dto.getBirthCertNumber());
        profile.setParentName(dto.getParentName());
        profile.setParentPhone(dto.getParentPhone());
        profile.setParentRelationship(dto.getParentRelationship());
        profile.setCounty(dto.getCounty() != null ? dto.getCounty() : "Kakamega");
        profile.setSubCounty(dto.getSubCounty());
        profile.setWard(dto.getWard());
        profile.setPreviousSchool(dto.getPreviousSchool());
        profile.setHighestQualification(dto.getHighestQualification());

        // Handle multiple document uploads
        List<MultipartFile> documents = dto.getDocuments();
        if (documents != null && !documents.isEmpty()) {
            List<String> filePaths = new ArrayList<>();
            for (MultipartFile file : documents) {
                if (!file.isEmpty()) {
                    String filename = fileStorageService.storeFile(file);
                    filePaths.add(filename);
                }
            }
            profile.setDocumentUrlsList(filePaths);
        }

        return profileRepository.save(profile);
    }
}