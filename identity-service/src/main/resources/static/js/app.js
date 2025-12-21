$(document).ready(function() {
    $.validator.setDefaults({
        errorElement: 'div',
        errorPlacement: function(error, element) {
            error.addClass('invalid-feedback text-start');
            if (element.prop('type') === 'checkbox') {
                error.insertAfter(element.parent('div'));
            } else if (element.closest('.input-group').length > 0) {
                error.insertAfter(element.closest('.input-group'));
            } else {
                error.insertAfter(element);
            }
        },
        highlight: function(element, errorClass, validClass) {
            $(element).addClass(errorClass).removeClass(validClass);
            $(element).closest('.input-group').addClass('is-invalid').removeClass('is-valid');
        },
        unhighlight: function(element, errorClass, validClass) {
            $(element).removeClass(errorClass).addClass(validClass);
            $(element).closest('.input-group').removeClass('is-invalid').addClass('is-valid');
        }
    });
    $(".toggle-password").click( function () {
        const $btn = $(this);
        const $input = $btn.closest('.input-group').find('input');
        const $icon = $btn.find('i');
        if ($input.attr('type') === 'password') {
            $input.attr('type', 'text');
            $icon.removeClass('fa-eye').addClass('fa-eye-slash');
        } else {
            $input.attr('type', 'password');
            $icon.removeClass('fa-eye-slash').addClass('fa-eye');
        }
    })
})