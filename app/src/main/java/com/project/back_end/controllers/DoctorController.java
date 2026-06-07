package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private CommonService service;

    @GetMapping("/{user}/getAvailability/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token) {
        if (!service.validateToken(token, user)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        LocalDate localDate = LocalDate.parse(date);
        List<String> availability = doctorService.getDoctorAvailability(doctorId, localDate);
        Map<String, Object> response = new HashMap<>();
        response.put("availability", availability);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getDoctors")
    public ResponseEntity<Map<String, Object>> getDoctor() {
        List<Doctor> doctors = doctorService.getDoctors();
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctors);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/saveDoctor/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {
        if (!service.validateToken(token, "admin")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        int result = doctorService.saveDoctor(doctor);
        Map<String, String> response = new HashMap<>();
        if (result == 1) {
            response.put("message", "Doctor added to db");
            return ResponseEntity.ok(response);
        } else if (result == -1) {
            response.put("message", "Doctor already exists");
            return ResponseEntity.status(409).body(response);
        } else {
            response.put("message", "Some internal error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/updateDoctor/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {
        if (!service.validateToken(token, "admin")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        int result = doctorService.updateDoctor(doctor);
        Map<String, String> response = new HashMap<>();
        if (result == 1) {
            response.put("message", "Doctor updated");
            return ResponseEntity.ok(response);
        } else if (result == -1) {
            response.put("message", "Doctor not found");
            return ResponseEntity.status(404).body(response);
        } else {
            response.put("message", "Some internal error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/deleteDoctor/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable long id,
            @PathVariable String token) {
        if (!service.validateToken(token, "admin")) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid or expired token");
            return ResponseEntity.status(401).body(error);
        }
        int result = doctorService.deleteDoctor(id);
        Map<String, String> response = new HashMap<>();
        if (result == 1) {
            response.put("message", "Doctor deleted successfully");
            return ResponseEntity.ok(response);
        } else if (result == -1) {
            response.put("message", "Doctor not found with id");
            return ResponseEntity.status(404).body(response);
        } else {
            response.put("message", "Some internal error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {
        Map<String, Object> result = service.filterDoctor(name, speciality, time);
        return ResponseEntity.ok(result);
    }
}
