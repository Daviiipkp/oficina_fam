package org.daviipkp.oficina_fam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.daviipkp.oficina_fam.Main.CheckInRequest;
import org.daviipkp.oficina_fam.Main.IntChallenge;

import io.javalin.Javalin;
import io.javalin.http.sse.SseClient;

public class Main {

    private static final List<String> checkins = new ArrayList<>();

    private static final ConcurrentLinkedQueue<SseClient> sseClients = new ConcurrentLinkedQueue<>();

    private static String abobora_winner;

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
                checkins.clear();
                abobora_winner = null;
            });
            config.routes.post("/checkin", ctx -> {
                CheckInRequest req = ctx.bodyAsClass(CheckInRequest.class);
                checkins.add(req.nome());
                sseClients.forEach((client) -> {
                    client.sendEvent("users", checkins);
                });
                ctx.result("opa! check-in recebido :D");
            });

            config.routes.post("/abobora", ctx -> {
                IntChallenge req = ctx.bodyAsClass(IntChallenge.class);
                if((req.resposta() == 13 || req.resposta() == 13.0) && (abobora_winner == null)) {
                    ctx.result("boa! desafio concluído!");
                    abobora_winner = req.nome();
                    for(SseClient c : sseClients) {
                        if(c.ctx().path().contains("abobora")) {
                            c.sendEvent("winner", req.nome());
                        }
                    }
                }else{
                    if(abobora_winner != null) {
                        return;
                    }
                    ctx.result("sua resposta \"" + req.resposta() + "\" está incorreta.");
                }
            });
            
            config.routes.sse("/checkinevents", client -> {

                client.keepAlive(); 

                client.sendEvent("users", checkins);

                sseClients.add(client);
                client.onClose(() -> {
                    sseClients.remove(client);
                });
            });

            config.routes.sse("/aboboraevents", client -> {

                client.keepAlive(); 
                if(abobora_winner != null) {
                    client.sendEvent("winner", abobora_winner);
                }
                sseClients.add(client);
                client.onClose(() -> {
                    sseClients.remove(client);
                });
            });



            
        }).start(7070);
    }

    public record CheckInRequest(String nome) {}

    public record IntChallenge(String nome, int resposta) {}

}
