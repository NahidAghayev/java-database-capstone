package com.project.back_end.config;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public DataSeeder(AdminRepository adminRepository, DoctorRepository doctorRepository,
                      PatientRepository patientRepository, AppointmentRepository appointmentRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.count() > 0) {
            return;
        }

        Admin admin = new Admin("admin", "admin123");
        adminRepository.save(admin);

        Doctor doc1 = new Doctor("Dr. John Smith", "Cardiology", "john.smith@clinic.com",
                "pass123", "9876543210", List.of("09:00-10:00", "10:00-11:00", "11:00-12:00"));
        Doctor doc2 = new Doctor("Dr. Sarah Johnson", "Pediatrics", "sarah.johnson@clinic.com",
                "pass123", "9876543211", List.of("09:00-10:00", "10:00-11:00", "11:00-12:00"));
        Doctor doc3 = new Doctor("Dr. Michael Brown", "Orthopedics", "michael.brown@clinic.com",
                "pass123", "9876543212", List.of("10:00-11:00", "11:00-12:00", "12:00-13:00"));
        Doctor doc4 = new Doctor("Dr. Emily Davis", "Dermatology", "emily.davis@clinic.com",
                "pass123", "9876543213", List.of("09:00-10:00", "10:00-11:00", "11:00-12:00"));
        Doctor doc5 = new Doctor("Dr. James Wilson", "Neurology", "james.wilson@clinic.com",
                "pass123", "9876543214", List.of("09:00-10:00", "10:00-11:00", "11:00-12:00"));
        Doctor doc6 = new Doctor("Dr. Lisa Garcia", "Ophthalmology", "lisa.garcia@clinic.com",
                "pass123", "9876543215", List.of("10:00-11:00", "11:00-12:00", "12:00-13:00"));
        Doctor doc7 = new Doctor("Dr. Robert Martinez", "General Physician", "robert.martinez@clinic.com",
                "pass123", "9876543216", List.of("09:00-10:00", "10:00-11:00", "11:00-12:00"));
        Doctor doc8 = new Doctor("Dr. Jennifer Taylor", "ENT Specialist", "jennifer.taylor@clinic.com",
                "pass123", "9876543217", List.of("09:00-10:00", "10:00-11:00", "11:00-12:00"));
        doctorRepository.saveAll(List.of(doc1, doc2, doc3, doc4, doc5, doc6, doc7, doc8));

        Patient pat1 = new Patient("Alice Williams", "alice@email.com", "pass123", "1234567890", "123 Main St");
        Patient pat2 = new Patient("Bob Davis", "bob@email.com", "pass123", "1234567891", "456 Oak Ave");
        Patient pat3 = new Patient("Carol Miller", "carol@email.com", "pass123", "1234567892", "789 Pine Rd");
        Patient pat4 = new Patient("David Brown", "david@email.com", "pass123", "1234567893", "321 Elm St");
        Patient pat5 = new Patient("Emma Wilson", "emma@email.com", "pass123", "1234567894", "654 Maple Dr");
        patientRepository.saveAll(List.of(pat1, pat2, pat3, pat4, pat5));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        LocalDateTime dayAfter = now.plusDays(2);

        appointmentRepository.saveAll(List.of(
            // Tomorrow's appointments
            new Appointment(doc1, pat1, tomorrow.withHour(9).withMinute(0).withSecond(0).withNano(0), 0),
            new Appointment(doc2, pat2, tomorrow.withHour(10).withMinute(0).withSecond(0).withNano(0), 0),
            new Appointment(doc4, pat3, tomorrow.withHour(11).withMinute(0).withSecond(0).withNano(0), 0),
            new Appointment(doc7, pat4, tomorrow.withHour(9).withMinute(0).withSecond(0).withNano(0), 0),
            new Appointment(doc1, pat5, tomorrow.withHour(10).withMinute(0).withSecond(0).withNano(0), 0),
            new Appointment(doc3, pat1, tomorrow.withHour(11).withMinute(0).withSecond(0).withNano(0), 0),
            // Day-after appointments
            new Appointment(doc5, pat2, dayAfter.withHour(9).withMinute(0).withSecond(0).withNano(0), 0),
            new Appointment(doc8, pat3, dayAfter.withHour(10).withMinute(0).withSecond(0).withNano(0), 0),
            new Appointment(doc6, pat4, dayAfter.withHour(11).withMinute(0).withSecond(0).withNano(0), 0),
            new Appointment(doc3, pat5, dayAfter.withHour(9).withMinute(0).withSecond(0).withNano(0), 0),
            new Appointment(doc7, pat1, dayAfter.withHour(10).withMinute(0).withSecond(0).withNano(0), 2),
            new Appointment(doc2, pat2, dayAfter.withHour(11).withMinute(0).withSecond(0).withNano(0), 0)
        ));
    }
}
