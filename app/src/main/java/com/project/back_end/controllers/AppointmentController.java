package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private CommonService service;

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token) {
        if (!service.validateToken(token, "doctor")) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        LocalDate localDate = LocalDate.parse(date);
        Map<String, Object> result = appointmentService.getAppointment(patientName, localDate, token);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token) {
        if (!service.validateToken(token, "patient")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        int validation = service.validateAppointment(appointment);
        if (validation == -1) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Doctor not found");
            return ResponseEntity.badRequest().body(error);
        }
        if (validation == 0) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Appointment time is not available");
            return ResponseEntity.badRequest().body(error);
        }
        int result = appointmentService.bookAppointment(appointment);
        if (result == 1) {
            Map<String, String> success = new HashMap<>();
            success.put("message", "Appointment booked successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(success);
        }
        Map<String, String> error = new HashMap<>();
        error.put("message", "Error booking appointment");
        return ResponseEntity.internalServerError().body(error);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token) {
        if (!service.validateToken(token, "patient")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/cancel/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token) {
        if (!service.validateToken(token, "patient")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        return appointmentService.cancelAppointment(id, token);
    }
}
