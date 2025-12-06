$(document).ready(function() {
    $('#forgotPasswordForm').validate({
        rules: {
            email: {
                required: true,
                email: true
            }
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
        },
        submitHandler: function(form) {
            console.log('Forgot Password Valid');
            const $btn = $(form).find('button[type="submit"]');
            $btn.prop('disabled', true).text('Sending Link...');

            setTimeout(function() {
                // Redirect to New Password Page (Demo flow)
                window.location.href = 'new-password.html';
            }, 800);
        }
    });
});