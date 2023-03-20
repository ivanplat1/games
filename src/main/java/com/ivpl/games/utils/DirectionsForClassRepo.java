package com.ivpl.games.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivpl.games.constants.PieceType;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;

import static com.ivpl.games.constants.ExceptionMessages.DIRECTIONS_ARE_NOT_IMPLEMENTED;

@Repository
public class DirectionsForClassRepo {

    private final ObjectMapper objectMapper = new ObjectMapper();
    static final HashMap<String, HashMap<String, LinkedList<int[]>>> repository = new HashMap<>();

    public DirectionsForClassRepo() throws IOException {
        repository.putAll(objectMapper.readValue(
                getClass().getResourceAsStream("/static/pieceDirections.json"),
                new TypeReference<>(){}));
    }

    public static Map<String, LinkedList<int[]>> getDirectionsForType(PieceType type) {
        return repository.get(type.name());
    }

    public static List<int[]> getCertainDirectionForClass(PieceType type, String key) {
        return Optional.ofNullable(getDirectionsForType(type)).map(ds -> ds.get(key))
                .orElseThrow(() -> new NoSuchElementException(
                        String.format(DIRECTIONS_ARE_NOT_IMPLEMENTED, type.name())));
    }
}
