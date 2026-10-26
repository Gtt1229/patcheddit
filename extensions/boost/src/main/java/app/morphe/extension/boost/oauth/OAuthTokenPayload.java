/*
 * Copyright 2026 wchill.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.extension.boost.oauth;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

public final class OAuthTokenPayload {
    public static final int MAX_CHARACTERS = 65536;

    public final String accessToken;
    public final String refreshToken;
    public final String username;
    public final String clientId;

    public OAuthTokenPayload(String accessToken, String refreshToken, String username, String clientId)
            throws OAuthTokenException {
        this.accessToken = token(accessToken);
        this.refreshToken = token(refreshToken);
        if (username != null && !username.matches("[A-Za-z0-9_-]{1,32}")) {
            throw new OAuthTokenException("The account name in the token JSON is invalid.");
        }
        if (clientId != null && !clientId.matches("[A-Za-z0-9_-]{1,256}")) {
            throw new OAuthTokenException("The OAuth client ID in the token JSON is invalid.");
        }
        this.username = username;
        this.clientId = clientId;
    }

    static String token(String value) throws OAuthTokenException {
        if (value == null || value.isEmpty() || value.length() > 16384) {
            throw new OAuthTokenException("Both access_token and refresh_token must contain a token.");
        }
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) <= 0x20 || value.charAt(i) >= 0x7f) {
                throw new OAuthTokenException("Tokens cannot contain whitespace or non-ASCII characters.");
            }
        }
        return value;
    }

    public static String read(Reader reader) throws IOException, OAuthTokenException {
        StringBuilder text = new StringBuilder();
        char[] buffer = new char[4096];
        int count;
        while ((count = reader.read(buffer)) != -1) {
            if (text.length() + count > MAX_CHARACTERS) {
                throw new OAuthTokenException("Token JSON must not exceed 64 KiB of text.");
            }
            text.append(buffer, 0, count);
        }
        return text.toString();
    }

    public static OAuthTokenPayload parse(String text) throws OAuthTokenException {
        if (text == null || text.length() > MAX_CHARACTERS) {
            throw new OAuthTokenException("Token JSON is missing or exceeds 64 KiB of text.");
        }
        String access = null;
        String refresh = null;
        String username = null;
        String client = null;
        Set<String> fields = new HashSet<>();
        try (JsonReader reader = new JsonReader(new StringReader(text))) {
            reader.setLenient(false);
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (!fields.add(name)) {
                    throw new OAuthTokenException("Token JSON contains a duplicate field.");
                }
                switch (name) {
                    case "version":
                        if (reader.peek() != JsonToken.NUMBER || !"1".equals(reader.nextString())) {
                            throw new OAuthTokenException("This token export version is not supported.");
                        }
                        break;
                    case "access_token":
                        access = string(reader);
                        break;
                    case "refresh_token":
                        refresh = string(reader);
                        break;
                    case "username":
                        username = string(reader);
                        break;
                    case "client_id":
                        client = string(reader);
                        break;
                    default:
                        throw new OAuthTokenException(
                                "Unsupported token JSON field. Use access_token, refresh_token, "
                                        + "and optionally username, client_id and version."
                        );
                }
            }
            reader.endObject();
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new OAuthTokenException("Token JSON contains trailing data.");
            }
        } catch (EOFException | IllegalStateException error) {
            throw new OAuthTokenException("Token JSON is incomplete or has an invalid field type.");
        } catch (IOException error) {
            throw new OAuthTokenException("Token JSON is not valid JSON.");
        }
        return new OAuthTokenPayload(access, refresh, username, client);
    }

    private static String string(JsonReader reader) throws IOException, OAuthTokenException {
        if (reader.peek() != JsonToken.STRING) {
            throw new OAuthTokenException("Token JSON fields must contain strings, not null or other types.");
        }
        return reader.nextString();
    }

    public String toJson() throws IOException {
        StringWriter output = new StringWriter();
        try (JsonWriter writer = new JsonWriter(output)) {
            writer.setIndent("  ");
            writer.beginObject().name("version").value(1);
            if (username != null) writer.name("username").value(username);
            if (clientId != null) writer.name("client_id").value(clientId);
            writer.name("access_token").value(accessToken);
            writer.name("refresh_token").value(refreshToken);
            writer.endObject();
        }
        return output.toString();
    }
}
