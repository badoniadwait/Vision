package com.project.vision.config;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import nu.pattern.OpenCV;

@Configuration
public class OpenCVConfig {

    private static final AtomicBoolean LOADED = new AtomicBoolean(false);

    @PostConstruct
    public void loadNativeLibrary() {
        if (LOADED.compareAndSet(false, true)) {
            OpenCV.loadLocally();
        }
    }
}