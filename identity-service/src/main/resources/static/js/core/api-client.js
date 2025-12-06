class ApiClient {
    constructor() {
        this.csrfToken = $("meta[name='_csrf']").attr("content");
        this.csrfHeader = $("meta[name='_csrf_header']").attr("content");
    }

    /**
     * CORE ENGINE: Chỉ lo việc gửi và xử lý kết quả/lỗi chung
     * Không quan tâm data là json hay query param (do hàm gọi quyết định)
     */
    _coreRequest(settings) {
        const _this = this;

        return new Promise((resolve, reject) => {
            $.ajax({
                ...settings,
                beforeSend: function(xhr) {
                    // Luôn check CSRF cho các method không an toàn (POST, PUT, DELETE)
                    if (settings.type !== 'GET' && _this.csrfHeader && _this.csrfToken) {
                        xhr.setRequestHeader(_this.csrfHeader, _this.csrfToken);
                    }
                    if (settings.loading) App.showLoading(true);
                },
                success: function(res) {
                    if (settings.showSuccess) Notify.success(settings.successMsg);
                    resolve(res);
                },
                error: function(xhr) {
                    if (settings.showError) _this._handleError(xhr);
                    reject(xhr);
                },
                complete: function() {
                    if (settings.loading) App.showLoading(false);
                }
            });
        });
    }

    // --- CÁC METHOD RIÊNG BIỆT (Xử lý option đặc thù) ---

    /**
     * GET: Đặc thù là data chuyển thành Query String, cache có thể bật
     */
    get(url, params = {}, options = {}) {
        // 1. Cấu hình đặc thù cho GET
        const defaults = {
            url: url,
            type: 'GET',
            data: params,      // jQuery tự chuyển object -> ?id=1&name=A
            cache: false,      // Thường API không nên cache
            loading: true,     // GET thường cần loading
            showSuccess: false // GET lấy về xem, ít khi thông báo "Thành công"
        };

        // 2. Trộn options người dùng (nếu muốn override)
        const finalSettings = $.extend(true, {}, defaults, options);

        // 3. Gọi engine
        return this._coreRequest(finalSettings);
    }

    /**
     * POST: Đặc thù là data phải stringify, contentType json, luôn check CSRF
     */
    post(url, data = {}, options = {}) {
        // 1. Cấu hình đặc thù cho POST
        const defaults = {
            url: url,
            type: 'POST',
            data: JSON.stringify(data), // POST JSON thì phải stringify
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            loading: true,
            showSuccess: true,          // POST thường là thêm mới -> Nên báo thành công
            successMsg: "Dữ liệu đã được lưu!"
        };

        // Nếu người dùng muốn upload file (contentType: false), ta không stringify data
        if (options.contentType === false) {
            defaults.data = data; // Giữ nguyên FormData
        }

        const finalSettings = $.extend(true, {}, defaults, options);

        return this._coreRequest(finalSettings);
    }

    /**
     * DELETE: Đặc thù là ít khi gửi body, cảnh báo quan trọng
     */
    delete(url, options = {}) {
        const defaults = {
            url: url,
            type: 'DELETE',
            loading: true,
            showSuccess: true,
            successMsg: "Xóa thành công!"
        };

        const finalSettings = $.extend(true, {}, defaults, options);
        return this._coreRequest(finalSettings);
    }

    // _handleError giữ nguyên như cũ...
    _handleError(xhr) { /* ... */ }
}