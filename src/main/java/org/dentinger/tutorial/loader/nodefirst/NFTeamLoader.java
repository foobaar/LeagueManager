package org.dentinger.tutorial.loader.nodefirst;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.dentinger.tutorial.dal.SportsBallRepository;
import org.dentinger.tutorial.domain.Team;
import org.dentinger.tutorial.util.AggregateExceptionLogger;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class NFTeamLoader {
  private static Logger logger = LoggerFactory.getLogger(NFTeamLoader.class);
  private SessionFactory sessionFactory;
  private SportsBallRepository repo;
  private TeamWorker submitableWorker;
  private int numThreads;
  private final AtomicLong recordsWritten = new AtomicLong(0);
  private ThreadPoolTaskExecutor poolTaskExecutor;

  private String MERGE_TEAM_NODES =
      "unwind {json} as team "
          + "merge (t:Team {id: team.id}) "
          + "on create set t.name = team.name ";

  private String MERGE_TEAM_RELATIONSHIPS =
      " UNWIND {json} AS team "
          + "unwind team.leagues as league "
          + "match (l:League {id: league.id}) "
          + "match (t:Team {id: team.id}) "
          + "merge(t)-[:MEMBERSHIP]-(l) ";

  private String CLEAN_UP =
      "match (t:Team) detach delete t";

  @Autowired
  public NFTeamLoader(ThreadPoolTaskExecutor teamProcessorThreadPool,
                      SessionFactory sessionFactory,
                      SportsBallRepository repo,
                      Environment env, TeamWorker submitableWorker) {
    this.sessionFactory = sessionFactory;
    this.repo = repo;
    this.submitableWorker = submitableWorker;
    this.numThreads = Integer.valueOf(env.getProperty("teams.loading.threads", "1"));
    this.poolTaskExecutor = teamProcessorThreadPool;
  }

  public void loadTeamNodes() {
    AggregateExceptionLogger aeLogger = AggregateExceptionLogger.getLogger(this.getClass());

    List<Team> teamList = repo.getTeams();
    logger.info("About to load {} Teams using {} threads", teamList.size(), numThreads);
    recordsWritten.set(0);

    int subListSize = (int) Math.floor(teamList.size() / numThreads);
    long start = System.currentTimeMillis();
    Lists.partition(teamList, subListSize).stream().parallel()
        .forEach((teams) -> {
          submitableWorker.doSubmitableWork(aeLogger, teams, MERGE_TEAM_NODES);

        });
    monitorThreadPool();
    logger.info("Processing of {} Teams using {} threads complete: {}ms", recordsWritten.get(),
        numThreads,
        System.currentTimeMillis() - start);
  }

  public void loadTeamRelationships() {
    AggregateExceptionLogger aeLogger = AggregateExceptionLogger.getLogger(this.getClass());
    List<Team> teamList = repo.getTeams();
    logger.info("About to load Team relationships using {} threads",numThreads);
    recordsWritten.set(0);

    int subListSize = (int) Math.floor(teamList.size() / numThreads);
    long start = System.currentTimeMillis();

    Lists.partition(teamList, subListSize).stream().parallel()
        .forEach((teamsSubList) -> {

          submitableWorker.doSubmitableWork(aeLogger, teamsSubList, MERGE_TEAM_RELATIONSHIPS);

        });
    monitorThreadPool();
    logger
        .info("Processing of {} Team relationships using {} threads complete: {}ms",
            recordsWritten.get(), numThreads,
            System.currentTimeMillis() - start);
  }

  private void monitorThreadPool() {
    while (poolTaskExecutor.getPoolSize() > 0) {
      logger.info("Currently running threads: {}, jobs still in pool {}",
          poolTaskExecutor.getActiveCount(),
          poolTaskExecutor.getPoolSize());
      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }



}
