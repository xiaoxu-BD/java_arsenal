package org.xiaoxu.web_boot.service;

import org.springframework.beans.factory.InitializingBean;
import org.xiaoxu.web_boot.enums.Scene;

public interface PayFacade extends InitializingBean {
    void pay();

    Scene getScene();

    @Override
    default void afterPropertiesSet() throws Exception {

    }
}
