package org.xiaoxu.service;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelService {
    void importCustomer(MultipartFile file);
}
