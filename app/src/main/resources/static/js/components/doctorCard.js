import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";

export function createDoctorCard(doctor) {
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  const nameEl = document.createElement("h3");
  nameEl.textContent = doctor.name;

  const specialtyEl = document.createElement("p");
  specialtyEl.textContent = doctor.specialty;

  const emailEl = document.createElement("p");
  emailEl.textContent = doctor.email;

  const timesEl = document.createElement("div");
  timesEl.classList.add("available-times");
  if (doctor.availableTimes && doctor.availableTimes.length > 0) {
    doctor.availableTimes.forEach(time => {
      const span = document.createElement("span");
      span.classList.add("time-slot");
      span.textContent = time;
      timesEl.appendChild(span);
    });
  }

  infoDiv.appendChild(nameEl);
  infoDiv.appendChild(specialtyEl);
  infoDiv.appendChild(emailEl);
  infoDiv.appendChild(timesEl);

  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  if (role === "admin") {
    const deleteBtn = document.createElement("button");
    deleteBtn.textContent = "Delete";
    deleteBtn.classList.add("adminBtn");
    deleteBtn.addEventListener("click", async () => {
      if (!token) return;
      const result = await deleteDoctor(doctor.id, token);
      if (result.success) {
        card.remove();
      } else {
        alert("Failed to delete doctor: " + result.message);
      }
    });
    actionsDiv.appendChild(deleteBtn);
  } else if (role === "patient") {
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "Book Now";
    bookBtn.addEventListener("click", () => {
      alert("Please log in first");
    });
    actionsDiv.appendChild(bookBtn);
  } else if (role === "loggedPatient") {
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "Book Now";
    bookBtn.addEventListener("click", async (e) => {
      if (!token) {
        window.location.href = "/pages/patientDashboard.html";
        return;
      }
      const patient = await getPatientData(token);
      if (!patient) {
        alert("Could not fetch patient details.");
        return;
      }
      showBookingOverlay(e, doctor, patient);
    });
    actionsDiv.appendChild(bookBtn);
  }

  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);

  return card;
}

function showBookingOverlay(e, doctor, patient) {
  const button = e.target;
  const ripple = document.createElement("div");
  ripple.classList.add("ripple-overlay");
  ripple.style.left = `${e.clientX}px`;
  ripple.style.top = `${e.clientY}px`;
  document.body.appendChild(ripple);
  setTimeout(() => ripple.classList.add("active"), 50);

  const modalApp = document.createElement("div");
  modalApp.classList.add("modalApp");
  modalApp.innerHTML = `
    <h2>Book Appointment</h2>
    <input class="input-field" type="text" value="${patient.name}" disabled />
    <input class="input-field" type="text" value="${doctor.name}" disabled />
    <input class="input-field" type="text" value="${doctor.specialty}" disabled />
    <input class="input-field" type="email" value="${doctor.email}" disabled />
    <input class="input-field" type="date" id="appointment-date" />
    <select class="input-field" id="appointment-time">
      <option value="">Select time</option>
      ${doctor.availableTimes ? doctor.availableTimes.map(t => `<option value="${t}">${t}</option>`).join('') : ''}
    </select>
    <button class="confirm-booking">Confirm Booking</button>
  `;
  document.body.appendChild(modalApp);
  setTimeout(() => modalApp.classList.add("active"), 600);

  modalApp.querySelector(".confirm-booking").addEventListener("click", async () => {
    const date = modalApp.querySelector("#appointment-date").value;
    const time = modalApp.querySelector("#appointment-time").value;
    const token = localStorage.getItem("token");
    const startTime = time.split('-')[0];
    const appointment = {
      doctor: { id: doctor.id },
      patient: { id: patient.id },
      appointmentTime: `${date}T${startTime}:00`,
      status: 0
    };

    const { bookAppointment } = await import("../services/appointmentRecordService.js");
    const { success, message } = await bookAppointment(appointment, token);

    if (success) {
      alert("Appointment Booked successfully");
      ripple.remove();
      modalApp.remove();
    } else {
      alert("Failed to book an appointment: " + message);
    }
  });
}
