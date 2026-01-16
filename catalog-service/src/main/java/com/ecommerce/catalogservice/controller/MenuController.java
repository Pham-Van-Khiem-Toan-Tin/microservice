package com.ecommerce.catalogservice.controller;

import com.ecommerce.catalogservice.dto.response.menu.MenuDTO;
import com.ecommerce.catalogservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuController {
    @Autowired
    CategoryService categoryService;
    @GetMapping
    public List<MenuDTO> getMenu() {
        return categoryService.getMenus();
    }
}
