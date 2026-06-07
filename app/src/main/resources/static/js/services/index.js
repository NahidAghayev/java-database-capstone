import { API_BASE_URL } from "../config/config.js";

const ADMIN_API = `${API_BASE_URL}/admin`;
const DOCTOR_API = `${API_BASE_URL}/doctor`;

document.addEventListener("DOMContentLoaded", () => {
  const adminLoginBtn = document.getElementById("adminLogin");
  if (adminLoginBtn) {
    adminLoginBtn.addEventListener("click", () => window.openModal("adminLogin"));
  }

  const doctorLoginBtn = document.getElementById("doctorLogin");
  if (doctorLoginBtn) {
    doctorLoginBtn.addEventListener("click", () => window.openModal("doctorLogin"));
  }
});

window.adminLoginHandler = async function () {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  const admin = { username, password };

  try {
    const response = await fetch(`${ADMIN_API}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(admin)
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem("token", data.token);
      selectRole("admin");
    } else {
      alert("Invalid admin credentials. Please try again.");
    }
  } catch (error) {
    console.error("Admin login error:", error);
    alert("Something went wrong. Please try again later.");
  }
};

window.doctorLoginHandler = async function () {
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  const doctor = { email, password };

  try {
    const response = await fetch(`${DOCTOR_API}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(doctor)
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem("token", data.token);
      selectRole("doctor");
    } else {
      alert("Invalid doctor credentials. Please try again.");
    }
  } catch (error) {
    console.error("Doctor login error:", error);
    alert("Something went wrong. Please try again later.");
  }
};
