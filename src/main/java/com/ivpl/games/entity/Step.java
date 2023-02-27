package com.ivpl.games.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Step {

    @Id
    @GeneratedValue
    private Long id;
    private CellKey from;
    private CellKey to;
    private Long figureId;
}
