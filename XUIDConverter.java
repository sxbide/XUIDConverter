package sxbide;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XUIDConverter {

    Cache<String, UUID> NAME_TO_UNIQUE_XUID_CACHE = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofDays(5)).build();
    Cache<UUID, String> UNIQUE_XUID_TO_NAME_CACHE = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofDays(5)).build();

    HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public CompletableFuture<UUID> getUniqueXUIDAsync(@NonNull String userName) {
        return CompletableFuture.supplyAsync(() -> getUniqueXUID(userName));
    }

    public CompletableFuture<String> getUsernameAsync(@NonNull UUID xuid) {
        return CompletableFuture.supplyAsync(() -> getUsername(xuid));
    }

    public @Nullable UUID getUniqueXUID(@NotNull String userName) {
        userName = userName.toLowerCase(Locale.ROOT);
        UUID xuid = NAME_TO_UNIQUE_XUID_CACHE.getIfPresent(userName);
        if (xuid != null) {
            return xuid;
        }

        String xuidStr = fetchXUIDFromAPI(userName);
        if (xuidStr == null) {
            return null;
        }

        xuid = formatXUID(xuidStr);
        NAME_TO_UNIQUE_XUID_CACHE.put(userName, xuid);
        return xuid;
    }

    public @Nullable String getUsername(@NonNull UUID xuid) {
        String name = UNIQUE_XUID_TO_NAME_CACHE.getIfPresent(xuid);
        if (name != null) {
            return "." + name;
        }

        name = fetchUsernameFromAPI(xuid);
        if (name != null) {
            UNIQUE_XUID_TO_NAME_CACHE.put(xuid, name);
        }
        return "." + name;
    }

    public boolean isXUID(UUID uuid) {
        return uuid.toString().startsWith("00000000-0000-0000-");
    }

    private @Nullable String fetchXUIDFromAPI(@NotNull String userName) {
        try {
            String url = "https://api.geysermc.org/v2/xbox/xuid/" + userName;
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .setHeader("User-Agent", "XUIDConverter-1.0")
                    .build();

            String result = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
            JsonObject object = JsonParser.parseString(result).getAsJsonObject();
            JsonElement xuidElement = object.get("xuid");

            return (xuidElement != null && !(xuidElement instanceof JsonNull)) ? xuidElement.getAsString() : null;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    private @Nullable String fetchUsernameFromAPI(@NotNull UUID xuid) {
        try {
            String xuidHex = xuid.toString().replace("00000000-0000-0000-", "").replace("-", "");
            BigInteger decimalXUID = new BigInteger(xuidHex, 16);

            String url = "https://api.geysermc.org/v2/xbox/gamertag/" + decimalXUID;
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .setHeader("User-Agent", "XUIDConverter-1.0")
                    .build();

            String result = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
            JsonObject object = JsonParser.parseString(result).getAsJsonObject();
            JsonElement gamerTagElement = object.get("gamertag");

            return (gamerTagElement != null && !(gamerTagElement instanceof JsonNull)) ? gamerTagElement.getAsString() : null;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    private @NotNull UUID formatXUID(@NotNull String xuidStr) {
        BigInteger xuid = new BigInteger(xuidStr);
        String hex = String.format("%016X", xuid);
        return UUID.fromString("00000000-0000-0000-" + hex.substring(0, 4) + "-" + hex.substring(4));
    }
}
