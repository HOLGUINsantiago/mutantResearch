package com.stage.neuroPsi.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @Autowired
    private ResourceLoader resourceLoader;

    // Renvoie l'interface graphique
    @RequestMapping(value = { "/", "/home" })
    public String getIndex() {
        Resource resource = resourceLoader.getResource("classpath:static/index.html");
        try {
            System.out.println(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "forward:/index.html";
    }
}
