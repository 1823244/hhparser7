package com.ens.hhparser5;

import com.ens.hhparser5.configuration.AppConfig;
import com.ens.hhparser5.model.Project;
import com.ens.hhparser5.repository.ProjectRepo;
import com.ens.hhparser5.service.HttpRequestService;
import com.ens.hhparser5.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ProjectServiceTest {

    @Autowired
    private ProjectRepo projectRepo;
    @MockBean
    private HttpRequestService httpRequestService;
    @MockBean
    private AppConfig appConfig;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Фактически, это интеграционный тест
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    void testFeatureSubproject() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Project project = projectRepo.findByName("java");
        final Date currentDate = Date.valueOf(LocalDate.now());

        HttpResponse<String> httpResponseStub_java_test = new HttpResponseStub_java_test();
        HttpResponse<String> httpResponseStub_java_QA = new HttpResponseStub_java_QA();

        //given(this.httpRequestService.executeRequestAndGetResponse("")).willReturn(responseStub);
        String url1 = "https://api.hh.ru/vacancies?area=1&search_field=name&text=java+test&per_page=100&page=0";
        Mockito.when(this.httpRequestService.executeRequestAndGetResponse(url1)).thenReturn(httpResponseStub_java_test);

        String url2 = "https://api.hh.ru/vacancies?area=1&search_field=name&text=java+QA&per_page=100&page=0";
        Mockito.when(this.httpRequestService.executeRequestAndGetResponse(url2)).thenReturn(httpResponseStub_java_QA);

        //given(this.appConfig.getDelay()).willReturn(0);
        Mockito.when(this.appConfig.getDelay()).thenReturn(0);
        Mockito.when(this.appConfig.getVacanciesPerPage()).thenReturn(100);
        Mockito.when(this.appConfig.isUsePostgres()).thenReturn(true);


        ProjectService prjs = applicationContext.getBean(ProjectService.class);
        //prjs.processOneProject(project, currentDate);

        Method method = ProjectService.class.getDeclaredMethod("processOneProject", Project.class, Date.class);
        method.setAccessible(true);
        //assertEquals("SKB", method.invoke(tradesParser, getTradeJsonNode()));
        method.invoke(prjs, project, currentDate);

    }


    static class HttpResponseStub_java_test implements HttpResponse<String>{





        // responce for the url:
        //https://api.hh.ru/vacancies?area=1&search_field=name&text=java+test&per_page=100&page=0
        @Override
        public int statusCode() {
            return 200;
        }
        @Override
        public String body() {
            String json;
            Path path = Paths.get("./src/test/resources/java_test.json");
            try {
                json = new String(Files.readAllBytes(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return json;
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

    static class HttpResponseStub_java_QA implements HttpResponse<String>{

        // responce for the url:
        //https://api.hh.ru/vacancies?area=1&search_field=name&text=java+QA&per_page=100&page=0
        @Override
        public int statusCode() {
            return 200;
        }
        @Override
        public String body() {
            String json;
            Path path = Paths.get("./src/test/resources/java_QA.json");
            try {
                json = new String(Files.readAllBytes(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return json;
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
