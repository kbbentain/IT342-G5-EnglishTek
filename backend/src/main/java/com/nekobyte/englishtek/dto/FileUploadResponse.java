package com.nekobyte.englishtek.dto;

import lombok.Data;

@Data
public class FileUploadResponse {
    private String fileName;
    private String fileDownloadUri;
    private long size;
}
