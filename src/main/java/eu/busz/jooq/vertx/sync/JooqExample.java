package eu.busz.jooq.vertx.sync;

import co.paralleluniverse.fibers.Suspendable;
import lombok.SneakyThrows;
import lombok.ToString;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class JooqExample {

    private final Connection conn;
    private final DSLContext ctx;

    @SneakyThrows
    public JooqExample(DataSource dataSource) {
        this.conn = dataSource.getConnection();
        this.ctx = DSL.using(conn);
    }

    @ToString
    public static class Something {
        public final int id;
        public final String name;
        public static RecordMapper<Record, Something> mapper =
                r -> new Something(r.getValue(field("id", Integer.class)), r.getValue(field("name", String.class)));

        public Something(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Suspendable
    public void jooqSelect() {
        //ctx.insertInto(table("something"), field("id"), field("name")).values(0, "name").execute();
        System.out.println("Insertion was completed!");
        Something something = ctx.select(field("id"), field("name")).from(table("something"))
                .where(field("id", Integer.class).eq(0)).fetchOne().map(Something.mapper);
        System.out.println(something);
    }

    public void jooqSelectNonFiber() {
       // ctx.insertInto(table("something"), field("id"), field("name")).values(0, "name").execute();
        System.out.println("Insertion was completed!");
        Something something = ctx.select(field("id"), field("name")).from(table("something"))
                .where(field("id", Integer.class).eq(0)).fetchOne().map(Something.mapper);
        System.out.println(something);
    }

    @SneakyThrows
    @Suspendable
    public void queryWithoutJooq() {
        System.out.println("Executing query without jooq...");
        Statement stmt = conn.createStatement();
        //stmt.execute("INSERT INTO something VALUES (0, 'some_name')");
        System.out.println("Insertion was completed!");
        ResultSet rs = stmt.executeQuery("select * from something");
        while (rs.next()) {
            System.out.println(rs.getString(1) + ", " + rs.getString(2));
        }
    }

    @SneakyThrows
    public void queryWithoutJooqNonFiber() {
        System.out.println("Executing query without jooq...");
        Statement stmt = conn.createStatement();
        //stmt.execute("INSERT INTO something VALUES (0, 'some_name')");
        System.out.println("Insertion was completed!");
        ResultSet rs = stmt.executeQuery("select * from something");
        while (rs.next()) {
            System.out.println(rs.getString(1) + ", " + rs.getString(2));
        }
    }
}
