package com.arraywork.puffin.basedata;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分级
 * @author AiChen
 * @created 2024/04/21
 */
@AllArgsConstructor
@Getter
public enum Rating {

    UNIVERSAL("普适级"),
    GUIDANCE("指导级"),
    RESTRICTED("限制级"),
    ADULT("成人级");

    private final String label;

}