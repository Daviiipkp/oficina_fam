package org.daviipkp.oficina_fam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.daviipkp.oficina_fam.Main.CheckInRequest;
import org.daviipkp.oficina_fam.Main.IntChallenge;
import org.daviipkp.oficina_fam.Main.TomateDesafio;

import io.javalin.Javalin;
import io.javalin.http.sse.SseClient;

public class Main {

    private static final List<String> checkins = new ArrayList<>();

    private static final ConcurrentLinkedQueue<SseClient> sseClients = new ConcurrentLinkedQueue<>();
    
    //
    //
    //
    static String melancia_winner = null;

    static List<SseClient> melanciaClients = new CopyOnWriteArrayList<>();


    static List<String> tomate_winners = new CopyOnWriteArrayList<>();
    static Map<String, Integer> tomate_desafios = new ConcurrentHashMap<>();
    static List<SseClient> tomateClients = new CopyOnWriteArrayList<>();

    public static class TomateDesafio {
        public String id;
        public int numero;
        
        public TomateDesafio(String id, int numero) {
            this.id = id;
            this.numero = numero;
        }
    }

    public static class TomateSubmissao {
        public String nome;
        public String id;
        public int resposta;
    }

    //
    //
    //

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
                client.sendEvent("connected", "Conexão estabelecida"); 
                if(abobora_winner != null) {
                    client.sendEvent("winner", abobora_winner);
                }
                sseClients.add(client);
                client.onClose(() -> {
                    sseClients.remove(client);
                });
            });

//
//
//
//
//
            config.routes.post("/melancia", ctx -> {
                if (melancia_winner != null) {
                    ctx.status(400).result("O desafio já tem um vencedor!");
                    return;
                }

                IntChallenge sub = ctx.bodyAsClass(IntChallenge.class);
                
                if ("1024".equals(sub.resposta)) {
                    melancia_winner = sub.nome;
                    melanciaClients.forEach(client -> client.sendEvent("winner", melancia_winner));
                    ctx.result("Correto! Você venceu o Desafio Melancia.");
                } else {
                    ctx.status(400).result("Resposta incorreta.");
                }
            });

            config.routes.sse("/melanciaevents", client -> {
                client.keepAlive();
                client.sendEvent("connected", "Conexão estabelecida");
                
                if (melancia_winner != null) {
                    client.sendEvent("winner", melancia_winner);
                }
                
                melanciaClients.add(client);
                client.onClose(() -> melanciaClients.remove(client));
            });

            config.routes.sse("/tomateevents", client -> {
                client.keepAlive();
                client.sendEvent("connected", "Conexão estabelecida");
                
                for (String winner : tomate_winners) {
                    client.sendEvent("winner", winner);
                }
                
                tomateClients.add(client);
                client.onClose(() -> tomateClients.remove(client));
            });

            config.routes.get("/tomate", ctx -> {
                String id = UUID.randomUUID().toString();
                int numero = new Random().nextInt(50) + 1;
                
                tomate_desafios.put(id, numero);
                
                ctx.json(new TomateDesafio(id, numero));
            });

            config.routes.post("/tomate", ctx -> {
                TomateSubmissao sub = ctx.bodyAsClass(TomateSubmissao.class);
                
                if (sub.id != null && tomate_desafios.containsKey(sub.id)) {
                    int numeroOriginal = tomate_desafios.get(sub.id);
                    int respostaEsperada = numeroOriginal * numeroOriginal;
                    
                    if (sub.resposta == respostaEsperada) {
                        if (!tomate_winners.contains(sub.nome)) {
                            tomate_winners.add(sub.nome);
                            tomateClients.forEach(client -> client.sendEvent("winner", sub.nome));
                        }
                        ctx.result("Correto! Você venceu o Desafio Tomate.");
                        tomate_desafios.remove(sub.id); 
                    } else {
                        ctx.status(400).result("Resposta incorreta.");
                    }
                } else {
                    ctx.status(400).result("ID inválido ou desafio expirado.");
                }
            });
            
            //
            //
            //
            //
            //
            //
            //
            //
            //
        }).start(7070);

    

    }

    public record CheckInRequest(String nome) {}

    public record IntChallenge(String nome, int resposta) {}

    public record StringChallenge(String nome, String resposta) {}

}
