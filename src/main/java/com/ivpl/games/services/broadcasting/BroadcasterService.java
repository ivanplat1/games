package com.ivpl.games.services.broadcasting;

import com.vaadin.flow.shared.Registration;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class BroadcasterService {

    private final Map<Long, Broadcaster> broadcasters = new HashMap<>();

    public Registration registerBroadcasterListener(Long gameId, Consumer<Integer> listener) {
        Optional<Broadcaster> broadcaster = Optional.ofNullable(broadcasters.get(gameId));
        return broadcaster.map(b -> b.register(listener))
                .orElseGet(() -> {
                    Broadcaster b = new Broadcaster();
                    broadcasters.put(gameId, b);
                    return b.register(listener);
                });
    }

    public Broadcaster getBroadcaster(Long gameId) {
        return Optional.ofNullable(broadcasters.get(gameId))
                .orElseThrow(() ->
                        new RuntimeException(String
                                .format("Broadcasting is not registered for game %s", gameId)));
    }
}
