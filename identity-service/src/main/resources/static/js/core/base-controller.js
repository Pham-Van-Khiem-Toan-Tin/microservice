class BaseController {
    constructor() {
        // Kiểm tra xem trang hiện tại có đúng là trang cần chạy controller này không
        // Logic: Mỗi trang HTML sẽ có 1 thẻ <div id="page-xyz">
        if (this.pageId && $(`#${this.pageId}`).length > 0) {
            this.init();
        }
    }

    // Các hàm này sẽ được Override ở class con
    init() {
        this.loadData();
        this.registerEvents();
    }

    loadData() {}
    registerEvents() {}
}