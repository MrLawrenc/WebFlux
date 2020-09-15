package com.github.mrlawrenc.e_r2dbc.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * @author : MrLawrenc
 * date  2020/9/15 21:12
 */
@Data
public class User {
    @Id
    private Long id;

    private String userName;
    private String password;
    private String realName;
    private String img;
    private Integer isDel;
    /**
     * 时间输出web端序列化方式指定
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime delTime;

}