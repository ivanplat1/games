package com.ivpl.games.services.broadcasting;

import com.vaadin.flow.shared.Registration;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.IntConsumer;

public class Broadcaster {
    Executor executor = Executors.newSingleThreadExecutor();
    LinkedList<IntConsumer> listeners = new LinkedList<>();

    public synchronized Registration register(
            IntConsumer listener) {
        listeners.add(listener);

        return () -> {
            synchronized (Broadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    public synchronized void broadcast(Integer message) {
        for (IntConsumer listener : listeners) {
            executor.execute(() -> listener.accept(message));
        }
    }
}