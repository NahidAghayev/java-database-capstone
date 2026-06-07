package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class CommonService {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    public boolean validateToken(String token, String user) {
        try {
            return tokenService.validateToken(token, user);
        } catch (Exception e) {
            return false;
        }
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            if (admin == null) {
                response.put("message", "Admin not found");
                return ResponseEntity.status(401).body(response);
            }
            if (!admin.getPassword().equals(receivedAdmin.getPassword())) {
                response.put("message", "Invalid password");
                return ResponseEntity.status(401).body(response);
            }
            String token = tokenService.generateToken(admin.getUsername());
            response.put("token", token);
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Internal error");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        boolean hasName = name != null && !name.isEmpty() && !name.equals("null");
        boolean hasSpecialty = specialty != null && !specialty.isEmpty() && !specialty.equals("null");
        boolean hasTime = time != null && !time.isEmpty() && !time.equals("null");

        if (hasName && hasSpecialty && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
        } else if (hasName && hasSpecialty) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        } else if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(name, time);
        } else if (hasSpecialty && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(specialty, time);
        } else if (hasName) {
            return doctorService.findDoctorByName(name);
        } else if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(specialty);
        } else if (hasTime) {
            return doctorService.filterDoctorsByTime(time);
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("doctors", doctorService.getDoctors());
            return result;
        }
    }

    public int validateAppointment(Appointment appointment) {
        try {
            Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);
            if (doctor == null) {
                return -1;
            }
            LocalDate date = appointment.getAppointmentTime().toLocalDate();
            List<String> available = doctorService.getDoctorAvailability(doctor.getId(), date);
            String slot = String.format("%02d:00-%02d:00",
                    appointment.getAppointmentTime().getHour(),
                    appointment.getAppointmentTime().getHour() + 1);
            return available.contains(slot) ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean validatePatient(Patient patient) {
        try {
            Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
            return existing == null;
        } catch (Exception e) {
            return false;
        }
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(login.getEmail());
            if (patient == null) {
                response.put("message", "Patient not found");
                return ResponseEntity.status(401).body(response);
            }
            if (!patient.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid password");
                return ResponseEntity.status(401).body(response);
            }
            String token = tokenService.generateToken(patient.getEmail());
            response.put("token", token);
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Internal error");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        try {
            String email = tokenService.extractEmail(token);
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Patient not found");
                return ResponseEntity.status(401).body(response);
            }

            boolean hasCondition = condition != null && !condition.isEmpty() && !condition.equals("null");
            boolean hasName = name != null && !name.isEmpty() && !name.equals("null");

            if (hasCondition && hasName) {
                return patientService.filterByDoctorAndCondition(condition, name, patient.getId());
            } else if (hasCondition) {
                return patientService.filterByCondition(condition, patient.getId());
            } else if (hasName) {
                return patientService.filterByDoctor(name, patient.getId());
            } else {
                return patientService.getPatientAppointment(patient.getId(), token);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error filtering patient appointments");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
