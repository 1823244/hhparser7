package com.ens.hhparser5;


import com.ens.hhparser5.configuration.AppConfig;
import com.ens.hhparser5.model.VacancySource;
import com.ens.hhparser5.service.HhruService;
import com.ens.hhparser5.service.HttpRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.assertEquals;

@SpringBootTest
public class HhruServiceTest {

//    @Autowired
//    private HhruService hhruService;
    @Autowired
    private ApplicationContext applicationContext;
    @MockBean
    private HttpRequestService httpRequestService;
    @MockBean
    private AppConfig appConfig;

    /**
     * Этот тест охватывает 3 метода в классе HhruService:
     * - getOnePageForSearchString
     * - getVacanciesByOneSearchString
     * - processSearchTextsList
     *
     * @throws JsonProcessingException
     */
    @Test
    void processSearchTextsList() throws JsonProcessingException {
        //ethalon
        Map<String, VacancySource> allVacanciesByProject = new HashMap<>();
        allVacanciesByProject.put("321654", new VacancySource(321654, "321654"));


        final List<String> searchTextList = new ArrayList<>();
        searchTextList.add("java+test");

        final java.sql.Date currentDate = java.sql.Date.valueOf(LocalDate.now());

        HttpResponse<String> responseStub = new HttpResponseTest();

        //given(this.httpRequestService.executeRequestAndGetResponse("")).willReturn(responseStub);
        String url1 = "https://api.hh.ru/vacancies?area=1&search_field=name&text=java+test&per_page=100&page=0";
        Mockito.when(this.httpRequestService.executeRequestAndGetResponse(url1)).thenReturn(responseStub);
        //given(this.appConfig.getDelay()).willReturn(0);
        Mockito.when(this.appConfig.getDelay()).thenReturn(0);
        Mockito.when(this.appConfig.getVacanciesPerPage()).thenReturn(100);
        Mockito.when(this.appConfig.isUsePostgres()).thenReturn(true);
//
//        HhruService hhruServiceMock = Mockito.mock(HhruService.class);
//
//        Map<String, VacancySourceDto> mockList = new HashMap<>();
//        mockList.put("321654", new VacancySourceDto(321654, "321654"));
//
//        Mockito.when(hhruServiceMock.processSearchTextsList(searchTextList, currentDate))
//                .thenReturn(mockList);
//
        HhruService hhru = applicationContext.getBean(HhruService.class);
        assertEquals(allVacanciesByProject.get("321654"),
                hhru.processSearchTextsList(searchTextList, currentDate).get("321654"));

    }

    static class HttpResponseTest implements HttpResponse<String>{

        @Override
        public int statusCode() {
            return 200;
        }
        @Override
        public String body() {
            return """
                    {
                        "items" : [
                            {
                                "id" : "321654",
                                "name" : "test vacancy for processSearchTextsList()"
                            }
                        ],
                        "found":1,
                        "pages":1,
                        "per_page":100,
                        "page":0,
                        "clusters":null,
                        "arguments":null,
                        "alternate_url":"https://hh.ru/search/vacancy?area=1&enable_snippets=true&items_on_page=100&search_field=name&text=java+test"
                    }
                """;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return null;
        }


        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return null;
        }

        @Override
        public HttpClient.Version version() {
            return null;
        }


    }

}
