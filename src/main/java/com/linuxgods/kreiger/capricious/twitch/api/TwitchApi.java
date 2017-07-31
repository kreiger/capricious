package com.linuxgods.kreiger.capricious.twitch.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TwitchApi {
    private static final boolean DEBUG = false;
    private final String clientId;
    private final ObjectMapper objectMapper;

    public TwitchApi(String clientId) {
        this.clientId = clientId;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        PropertyNamingStrategy propertyNamingStrategy = new PropertyNamingStrategy.SnakeCaseStrategy() {
            @Override
            public String translate(String input) {
                return "id".equals(input) ? "_id" : super.translate(input);
            }
        };
        objectMapper.setPropertyNamingStrategy(propertyNamingStrategy);
    }


    public List<Stream> getLiveStreams() {
        return get("https://api.twitch.tv/kraken/streams/?limit=99", Streams.class).getStreams();
    }

    public Optional<Channel> getChannelByName(String name) {
        return getUsersByLogin(name).stream()
                .filter(user -> name.equalsIgnoreCase(user.getName()))
                .map(user -> get("https://api.twitch.tv/kraken/channels/"+user.getId(), Channel.class))
                .findFirst();
    }

    public List<User> getUsersByLogin(String login) {
        return get("https://api.twitch.tv/kraken/users?login=" + login, Users.class).getUsers();
    }

    private <T> T get(String url, Class<T> returnType) {
        try {
            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
            urlConnection.setRequestProperty("Client-ID", clientId);
            InputStream inputStream = urlConnection.getInputStream();
            if (DEBUG) {
                String json = new Scanner(inputStream).useDelimiter("\\A").next();
                System.out.println(json);
                return objectMapper.readValue(json, returnType);
            }
            return objectMapper.readValue(inputStream, returnType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class DebugPropertyNamingStrategy extends PropertyNamingStrategy {

        private final PropertyNamingStrategy wrapped;

        private DebugPropertyNamingStrategy(PropertyNamingStrategy wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
            String snakeCase = wrapped.nameForField(config, field, defaultName);
            System.out.println("field: "+defaultName+" -> "+snakeCase);
            return snakeCase;
        }

        @Override
        public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            String snakeCase = wrapped.nameForGetterMethod(config, method, defaultName);
            System.out.println("getter: "+defaultName+" -> "+snakeCase);
            return snakeCase;
        }

        @Override
        public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            String snakeCase = wrapped.nameForSetterMethod(config, method, defaultName);
            System.out.println("setter: "+defaultName+" -> "+snakeCase);
            return snakeCase;
        }

        @Override
        public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam, String defaultName) {
            String snakeCase = wrapped.nameForConstructorParameter(config, ctorParam, defaultName);
            System.out.println("constructor: "+defaultName+" -> "+snakeCase);
            return snakeCase;
        }
    }
}
