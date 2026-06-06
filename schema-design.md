# Smart Clinic Management System — Database Schema Design

## MySQL Database Design

### Table: patients
- id: BIGINT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(255), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone: VARCHAR(10), Not Null
- address: VARCHAR(255), Not Null

### Table: doctors
- id: BIGINT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- specialty: VARCHAR(50), Not Null
- email: VARCHAR(255), Not Null, Unique
- password: VARCHAR(255), Not Null
- phone: VARCHAR(10), Not Null
- doctor_available_times: Element Collection (stored in separate join table: doctor_id, available_times VARCHAR(255))

### Table: admin
- id: BIGINT, Primary Key, Auto Increment
- username: VARCHAR(255), Not Null
- password: VARCHAR(255), Not Null

### Table: appointments
- id: BIGINT, Primary Key, Auto Increment
- doctor_id: BIGINT, Foreign Key → doctors(id), Not Null, On Delete Cascade
- patient_id: BIGINT, Foreign Key → patients(id), Not Null, On Delete Cascade
- appointment_time: DATETIME, Not Null
- status: INT, Not Null (0 = Scheduled, 1 = Completed, 2 = Cancelled)

### Table: clinic_locations
- id: BIGINT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- address: VARCHAR(255), Not Null
- phone: VARCHAR(10)
- email: VARCHAR(255)

### Design Justifications

- **CASCADE on delete** for appointments ensures that when a patient or doctor is removed, their appointment history is also cleaned up, avoiding orphaned records.
- **Unique constraint on patient email and doctor email** prevents duplicate accounts and serves as the login identifier.
- **Doctor available times** are stored as an element collection rather than a separate table because they are simple string values tightly coupled to the doctor entity; no additional attributes are needed.
- **Appointment overlap prevention** is enforced at the service layer (not the database), since the clinic may allow overlapping slots for different patients with the same doctor under certain scheduling models.
- **Patient history is retained** — past appointments remain in the database indefinitely, providing a complete medical history for both patients and doctors to reference.
- **Email and phone format validation** is handled via Jakarta Bean Validation annotations on the entity fields (`@Email`, `@Pattern`) and in the frontend JavaScript, keeping the database schema focused on structural integrity rather than format rules.

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "ObjectId('64abc123456')",
  "appointmentId": 51,
  "patientName": "John Smith",
  "medication": ["Paracetamol", "Vitamin C"],
  "dosage": "Paracetamol 500mg — 1 tablet every 6 hours. Vitamin C 1000mg — once daily.",
  "doctorNotes": "Patient reported mild fever. Follow up in 1 week if symptoms persist.",
  "refillCount": 1,
  "tags": ["fever", "pain-relief"],
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  },
  "metadata": {
    "createdAt": "2026-01-15T10:30:00Z",
    "updatedAt": "2026-01-15T10:30:00Z",
    "createdBy": "Dr. Emily Clark"
  }
}
```

### Design Justifications

- **appointmentId** (a Number referencing `appointments.id` in MySQL) links the prescription to a specific appointment rather than embedding the full patient object. This avoids data duplication and keeps the relational source of truth in MySQL.
- **medication** is stored as an array, allowing multiple medicines in a single prescription without a separate join collection.
- **nested pharmacy object** groups related pharmacy metadata together, enabling future features like e-prescription routing without schema changes.
- **tags array** supports flexible categorization and search (e.g., filtering prescriptions by symptom or medication type).
- **metadata subdocument** tracks creation and update timestamps and the prescribing doctor — all optional fields that can evolve independently.
- **Schema flexibility** means new fields (e.g., `allergies`, `duration`, `sideEffects`) can be added to documents at any time without migrations, which is ideal for prescription data that varies per clinic and over time.
