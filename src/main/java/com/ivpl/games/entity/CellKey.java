package com.ivpl.games.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CellKey {

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
}
