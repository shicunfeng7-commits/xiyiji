package com.xiyiji.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {
    private String dir = "uploads";

    public String getDir() { return dir; }
    public void setDir(String dir) { this.dir = dir; }
}