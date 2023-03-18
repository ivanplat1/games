package com.ivpl.games.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivpl.games.entity.ui.checkers.CheckerView;
import com.ivpl.games.entity.ui.AbstractPieceView;
import com.ivpl.games.entity.ui.checkers.CheckerQueenView;
import com.ivpl.games.entity.ui.chess.*;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;

import static com.ivpl.games.constants.ExceptionMessages.DIRECTIONS_ARE_NOT_IMPLEMENTED;

@Repository
public class DirectionsForClassRepo {

    private final ObjectMapper objectMapper = new ObjectMapper();

    static Map<Class<?>, Map<String, List<int[]>>> repository  = new HashMap<>();

    public DirectionsForClassRepo() throws IOException {
        repository.put(CheckerView.class, calculateDirections(1));
        repository.put(CheckerQueenView.class, calculateDirections(7));
        repository.put(PawnView.class, calculateDirections(1));
        repository.put(RookView.class, calculateDirections(7));
        repository.put(HorseView.class, calculateDirections(3));
        repository.put(BishopView.class, calculateDirections(7));
        repository.put(QueenView.class, calculateDirections(7));
        repository.put(KingView.class, calculateDirections(1));

        loadDirections();
    }

    public static Map<String, List<int[]>> getDirectionsForClass(Class<? extends AbstractPieceView> clazz) {
        return repository.get(clazz);
    }

    public static List<int[]> getCertainDirectionForClass(Class<? extends AbstractPieceView> clazz, String key) {
        return Optional.ofNullable(getDirectionsForClass(clazz)).map(ds -> ds.get(key))
                .orElseThrow(() -> new NoSuchElementException(
                        String.format(DIRECTIONS_ARE_NOT_IMPLEMENTED, clazz.getName())));
    }

    private Map<String, List<int[]>> calculateDirections(int range) {
        Map<String, List<int[]>> directions = new LinkedHashMap<>();
        for (int i = 1; i < 5; i++) {
            List<int[]> direction = new LinkedList<>();
            for (int j = 1; j < range+1; j++) {
                switch (i) {
                    case 1:
                        direction.add(new int[]{j, j});
                        break;
                    case 2:
                        direction.add(new int[]{-j, -j});
                        break;
                    case 3:
                        direction.add(new int[]{-j, j});
                        break;
                    case 4:
                        direction.add(new int[]{j, -j});
                        break;
                    default:
                        break;
                }
            }
            directions.put(Arrays.toString(direction.get(0)), direction);
        }
        return directions;
    }

    private Map<?, ?> loadDirections() throws IOException {
        Map<String, Integer[]> map = objectMapper.readValue(getClass().getResourceAsStream("/static/pawnDirections.json"), HashMap.class);
        return map;
    }
}
