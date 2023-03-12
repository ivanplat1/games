package com.ivpl.games.entity.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.Range;

import java.io.Serial;
import java.io.Serializable;

@Getter
@AllArgsConstructor
public class CellKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 2405172041950251807L;

    private final int x;
    private final int y;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return x == ((CellKey) obj).getX() && y == ((CellKey) obj).getY();
    }

    public CellKey(Integer[] arr) {
        x = arr[0];
        y = arr[1];
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    public boolean inRange(int from, int to) {
        return Range.between(from, to).contains(x) && Range.between(from, to).contains(y);
    }

    public Integer[] getAsArray() {
        return new Integer[] {x, y};
    }
}
