const loginForm = document.getElementById("loginForm");
const loginMessage = document.getElementById("loginMessage");

const urlParams = new URLSearchParams(window.location.search);

const error = urlParams.get("error");
const registered = urlParams.get("registered");

// Display registration success message
if (registered === "success") {
    loginMessage.textContent =
        "Employee registration successful! Please log in.";

    loginMessage.classList.add("success");
}

// Display login errors returned by LoginController
if (error === "empty") {
    loginMessage.textContent =
        "Please enter your email and password.";

    loginMessage.classList.add("error");
}

if (error === "invalid") {
    loginMessage.textContent =
        "Invalid email or password. Please try again.";

    loginMessage.classList.add("error");
}

// Validate form before submission
loginForm.addEventListener("submit", function (event) {

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    if (email === "" || password.trim() === "") {

        event.preventDefault();

        loginMessage.textContent = "All fields are required.";

        loginMessage.classList.remove("success");
        loginMessage.classList.add("error");
    }
});
