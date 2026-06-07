package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private CommonService service;

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        if (!service.validateToken(token, "patient")) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        return patientService.getPatientDetails(token);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody Patient patient) {
        if (!service.validatePatient(patient)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Patient with email id or phone no already exist");
            return ResponseEntity.status(409).body(response);
        }
        int result = patientService.createPatient(patient);
        Map<String, String> response = new HashMap<>();
        if (result == 1) {
            response.put("message", "Signup successful");
            return ResponseEntity.ok(response);
        }
        response.put("message", "Internal server error");
        return ResponseEntity.internalServerError().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    @GetMapping("/{id}/{user}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(
            @PathVariable Long id,
            @PathVariable String user,
            @PathVariable String token) {
        if (!service.validateToken(token, user)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        return patientService.getPatientAppointment(id, token);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {
        if (!service.validateToken(token, "patient")) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        return service.filterPatient(condition, name, token);
    }
}
