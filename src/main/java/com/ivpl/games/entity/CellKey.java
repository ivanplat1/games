package com.ivpl.games.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

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

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
