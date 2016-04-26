package org.dentinger.tutorial;

import java.util.Arrays;
import java.util.List;
import org.dentinger.tutorial.loader.NodeIndexes;
import org.dentinger.tutorial.service.Neo4jLoaderService;
import org.dentinger.tutorial.service.NodeFirstNeo4jLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = "org.dentinger.tutorial.repository")
public class Neo4jDemoApplication {

  private final static Logger log = LoggerFactory.getLogger(Neo4jDemoApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(Neo4jDemoApplication.class, args).close();
  }

  @Bean CommandLineRunner runLoader(Neo4jLoaderService service,
                                    NodeFirstNeo4jLoaderService nodeFirstNeo4jLoaderService,
                                    NodeIndexes nodeIndexes) {
    nodeIndexes.createIndexes();
    return args -> {
      List<String> list = Arrays.asList(args);
      if (!list.contains("nodeFirst")) {
        service.runCombinedUnwindLoader(list);
      }
      if (list.contains("nodeFirst")) {
        nodeFirstNeo4jLoaderService.runLoader(list);
      }
      if (list.contains("cleanup")) {
        service.cleanup();
      }
    };
  }

}
