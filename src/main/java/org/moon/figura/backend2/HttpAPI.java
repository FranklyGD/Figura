package org.moon.figura.backend2;

import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiConsumer;

public class HttpAPI {


    // -- builders -- //


    private static URI getUri(String url) {
        return URI.create(getBackendAddress() + "/" + url);
    }

    private static String getBackendAddress() {
        ServerAddress backendIP = ServerAddress.parseString(Config.SERVER_IP.asString());
        return "https://" + backendIP.getHost() + "/api";
    }

    protected HttpRequest.Builder header(String url) {
        return HttpRequest
                .newBuilder(getUri(url))
                .header("user-agent", FiguraMod.MOD_NAME + "/" + FiguraMod.VERSION)
                .header("token", NetworkStuff.token);
    }


    // -- runners -- //


    public void runString(HttpRequest request, BiConsumer<Integer, String> consumer) {
        try {
            requestDebug(request);
            HttpResponse<String> response = NetworkStuff.client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            consumer.accept(response.statusCode(), response.body());
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    public void runStream(HttpRequest request, BiConsumer<Integer, InputStream> consumer) {
        try {
            requestDebug(request);
            HttpResponse<InputStream> response = NetworkStuff.client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            consumer.accept(response.statusCode(), response.body());
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }


    // -- feedback -- //


    private static void requestDebug(HttpRequest msg) {
        FiguraMod.debug( "Sent Http request:\n\t" + msg.uri().toString() + "\n\t" + msg.headers().map().toString());
    }


    // -- accessors -- //


    // will return 200 OK if token is valid
    public HttpRequest checkAuth() {
        return header("").build();
    }

    public HttpRequest getUser(UUID id) {
        return header(id.toString()).build();
    }

    public HttpRequest getLimits() {
        return header("limits").build();
    }

    public HttpRequest getVersion() {
        return header("version").build();
    }

    public HttpRequest getMotd() {
        return header("motd").build();
    }

    public HttpRequest getAvatar(UUID owner, String id) {
        return header(owner.toString() + '/' + id).build();
    }

    public HttpRequest uploadAvatar(String id, byte[] bytes) {
        return header(id).PUT(HttpRequest.BodyPublishers.ofByteArray(bytes)).header("Content-Type", "application/octet-stream").build();
    }

    public HttpRequest deleteAvatar(String id) {
        return header(id).DELETE().build();
    }

    public HttpRequest setEquipped(String json) {
        return header("equip").POST(HttpRequest.BodyPublishers.ofString(json)).header("Content-Type", "application/json").build();
    }
}