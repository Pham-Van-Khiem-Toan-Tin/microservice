class ApiClient {
    constructor(options = {}) {
        this.baseUrl = options.baseUrl || "";
        this.defaultTimeout = options.timeout || 15000;
        this.defaultHeaders = options.headers || {};
        this.globalBeforeSend = options.beforeSend || null;
        this.globalAfterSend = options.afterSend || null;
    }

    disableForm($form) {
        if (!$form || !$form.length) return;
        $form.find("input, select, textarea, button").prop("disabled", true);
        $form.addClass("form-disabled");
        $form.find(".form-overlay").removeClass("d-none");
    }

    enableForm($form) {
        if (!$form || !$form.length) return;
        $form.find("input, select, textarea, button").prop("disabled", false);
        $form.removeClass("form-disabled");
        $form.find(".form-overlay").addClass("d-none");
    }

    _fullUrl(url) {
        if (/^https?:\/\//i.test(url)) return url;
        return `${this.baseUrl}${url.startsWith("/") ? "" : "/"}${url}`;
    }

    _normalizeError(xhr) {
        const res = xhr.responseJSON;
        return {
            status: xhr.status,
            message:
                res?.messages ||
                res?.message ||
                xhr.statusText ||
                "Có lỗi xảy ra",
            raw: res
        };
    }

    request({
                url,
                method = "GET",
                data,
                form,
                headers,
                timeout,

                showError = true,
                showSuccess,

                // callbacks
                beforeSend,
                afterSend,
                onSuccess,
                onError,
            }) {
        const isFormData = (typeof FormData !== "undefined") && (data instanceof FormData);
        const $form = form ? $(form) : null;

        $.ajax({
            url: this._fullUrl(url),
            type: method,
            timeout: timeout ?? this.defaultTimeout,
            headers: { ...this.defaultHeaders, ...(headers || {}) },

            contentType: isFormData ? false : "application/json; charset=utf-8",
            processData: !isFormData,
            data:
                data === undefined
                    ? undefined
                    : (isFormData ? data : JSON.stringify(data)),

            beforeSend: (xhr, settings) => {
                this.globalBeforeSend && this.globalBeforeSend(xhr, settings);
                if ($form && $form.length) this.disableForm($form);
                beforeSend && beforeSend(xhr, settings);
            },

            success: (res) => {
                if (showSuccess) Notification.success(showSuccess, 1500);
                onSuccess && onSuccess(res);
            },

            error: (xhr) => {
                const err = this._normalizeError(xhr);
                if (showError) Notification.error(err.message, 1500);
                onError && onError(err, xhr);
            },

            complete: () => {
                if ($form && $form.length) this.enableForm($form);
                afterSend && afterSend();
                this.globalAfterSend && this.globalAfterSend();
            }
        });
    }

    get(url, options = {}) {
        this.request({ url, method: "GET", ...options });
    }

    post(url, data, options = {}) {
        this.request({ url, method: "POST", data, ...options });
    }

    put(url, data, options = {}) {
        this.request({ url, method: "PUT", data, ...options });
    }

    patch(url, data, options = {}) {
        this.request({ url, method: "PATCH", data, ...options });
    }

    delete(url, options = {}) {
        this.request({ url, method: "DELETE", ...options });
    }
}
