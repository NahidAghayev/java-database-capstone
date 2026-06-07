package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    @Transactional
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        List<String> allSlots = List.of("09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00");
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        var appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
        Set<String> booked = appointments.stream()
                .map(a -> String.format("%02d:00-%02d:00", a.getAppointmentTime().getHour(), a.getAppointmentTime().getHour() + 1))
                .collect(Collectors.toSet());
        return allSlots.stream().filter(s -> !booked.contains(s)).collect(Collectors.toList());
    }

    public int saveDoctor(Doctor doctor) {
        try {
            Doctor existing = doctorRepository.findByEmail(doctor.getEmail());
            if (existing != null) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public int updateDoctor(Doctor doctor) {
        try {
            Doctor existing = doctorRepository.findById(doctor.getId()).orElse(null);
            if (existing == null) {
                return -1;
            }
            if (doctor.getName() != null) existing.setName(doctor.getName());
            if (doctor.getSpecialty() != null) existing.setSpecialty(doctor.getSpecialty());
            if (doctor.getEmail() != null) existing.setEmail(doctor.getEmail());
            if (doctor.getPassword() != null) existing.setPassword(doctor.getPassword());
            if (doctor.getPhone() != null) existing.setPhone(doctor.getPhone());
            if (doctor.getAvailableTimes() != null) existing.setAvailableTimes(doctor.getAvailableTimes());
            doctorRepository.save(existing);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional
    public int deleteDoctor(long id) {
        try {
            Optional<Doctor> doctor = doctorRepository.findById(id);
            if (doctor.isEmpty()) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Doctor doctor = doctorRepository.findByEmail(login.getEmail());
            if (doctor == null) {
                response.put("message", "Doctor not found");
                return ResponseEntity.status(401).body(response);
            }
            if (!doctor.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid password");
                return ResponseEntity.status(401).body(response);
            }
            String token = tokenService.generateToken(doctor.getEmail());
            response.put("token", token);
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Internal error");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Transactional
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> result = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        result.put("doctors", doctors);
        return result;
    }

    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        List<Doctor> filtered = filterDoctorByTime(doctors, amOrPm);
        Map<String, Object> result = new HashMap<>();
        result.put("doctors", filtered);
        return result;
    }

    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        List<Doctor> byName = doctorRepository.findByNameLike(name);
        List<Doctor> filtered = filterDoctorByTime(byName, amOrPm);
        Map<String, Object> result = new HashMap<>();
        result.put("doctors", filtered);
        return result;
    }

    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        Map<String, Object> result = new HashMap<>();
        result.put("doctors", doctors);
        return result;
    }

    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        List<Doctor> bySpecialty = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        List<Doctor> filtered = filterDoctorByTime(bySpecialty, amOrPm);
        Map<String, Object> result = new HashMap<>();
        result.put("doctors", filtered);
        return result;
    }

    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        Map<String, Object> result = new HashMap<>();
        result.put("doctors", doctors);
        return result;
    }

    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        List<Doctor> all = doctorRepository.findAll();
        List<Doctor> filtered = filterDoctorByTime(all, amOrPm);
        Map<String, Object> result = new HashMap<>();
        result.put("doctors", filtered);
        return result;
    }

    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        if (amOrPm == null || amOrPm.isEmpty() || amOrPm.equals("null")) {
            return doctors;
        }
        return doctors.stream().filter(d -> {
            if (d.getAvailableTimes() == null) return false;
            return d.getAvailableTimes().stream().anyMatch(t -> isTimeInPeriod(t, amOrPm));
        }).collect(Collectors.toList());
    }

    private boolean isTimeInPeriod(String timeSlot, String amOrPm) {
        int startHour = Integer.parseInt(timeSlot.split("-")[0].split(":")[0]);
        if (amOrPm.equalsIgnoreCase("AM")) {
            return startHour < 12;
        } else {
            return startHour >= 12;
        }
    }
}
