document.addEventListener("DOMContentLoaded", () => {

    const registerForm = document.getElementById("registerForm");
    const password = document.getElementById("password");
    const confirmPassword = document.getElementById("confirmPassword");
    const teamSelect = document.getElementById("teamId");
    const registerMessage = document.getElementById("registerMessage");

    // Load teams for employee registration
    async function loadTeams() {
        try {
            const response = await fetch("publicTeams");

            if (!response.ok) {
                throw new Error("Unable to load teams");
            }

            const teams = await response.json();

            teamSelect.innerHTML =
                '<option value="">Select your team</option>';

            teams.forEach(team => {
                const option = document.createElement("option");

                option.value = team.teamId;
                option.textContent = team.teamName;

                teamSelect.appendChild(option);
            });

        } catch (error) {
            console.error("Team loading error:", error);

            teamSelect.innerHTML =
                '<option value="">Unable to load teams</option>';

            registerMessage.textContent =
                "Unable to load teams. Please refresh the page.";

            registerMessage.style.color = "#c0392b";
        }
    }

    loadTeams();

    // Validate registration form
    registerForm.addEventListener("submit", (event) => {

        if (password.value !== confirmPassword.value) {
            event.preventDefault();

            registerMessage.textContent =
                "Passwords do not match.";

            registerMessage.style.color = "#c0392b";
            confirmPassword.focus();

            return;
        }

        if (password.value.length < 8) {
            event.preventDefault();

            registerMessage.textContent =
                "Password must contain at least 8 characters.";

            registerMessage.style.color = "#c0392b";
            password.focus();

            return;
        }

        if (!teamSelect.value) {
            event.preventDefault();

            registerMessage.textContent =
                "Please select your team.";

            registerMessage.style.color = "#c0392b";

            return;
        }

        registerMessage.textContent = "";
    });

});
