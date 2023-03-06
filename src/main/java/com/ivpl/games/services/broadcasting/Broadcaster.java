package com.ivpl.games.services.broadcasting;

import com.ivpl.games.entity.CellKey;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Broadcaster {
    Executor executor = Executors.newSingleThreadExecutor();
    LinkedList<Consumer<Pair<Integer, CellKey>>> listeners = new LinkedList<>();

    public synchronized Registration register(
            Consumer<Pair<Integer, CellKey>> listener) {
        listeners.add(listener);

        return () -> {
            synchronized (Broadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    public synchronized void broadcast(Pair<Integer, CellKey> message) {
        for (Consumer<Pair<Integer, CellKey>> listener : listeners) {
            executor.execute(() -> listener.accept(message));
        }
    }
}