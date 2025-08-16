//package org.xiaoxu.web_boot.config;
//
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.json.jackson.JacksonJsonpMapper;
//import co.elastic.clients.transport.rest_client.RestClientTransport;
//import org.apache.http.HttpHost;
//import org.apache.http.auth.AuthScope;
//import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.elasticsearch.client.RestClient;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class EsConfig {
//
////    @Value("${elasticsearch.uris}")
////    private String esUrl;
////
////    @Value("${elasticsearch.username:}")
////    private String username;
////
////    @Value("${elasticsearch.password:}")
////    private String password;
//
//    @Bean
//    public ElasticsearchClient elasticsearchClient() {
//        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(AuthScope.ANY,
//                new UsernamePasswordCredentials("elastic", "es123456"));
//
//        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200))
//                .setHttpClientConfigCallback(httpClientBuilder ->
//                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
//                .build();
//
//        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
//
//        return new ElasticsearchClient(transport);
//    }
//
//
//
//}
