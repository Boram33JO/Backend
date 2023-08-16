package com.sparta.i_mu.repository.QueryDsl;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;


public class MySQLFunctions {
    public static NumberExpression<Double> stDistanceSphere(NumberPath<Double> longitude1, NumberPath<Double> latitude1, Double longitude2, Double latitude2) {
        return Expressions.numberTemplate(Double.class,
                "ST_Distance_Sphere(POINT({0}, {1}), POINT({2},{3}))",
                longitude1,latitude1,longitude2,latitude2);
    }
}
