const registerApi = (function () {
    'use strict';

    const api = new Object();

    api.duplicateCheck = async function (userId) {
        const url      = `http://133.186.144.236:8100/api/users/${userId}/exist`;
        const option   = {
            method : "POST",
            headers: {
                "Content-type": "application/json"
            }
        };
        const response = await fetch(url, option);

        if (!response.ok) {
            console.log(response);
            throw new Error("duplicateCheck Error!");
        }

        const json = await response.json();

        //console.log(json);
        return json.result;
    };

    api.register = async function (userId, userName, userPassword) {
        const url = `http://133.186.144.236:8100/api/users`;

        const user = {
            "userId"      : userId,
            "userName"    : userName,
            "userPassword": userPassword
        }

        const option = {
            method : "POST",
            headers: {
                "Content-type": "application/json"
            },
            body   : JSON.stringify(user)
        };

        const response = await fetch(url, option);

        if (!response.ok) {
            console.log(response);
            throw new Error("register Error!");
        }

        return response.status;
    };

    return api;
});

document.addEventListener("DOMContentLoaded", event => {
    'use strict';

    const registerForm = document.getElementById("register-form");

    registerForm.addEventListener("submit", async event => {
        event.preventDefault();

        if (!validateForm(registerForm)) {
            return;
        }

        const userId       = registerForm['userId'].value.trim();
        const userName     = registerForm['userName'].value.trim();
        const userPassword = registerForm['userPassword1'].value.trim();

        const check = await registerApi().duplicateCheck(userId);

        if (check) {
            alert("userId duplicate!");
            registerForm['userId'].focus();
            return false;
        }

        const code = await registerApi().register(userId, userName, userPassword);

        if (code === 201) {
            alert("회원가입 완료");
        } else {
            alert("회원가입 실패");
        }

    });

    function validateForm(form) {
        if (form['userId'].value.trim() === '') {
            alert("userId empty!");
            form['userId'].focus();
            return false;
        }

        if (form['userName'].value.trim() === '') {
            alert("userName empty!");
            form['userName'].focus();
            return false;
        }

        if (form['userPassword1'].value.trim() === '') {
            alert("userPassword1 empty!");
            form['userPassword1'].focus();
            return false;
        }

        if (form['userPassword2'].value.trim() === '') {
            alert("userPassword2 empty!");
            form['userPassword2'].focus();
            return false;
        }

        if (form['userPassword1'].value !== form['userPassword2'].value) {
            alert("userPassword1 & 2 not equality!");
            form['userPassword2'].focus();
            return false;
        }

        return true;
    }
});