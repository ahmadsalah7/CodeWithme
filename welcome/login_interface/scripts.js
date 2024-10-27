const LOGIN_SERVER_BASE_URL = "http://44.222.203.63:8081";
const COMPILER_INTERFACE_URL = "http://44.222.203.63/compiler_interface/compiler.html";

document.addEventListener('DOMContentLoaded', function () {
    const signInForm = document.getElementById('sign-in-form');
    const signUpForm = document.getElementById('sign-up-form');
    const showSignUpLink = document.getElementById('show-signup');
    const showSignInLink = document.getElementById('show-signin');

    showSignUpLink.addEventListener('click', function (event) {
        event.preventDefault(); 
        goToSignUp();
    });

    showSignInLink.addEventListener('click', function (event) {
        event.preventDefault();
        goToSignIn();
    });
    
    function goToSignIn(){
        signUpForm.classList.add('hidden');
        signInForm.classList.remove('hidden');
    }
    function goToSignUp(){
        signInForm.classList.add('hidden');
        signUpForm.classList.remove('hidden');
    }
    
    signInForm.addEventListener('submit', function (event) {
        event.preventDefault(); 

        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        const loginData = {
            email: email,
            password: password
        };

        signInJSON(`${LOGIN_SERVER_BASE_URL}/login`, loginData);
    });

    signUpForm.addEventListener('submit', function (event) {
        event.preventDefault(); 

        const username = document.getElementById('signup-username').value;
        const email = document.getElementById('signup-email').value;
        const password = document.getElementById('signup-password').value;
        const rePassword = document.getElementById('signup-repassword').value;
        
        if (password !== rePassword) {
            alert('Passwords do not match!');
            return;
        }

        const signUpData = {
            username: username,
            email: email,
            password: password
        };
        
        signUpJSON(`${LOGIN_SERVER_BASE_URL}/users`, signUpData);
    });

    // GitHub OAuth login link
    window.loginWithGitHub = function () {
        window.location.href = `${LOGIN_SERVER_BASE_URL}/oauth/github`;
    };

    function signInJSON(url, data) {
        fetch(url, {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        })
        .then(response => {
            if(response.ok){
                return response.json();
            } else {
                alert("Invalid Email or Password");
            }
        })
        .then(data => {
            localStorage.setItem("token", data.token);
            window.location.href = COMPILER_INTERFACE_URL;
        })
        .catch((error) => {
            console.log(`Error: ${error}`);
            alert('An error occurred!');
        });
    }

    function signUpJSON(url, data) {
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        })
        .then(response => {
            if (response.status === 201){
                goToSignIn();
            }
            else if (response.status === 409){ 
                alert("User Already Exist");
            }
            else{
                return response.json().then(data =>{
                    alert(`${data.message}, An error occurred! Please try again.`)
                })
            }
        })
        .catch((error) => {
            console.error('Error:', error);
            alert('An error occurred! Please try again.');
        });
    }
});
