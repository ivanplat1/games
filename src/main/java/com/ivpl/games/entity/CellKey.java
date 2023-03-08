package com.ivpl.games.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.Range;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Array;

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

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    public boolean inRange(int from, int to) {
        return Range.between(from, to).contains(x) && Range.between(from, to).contains(y);
    }

    public int[] getAsArray() {
        return new int[] {x, y};
    }
}
