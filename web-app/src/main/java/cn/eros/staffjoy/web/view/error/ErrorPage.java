package cn.eros.staffjoy.web.view.error;

import lombok.Builder;
import lombok.Data;

/**
 * @author 周光兵
 * @date 2021/7/21 21:31
 */
@Data
@Builder
public class ErrorPage {
    /**
     * Used in <title> and <h1>
     */
    private String title;
    /**
     * Tell the user what's wrong
     */
    private String explanation;
    /**
     * http status code
     */
    private int headerCode;
    /**
     * Where do you want user to go
     */
    private String linkText;
    /**
     * what's the link
     */
    private String linkHref;
    /**
     * What do we track the view as on the backend
     */
    private String sentryErrorId;
    /**
     * Config for app
     */
    private String sentryPublicDsn;
    private String imageBase64;
}
