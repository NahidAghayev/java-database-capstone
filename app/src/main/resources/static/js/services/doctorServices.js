import { API_BASE_URL } from "../config/config.js";
const DOCTOR_API = `${API_BASE_URL}/doctor`;

export async function getDoctors() {
  try {
    const response = await fetch(DOCTOR_API);
    const data = await response.json();
    return data.doctors || [];
  } catch (error) {
    console.error("Error fetching doctors:", error);
    return [];
  }
}

export async function deleteDoctor(doctorId, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/deleteDoctor/${doctorId}/${token}`, {
      method: "DELETE"
    });
    const data = await response.json();
    return { success: response.ok, message: data.message || "Doctor deleted" };
  } catch (error) {
    console.error("Error deleting doctor:", error);
    return { success: false, message: "Failed to delete doctor" };
  }
}

export async function saveDoctor(doctor, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/saveDoctor/${token}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(doctor)
    });
    const data = await response.json();
    return { success: response.ok, message: data.message || "Doctor saved" };
  } catch (error) {
    console.error("Error saving doctor:", error);
    return { success: false, message: "Failed to save doctor" };
  }
}

export async function filterDoctors(name, time, specialty) {
  const nameParam = name || "null";
  const timeParam = time || "null";
  const specialtyParam = specialty || "null";

  try {
    const response = await fetch(`${DOCTOR_API}/filter/${nameParam}/${timeParam}/${specialtyParam}`);
    if (response.ok) {
      const data = await response.json();
      return data;
    }
    console.error("Failed to filter doctors:", response.statusText);
    return { doctors: [] };
  } catch (error) {
    console.error("Error filtering doctors:", error);
    alert("Something went wrong while filtering doctors.");
    return { doctors: [] };
  }
}
