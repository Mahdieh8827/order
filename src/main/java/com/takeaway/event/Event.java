package com.takeaway.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Event {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");
    String value;
}
