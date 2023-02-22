package com.ivpl.games.utils;

import com.ivpl.games.entity.Checker;
import com.ivpl.games.entity.Figure;
import com.ivpl.games.entity.Queen;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DirectionsForClassRepo {

    static Map<Class<?>, Map<String, List<int[]>>> repository  = new HashMap<>();

    public DirectionsForClassRepo() {
        repository.put(Checker.class, calculateDirections(1));
        repository.put(Queen.class, calculateDirections(7));
    }

    public static Map<String, List<int[]>> getDirectionsForClass(Class<? extends Figure> clazz) {
        return repository.get(clazz);
    }

    public static List<int[]> getCertainDirectionForClass(Class<? extends Figure> clazz, String key) {
        return Optional.ofNullable(getDirectionsForClass(clazz)).map(ds -> ds.get(key))
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("Directions are not implemented for Figure child class %s", clazz.getName())));
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
