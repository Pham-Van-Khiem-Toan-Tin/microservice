class Notification {
    // Hàm private (nội bộ) để render UI
    static #show(message, type) {
        // Ví dụ dùng Bootstrap Toast hoặc Alert đơn giản
        // Bạn có thể thay thế bằng SweetAlert2 ở đây
        const iconMap = {
            success: '✅',
            warning: '⚠️',
            error: '❌'
        };

        console.log(`${iconMap[type]} [${type.toUpperCase()}]: ${message}`);

        // Ví dụ code giả lập hiển thị Toast
        // Toast.fire({ icon: type, title: message });
        alert(`${type.toUpperCase()}: ${message}`);
    }

    static success(message) {
        this.#show(message, 'success');
    }

    static warning(message) {
        this.#show(message, 'warning');
    }

    static error(message) {
        this.#show(message, 'error');
    }
}