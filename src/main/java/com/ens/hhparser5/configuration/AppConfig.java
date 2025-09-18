package com.ens.hhparser5.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${usePostgres}")
    private boolean usePostgres;
    @Value("${enableRabbitListener}")
    private boolean enableRabbitListener;
    @Value("${vacanciesPerPage}")
    private int vacanciesPerPage = 20;

    @Value("${pagination}")
    private int pagination;

    @Value("${requestDelay}")
    private int requestDelay;

    public int getPagination(){
        return pagination;
    }

    public boolean isUsePostgres() {
        return usePostgres;
    }

    public void setUsePostgres(boolean usePostgres) {
        this.usePostgres = usePostgres;
    }

    public boolean isEnableRabbitListener() {
        return enableRabbitListener;
    }

    public void setEnableRabbitListener(boolean enableRabbitListener) {
        this.enableRabbitListener = enableRabbitListener;
    }

    public int getVacanciesPerPage() {
        return vacanciesPerPage;
    }



    /**
     * Здесь указывается задержка между вызовами API hh.ru
     * @return
     */
    public int getDelay(){
        return requestDelay;
    }
}
