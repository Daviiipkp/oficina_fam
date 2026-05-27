package org.daviipkp.oficina_fam;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.daviipkp.oficina_fam.Main.CheckInRequest;
import org.daviipkp.oficina_fam.Main.IntChallenge;

import io.javalin.Javalin;
import io.javalin.http.sse.SseClient;

public class Main {

    private static final Map<String, String> users = new ConcurrentHashMap<>();

    private static final ConcurrentLinkedQueue<SseClient> sseClients = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        System.out.println("Initializing!!!");
        System.out.println("Trying to initialize endpoints...");
        initializeServer();
        System.out.println("Done!");
    }

    public static void initializeServer() {
        Javalin j = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> rule.anyHost());
            });
            config.contextResolver.ip = ctx -> {
                String forwardedFor = ctx.header("X-Forwarded-For");
                return forwardedFor != null ? forwardedFor.split(",")[0] : ctx.req().getRemoteAddr();
            };
            config.routes.get("/clear", ctx -> {
                users.clear();
            });
            config.routes.post("/checkin", ctx -> {
                CheckInRequest req = ctx.bodyAsClass(CheckInRequest.class);
                users.put(ctx.ip(), req.nome());
                sseClients.forEach((client) -> {
                    client.sendEvent("users", users.values());
                });
                ctx.result("opa! check-in recebido :D");
            });

            config.routes.post("/galinha", ctx -> {
                IntChallenge req = ctx.bodyAsClass(IntChallenge.class);
                if(!users.keySet().contains(ctx.ip())) {
                    ctx.result("tu não fez check-in!");
                    return;
                }
                String user = users.get(ctx.ip());
                if(req.resposta() == 13) {
                    ctx.result("boa! desafio concluído!");
                }else{
                    ctx.result("sua resposta \"" + req.resposta() + "\" está incorreta.");
                }
            });
            
            config.routes.sse("/checkinevents", client -> {

                client.keepAlive(); 

                client.sendEvent("users", users.values());

                sseClients.add(client);
                client.onClose(() -> {
                    sseClients.remove(client);
                });
            
            });


            
        }).start(7070);
    }

    public record CheckInRequest(String nome) {}

    public record IntChallenge(String nick, int resposta) {}

}
