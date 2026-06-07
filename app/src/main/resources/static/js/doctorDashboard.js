import { getAllAppointments } from './services/appointmentRecordService.js';
import { createPatientRow } from './components/patientRows.js';

const token = localStorage.getItem("token");
let selectedDate = getFormattedDate(new Date());
let patientName = "null";

document.addEventListener("DOMContentLoaded",() => {
  document.getElementById("datePicker").value = selectedDate;
  loadAppointments();

  document.getElementById("searchBar").addEventListener("input", (e) => {
    const value = e.target.value.trim();
    patientName = value.length > 0 ? value : "null";
    loadAppointments();
  });

  document.getElementById("todayButton").addEventListener("click", () => {
    selectedDate = getFormattedDate(new Date());
    document.getElementById("datePicker").value = selectedDate;
    loadAppointments();
  });

  document.getElementById("datePicker").addEventListener("change", (e) => {
    selectedDate = e.target.value;
    loadAppointments();
  });
});

function getFormattedDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

async function loadAppointments() {
  try {
    const data = await getAllAppointments(selectedDate, patientName, token);
    const tbody = document.getElementById("patientTableBody");
    tbody.innerHTML = "";

    if (!data.appointments || data.appointments.length === 0) {
      const row = document.createElement("tr");
      row.innerHTML = `<td colspan="5" class="noPatientRecord">No Appointments found for today.</td>`;
      tbody.appendChild(row);
      return;
    }

    data.appointments.forEach(({ id, patient, doctor }) => {
      const patientObj = {
        id: patient.id,
        name: patient.name,
        phone: patient.phone,
        email: patient.email
      };
      const row = createPatientRow(patientObj, id, doctor?.id);
      tbody.appendChild(row);
    });
  } catch (error) {
    console.error("Error loading appointments:", error);
    const tbody = document.getElementById("patientTableBody");
    tbody.innerHTML = `<tr><td colspan="5" class="noPatientRecord">Error loading appointments. Try again later.</td></tr>`;
  }
}
