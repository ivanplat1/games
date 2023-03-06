package com.ivpl.games.services.broadcasting;

import com.ivpl.games.entity.CellKey;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class BroadcasterService {

    private final Map<Long, Broadcaster> broadcasters = new HashMap<>();

    public Registration registerBroadcasterListener(Long gameId, Consumer<Pair<Integer, CellKey>> listener) {
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
