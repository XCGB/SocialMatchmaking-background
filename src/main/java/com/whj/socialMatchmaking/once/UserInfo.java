package com.whj.socialMatchmaking.once;

import lombok.Data;

import java.util.Objects;

/**
 * @author: Baldwin
 * @createTime: 2023-07-18 14:18
 * @description:
 */
@Data
public class UserInfo {
    private String name;
    private Integer gender;

    public UserInfo(String name, Integer gender) {
        this.name = name;
        this.gender = gender;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInfo)) return false;
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(name, userInfo.name) &&
                Objects.equals(gender, userInfo.gender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, gender);
    }
}
