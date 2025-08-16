package org.xiaoxu.web_boot.service.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xiaoxu.web_boot.entity.es.User;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final ElasticsearchClient elasticsearchClient;



    // 新增/更新文档
    public void searchDoc(User userEs) throws IOException {
        IndexResponse response = elasticsearchClient.index(i -> i.index("users").id(String.valueOf(userEs.getId())).document(userEs));
        log.info("response:{}", response);
    }

    // 查询文档 : 通过姓名去查询
    public void searchUser(String name) throws IOException{
        SearchResponse<User> response = elasticsearchClient.search(
                s -> s.index("users")
                        .query(q -> q.match(m -> m.field("name").query(name))), User.class
        );
        List<Hit<User>> hits = response.hits().hits();
        for (Hit<User> hit : hits) {
          log.info("hit:{}", hit.source());
        }
    }
}
