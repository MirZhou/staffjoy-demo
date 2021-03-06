package cn.eros.staffjoy.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author 周光兵
 * @date 2021/7/23 13:57
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("cn.eros.staffjoy.account.controller"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(this.apiEndPointsInfo())
            .useDefaultResponseMessages(false);
    }

    public ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title("Account REST API")
            .description("Staffjoy Account REST API")
            .contact(new Contact("Eros", "https://github.com/MirZhou/staffjoy-demo", "mir3732@live.com"))
            .license("The MIT License")
            .license("https://opensource.org/licenses/MIT")
            .version("v1")
            .build();
    }
}
