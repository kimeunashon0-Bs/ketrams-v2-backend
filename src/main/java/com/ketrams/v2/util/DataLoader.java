package com.ketrams.v2.util;

import com.ketrams.v2.entity.*;
import com.ketrams.v2.entity.enums.Role;
import com.ketrams.v2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private SubCountyRepository subCountyRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Load sub-counties and wards if empty
        if (subCountyRepository.count() == 0) {
            loadSubCountiesAndWards();
        }

        // Load institutions and courses if empty
        if (institutionRepository.count() == 0) {
            loadInstitutionsAndCourses();
        }

        // Ensure all institutions have at least one admin user
        createInstitutionUsers();

        // Migrate any existing institution users that might have null institution
        migrateExistingInstitutionUsers();

        // Create sub-county user accounts if they don't exist
        createSubCountyUsers();

        // Create admin user if not exists
        createAdminUser();
    }

    private void loadSubCountiesAndWards() {
        // Create Butere sub-county
        SubCounty butere = new SubCounty();
        butere.setName("Butere");
        butere.setConstituency("Butere Constituency");
        subCountyRepository.save(butere);

        Ward ward1 = new Ward();
        ward1.setName("Marama West");
        ward1.setSubCounty(butere);
        wardRepository.save(ward1);

        Ward ward2 = new Ward();
        ward2.setName("Marama North");
        ward2.setSubCounty(butere);
        wardRepository.save(ward2);

        // Create Ikolomani sub-county
        SubCounty ikolomani = new SubCounty();
        ikolomani.setName("Ikolomani");
        ikolomani.setConstituency("Ikolomani Constituency");
        subCountyRepository.save(ikolomani);

        Ward ward3 = new Ward();
        ward3.setName("Idakho North");
        ward3.setSubCounty(ikolomani);
        wardRepository.save(ward3);
    }

    private void loadInstitutionsAndCourses() {
        SubCounty butere = subCountyRepository.findByName("Butere")
                .orElseThrow(() -> new RuntimeException("Butere sub-county not found"));

        Ward maramaWest = wardRepository.findBySubCountyId(butere.getId()).stream()
                .filter(w -> w.getName().equals("Marama West"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Marama West ward not found"));

        // Butere Technical & Vocational College
        Institution inst1 = new Institution();
        inst1.setName("Butere Technical & Vocational College");
        inst1.setCategory("Public TVET");
        inst1.setSubCounty(butere);
        inst1.setWard(maramaWest);
        institutionRepository.save(inst1);

        Course course1 = new Course();
        course1.setName("Business Management & Entrepreneurship");
        course1.setLevel("Certificate");
        course1.setCategory("Business");
        course1.setInstitution(inst1);
        courseRepository.save(course1);

        Course course2 = new Course();
        course2.setName("ICT");
        course2.setLevel("Certificate");
        course2.setCategory("Technology");
        course2.setInstitution(inst1);
        courseRepository.save(course2);

        // Butere Vocational Training Centre
        Institution inst2 = new Institution();
        inst2.setName("Butere Vocational Training Centre");
        inst2.setCategory("NITA-linked");
        inst2.setSubCounty(butere);
        inst2.setWard(maramaWest);
        institutionRepository.save(inst2);

        Course course3 = new Course();
        course3.setName("Tailoring & Dressmaking");
        course3.setLevel("NITA Grade III");
        course3.setCategory("Fashion");
        course3.setInstitution(inst2);
        courseRepository.save(course3);
    }

    private void createInstitutionUsers() {
        List<Institution> institutions = institutionRepository.findAll();
        for (Institution inst : institutions) {
            String phone = "inst_" + inst.getId();
            if (!appUserRepository.findByPhoneNumber(phone).isPresent()) {
                AppUser user = new AppUser();
                user.setPhoneNumber(phone);
                user.setPasswordHash(passwordEncoder.encode("password"));
                user.setRole(Role.INSTITUTION);
                user.setEnabled(true);
                user.setInstitution(inst);
                appUserRepository.save(user);
            }
        }
    }

    private void migrateExistingInstitutionUsers() {
        // Find all institution users that have no institution set
        List<AppUser> usersWithoutInstitution = appUserRepository.findByRole(Role.INSTITUTION).stream()
                .filter(u -> u.getInstitution() == null)
                .toList();

        for (AppUser user : usersWithoutInstitution) {
            // Try to infer institution from phone number pattern "inst_X"
            String phone = user.getPhoneNumber();
            if (phone != null && phone.startsWith("inst_")) {
                try {
                    Long instId = Long.parseLong(phone.substring(5));
                    Institution institution = institutionRepository.findById(instId).orElse(null);
                    if (institution != null) {
                        user.setInstitution(institution);
                        appUserRepository.save(user);
                        System.out.println("Migrated user " + phone + " to institution " + instId);
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
    }

    private void createSubCountyUsers() {
        // Create a sub-county user for Butere
        String phone = "sub_butere";
        if (!appUserRepository.findByPhoneNumber(phone).isPresent()) {
            AppUser user = new AppUser();
            user.setPhoneNumber(phone);
            user.setPasswordHash(passwordEncoder.encode("password"));
            user.setRole(Role.SUB_COUNTY);
            user.setEnabled(true);
            user.setSubCounty("Butere"); // jurisdiction
            appUserRepository.save(user);
        }
    }

    private void createAdminUser() {
        String adminPhone = "0000000000"; // Numeric phone number to pass validation
        if (!appUserRepository.findByPhoneNumber(adminPhone).isPresent()) {
            AppUser admin = new AppUser();
            admin.setPhoneNumber(adminPhone);
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            appUserRepository.save(admin);
            System.out.println("Default admin created: " + adminPhone + " / admin123");
        }
    }
}