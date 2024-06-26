package com.arraywork.puffin.enums;

import com.arraywork.puffin.metafield.MetafieldEnum;
import com.arraywork.springforce.databind.GenericEnum;
import com.arraywork.springforce.databind.GenericEnumConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分级枚举
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/04/21
 */
@AllArgsConstructor
@Getter
public enum Rating implements MetafieldEnum, GenericEnum<Integer> {

    UNIVERSAL(0, "普适级"),
    GUIDANCE(1, "指导级"),
    RESTRICTED(2, "限制级"),
    ADULT(3, "成人级");

    private final Integer code;
    private final String label;

    public static class Converter extends GenericEnumConverter<Rating, Integer> {}

}