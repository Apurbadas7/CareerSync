document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("resumeForm");
  const alertBox = document.getElementById("uploadAlert");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const fileInput = document.getElementById("resumeFile");
    const file = fileInput.files[0];

    alertBox.classList.add("d-none");

    if (!file) {
      alertBox.textContent = "⚠️ Please select a resume file.";
      alertBox.className = "alert alert-warning mt-3";
      alertBox.classList.remove("d-none");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch("http://localhost:8080/resumes/upload", {
        method: "POST",
        body: formData,
      });

      if (!response.ok) throw new Error("Upload failed");

      const resume = await response.json();

      // Save resume to localStorage
      localStorage.setItem("resumeData", JSON.stringify(resume));

      // Redirect to jobs page
      window.location.href = "jobs.html";
    } catch (error) {
      console.error("❌ Upload error:", error);
      alertBox.textContent = "❌ Upload failed. Please try again.";
      alertBox.className = "alert alert-danger mt-3";
      alertBox.classList.remove("d-none");
    }
  });
});
