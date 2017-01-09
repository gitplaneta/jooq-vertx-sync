package eu.busz.jooq.vertx.sync;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.jdbc.FiberDataSource;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.postgresql.ds.PGPoolingDataSource;

import javax.sql.DataSource;

public class JooqVerticle extends SyncVerticle {

    private final JooqExample jooqExample;

    public JooqVerticle() {
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("xxxxxxxxxxxx");
        source.setServerName("localhost");
        source.setDatabaseName("postgres");
        source.setUser("postgres");
        source.setPassword("postgres");
        source.setMaxConnections(10);
        DataSource wrappedDataSource = FiberDataSource.wrap(source);

        jooqExample = new JooqExample(wrappedDataSource);
    }

    public JooqVerticle(DataSource ds) {
        jooqExample = new JooqExample(ds);
    }

    @Override
    @Suspendable
    public void start() throws Exception {
        System.out.println("Starting http server...");
        startHttpServer();
    }

    @Suspendable
    private void startHttpServer() {
        Router router = Router.router(vertx);
        router.route("/*").handler(BodyHandler.create());

        router.get("/jooq").handler(Sync.fiberHandler(rc -> {
            jooqExample.jooqSelect();
            rc.response().end();
        }));

        router.get("/jooq-non-fiber").handler(rc -> {
            vertx.executeBlocking(f -> {
                jooqExample.jooqSelectNonFiber();
                f.complete(null);
            }, false, ar -> {
                if (ar.succeeded()) {
                    rc.response().end();
                } else {
                    rc.fail(ar.cause());
                }
            });
        });

        router.get("/no-jooq").handler(Sync.fiberHandler(rc -> {
            jooqExample.queryWithoutJooq();
            rc.response().end();
        }));

        router.get("/no-jooq-non-fiber").handler(rc -> {
            jooqExample.queryWithoutJooqNonFiber();
            rc.response().end();
        });

        router.get("/health-check").handler(rc -> rc.response().end("OK"));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(9999, "127.0.0.1", ar -> {
                    if (ar.succeeded()) {
                        System.out.println("deployed!!!");
                    } else {
                        ar.cause().printStackTrace();
                    }
                });
    }
}