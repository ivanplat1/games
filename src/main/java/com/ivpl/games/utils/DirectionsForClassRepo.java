package com.ivpl.games.utils;

import com.ivpl.games.entity.ui.CheckerView;
import com.ivpl.games.entity.ui.PieceView;
import com.ivpl.games.entity.ui.CheckerQueenView;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DirectionsForClassRepo {

    static Map<Class<?>, Map<String, List<int[]>>> repository  = new HashMap<>();

    public DirectionsForClassRepo() {
        repository.put(CheckerView.class, calculateDirections(1));
        repository.put(CheckerQueenView.class, calculateDirections(7));
    }

    public static Map<String, List<int[]>> getDirectionsForClass(Class<? extends PieceView> clazz) {
        return repository.get(clazz);
    }

    public static List<int[]> getCertainDirectionForClass(Class<? extends PieceView> clazz, String key) {
        return Optional.ofNullable(getDirectionsForClass(clazz)).map(ds -> ds.get(key))
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("Directions are not implemented for Item child class %s", clazz.getName())));
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
}
