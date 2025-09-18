package com.ens.hhparser5.repository;

import java.util.Map;

public interface PublicationHistoryRepo {

    public Map<String, Long> findPublication(long projectId, String hhid);

}
