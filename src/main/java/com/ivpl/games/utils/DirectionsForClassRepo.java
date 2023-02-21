package com.ivpl.games.utils;

import com.ivpl.games.entity.Checker;
import com.ivpl.games.entity.Figure;
import com.ivpl.games.entity.Queen;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository
public class DirectionsForClassRepo {

    static Map<Class<?>, List<List<int[]>>> repository  = new HashMap<>();

    public DirectionsForClassRepo() {
        repository.put(Checker.class, calculateDirections(1));
        repository.put(Queen.class, calculateDirections(7));
    }

    public static List<List<int[]>> getDirectionsForClass(Class<? extends Figure> clazz) {
        return repository.get(clazz);
    }

    private List<List<int[]>> calculateDirections(int range) {
        List<List<int[]>> directions = new LinkedList<>();
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
            directions.add(direction);
        }
        return directions;
    }
}
