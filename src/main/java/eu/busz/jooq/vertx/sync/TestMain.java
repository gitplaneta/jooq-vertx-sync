package eu.busz.jooq.vertx.sync;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.jdbc.FiberDataSource;
import io.vertx.core.Vertx;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.postgresql.ds.PGPoolingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.jooq.impl.DSL.field;

@Log4j2
public class TestMain {

    @SneakyThrows
    public static void main(String[] args) throws InterruptedException {
        log.info("MAIN");
        log.info("DEBUG");
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("xxxxxxxxxxxx");
        source.setServerName("localhost");
        source.setDatabaseName("postgres");
        source.setUser("postgres");
        source.setPassword("postgres");
        source.setMaxConnections(10);
        DataSource wrappedDataSource = FiberDataSource.wrap(source);

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new JooqVerticle(wrappedDataSource), ar -> {
            if (ar.succeeded()) {
                System.out.println("Verticle deployed!");
            } else {
                ar.cause().printStackTrace();
                System.out.println("FAILED");
            }
        });

        Thread.sleep(200000);
    }
}
