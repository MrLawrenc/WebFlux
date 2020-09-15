package com.github.mrlawrenc.e_r2dbc.entity;

import lombok.Data;

/**
 * @author : MrLawrenc
 * date  2020/9/15 23:16
 * 组合对象  user表的username和test表的role组合
 */
@Data
public class MultiObj {
    private String userName;
    private String role;

}