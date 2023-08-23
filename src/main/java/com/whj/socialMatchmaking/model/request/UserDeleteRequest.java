
package com.whj.socialMatchmaking.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDeleteRequest implements Serializable {

    private static final long serialVersionUID = -7682187501543930543L;
    private long id;
}
