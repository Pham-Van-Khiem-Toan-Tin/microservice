$(document).ready(function() {
    $('#loginForm').submit(function(event) {
        event.preventDefault(); // Ngăn chặn submit mặc định của form

        var formData = $(this).serialize(); // Serialize dữ liệu form

        $.ajax({
            type: 'POST',
            url: '/login',
            contentType: "application/x-www-form-urlencoded",
            data: formData,
            success: function(data, textStatus, request){
                localStorage.setItem("accesstoken",request.getResponseHeader('Authorization'));
                window.location.href = "token";
            },
            error: function(xhr, textStatus, error) {
                console.error('Error:', error);
                // Xử lý lỗi, ví dụ hiển thị thông báo lỗi
            }
        });
    });
});