let api = new ApiClient();

$(document).ready(function() {
    $('#registerForm').validate({

        rules: {
            firstName: {
                required: true,
                minlength: 2
            },
            lastName: {
                required: true,
                minlength: 2
            },
            email: {
                required: true,
                email: true
            },
            password: {
                required: true,
                minlength: 8
            },
            confirmPassword: {
                required: true,
                equalTo: '#password'
            },
            terms: {
                required: true
            }
        },
        messages: {
            firstName: {
                required: "First Name is required",
                minlength: "At least 2 characters"
            },
            lastName: {
                required: "Last Name is required",
                minlength: "At least 2 characters"
            },
            email: {
                required: "Please enter a valid email address",
                email: "Please enter correct email format"
            },
            password: {
                required: "Password is required",
                minlength: "Password must be at least 8 characters"
            },
            confirmPassword: {
                required: "Please confirm your password",
                equalTo: "Passwords do not match"
            },
            terms: "You must accept the Terms and Privacy Policy"
        },
        onkeyup: function(element) {
            $(element).valid();   // mỗi lần gõ/xóa sẽ check lại field đó
        },
        onfocusout: function(element) {
            $(element).valid();   // blur ra ngoài cũng check
        },
        submitHandler: function(form) {
            const $form = $(this);

            const payload = {
                email: $.trim($("#email").val()),
                firstName: $.trim($("#firstName").val()),
                lastName: $.trim($("#lastName").val()),
                password: $("#password").val(),
                confirmPassword: $("#confirmPassword").val()
            }

            api.post("/register", payload, {
                form: $form,
                timeout: 5000,
                onSuccess: function (data) {
                    Notification.success("Trang đang được chuyển hướng", 1500)

                    const email = encodeURIComponent(payload.email);
                    setTimeout(function () {
                        window.location.href = "/verify-email?e=" + email;
                    }, 1500)
                },
                onError: function () {
                    const res = xhr.responseJSON;
                    console.log(res)
                    Notification.error(res.messages, 1500)
                    api.enableForm($(form))
                }
            })
        }
    });

});