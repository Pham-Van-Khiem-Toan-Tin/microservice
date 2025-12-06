$(document).ready(function() {
    $('#loginForm').validate({
        rules: {
            email: {
                required: true,
                email: true
            },
            password: { required: true, minlength: 6 }
        },
        onkeyup: function(element) {
            $(element).valid();   // mỗi lần gõ/xóa sẽ check lại field đó
        },
        onfocusout: function(element) {
            $(element).valid();   // blur ra ngoài cũng check
        },
        messages: {
            email: {
                required: "Please enter a valid email address",
                email: "Please enter correct email format"
            },
            password: {
                required: "Please enter a valid password",
                minlength: "Password must be at least 6 characters"
            }
        },
    });
});
