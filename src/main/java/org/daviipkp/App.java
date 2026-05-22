package org.daviipkp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.daviipkp.App.CheckInRequest;

import io.javalin.Javalin;

public class App {

    private static final Map<String, String> users = new ConcurrentHashMap<>();

    public static void main( String[] args ) {
        System.out.println("Initializing!");
        System.out.println("Trying to initialize endpoints...");
        initializeServer();
        System.out.println("Done!");
    }

    public static void initializeServer() {
        Javalin j = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> rule.anyHost());
            });
            config.contextResolver.ip = ctx -> {
                String forwardedFor = ctx.header("X-Forwarded-For");
                return forwardedFor != null ? forwardedFor.split(",")[0] : ctx.req().getRemoteAddr();
            };
            config.routes.post("/checkin", ctx -> {
                CheckInRequest req = ctx.bodyAsClass(CheckInRequest.class);
                if(users.keySet().contains(ctx.ip())) {
                    ctx.result("tu já fez checkin macho");
                    return;
                }
                users.put(ctx.ip(), req.nick);
                ctx.result("opa! check-in recebido :D");
            });
            
        }).start(7070);
    }

    class CheckInRequest {
        public String nick;
    }

}
