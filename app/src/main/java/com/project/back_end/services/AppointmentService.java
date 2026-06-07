package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CommonService service;

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();
        try {
            Appointment existing = appointmentRepository.findById(appointment.getId()).orElse(null);
            if (existing == null) {
                response.put("message", "Appointment not found");
                return ResponseEntity.badRequest().body(response);
            }

            if (appointment.getPatient() != null && existing.getPatient().getId() != appointment.getPatient().getId()) {
                response.put("message", "Patient ID mismatch");
                return ResponseEntity.badRequest().body(response);
            }

            int validation = service.validateAppointment(appointment);
            if (validation == 0) {
                response.put("message", "Appointment time is not available");
                return ResponseEntity.badRequest().body(response);
            }
            if (validation == -1) {
                response.put("message", "Doctor not found");
                return ResponseEntity.badRequest().body(response);
            }

            existing.setAppointmentTime(appointment.getAppointmentTime());
            existing.setStatus(appointment.getStatus());
            appointmentRepository.save(existing);
            response.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error updating appointment");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();
        try {
            Appointment appointment = appointmentRepository.findById(id).orElse(null);
            if (appointment == null) {
                response.put("message", "Appointment not found");
                return ResponseEntity.badRequest().body(response);
            }

            String email = tokenService.extractEmail(token);
            if (!appointment.getPatient().getEmail().equals(email)) {
                response.put("message", "Unauthorized to cancel this appointment");
                return ResponseEntity.status(401).body(response);
            }

            appointmentRepository.delete(appointment);
            response.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error cancelling appointment");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> result = new HashMap<>();
        try {
            String email = tokenService.extractEmail(token);
            Doctor doctor = doctorRepository.findByEmail(email);
            if (doctor == null) {
                result.put("appointments", List.of());
                return result;
            }

            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);

            List<Appointment> appointments;
            if (pname != null && !pname.isEmpty() && !pname.equals("null")) {
                appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                        doctor.getId(), pname, start, end);
            } else {
                appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), start, end);
            }

            List<AppointmentDTO> dtos = appointments.stream().map(a -> new AppointmentDTO(
                    a.getId(),
                    a.getDoctor().getId(),
                    a.getDoctor().getName(),
                    a.getPatient().getId(),
                    a.getPatient().getName(),
                    a.getPatient().getEmail(),
                    a.getPatient().getPhone(),
                    a.getPatient().getAddress(),
                    a.getAppointmentTime(),
                    a.getStatus()
            )).collect(Collectors.toList());

            result.put("appointments", dtos);
            return result;
        } catch (Exception e) {
            result.put("appointments", List.of());
            return result;
        }
    }

    @Transactional
    public void changeStatus(int status, long id) {
        appointmentRepository.updateStatus(status, id);
    }
}
