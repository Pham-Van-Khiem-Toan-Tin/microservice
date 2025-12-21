const api = new ApiClient();
function getQueryParam(name) {
    return new URLSearchParams(window.location.search).get(name);
}
$(document).ready(function () {
    let inputOtp = $("#otp");
    inputOtp.on("keydown",function (e) {
        const key = e.key;

        // Cho phép: backspace, delete, tab, esc, enter, mũi tên, home/end
        const controlKeys = ["Backspace", "Delete", "Tab", "Escape", "Enter", "ArrowLeft", "ArrowRight", "Home", "End"];
        if (controlKeys.includes(key)) return;

        // Cho phép Ctrl/Cmd + A/C/V/X (select all/copy/paste/cut)
        if ((e.ctrlKey || e.metaKey) && ["a", "c", "v", "x"].includes(key.toLowerCase())) return;

        // Chỉ cho số 0-9
        if (!/^\d$/.test(key)) e.preventDefault();
    })
    inputOtp.on("input", function () {
        this.value = this.value.replace(/\D/g, "");
    });
    $("#verifyForm").on("submit",(function (e) {
        e.preventDefault();
        const $form = $(this);
        const email = getQueryParam("e");
        const payload = {
            otp: $.trim($("#otp").val()),
            email: email
        }
        api.post("/verify-email", payload, {
            form: $form,
            timeout: 5000,
            contentType: "application/json; charset=utf-8",
            onSuccess: function (data) {
                Notification.success(data.message, 1500)
                setTimeout(function () {
                    window.location.href = "/login" + data.data;
                }, 1500)
            },
            onError: function () {
                const res = xhr.responseJSON;
                Notification.error(res.messages, 1500)
                api.enableForm($form)
            }
        })
    }))
})