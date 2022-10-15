package com.config;
import com.interceptors.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class AdminWebConfig implements WebMvcConfigurer {
    //解决跨域问题
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 匹配所有的路径
            .allowCredentials(true) // 设置允许凭证
            .allowedHeaders("*")   // 设置请求头
            .allowedMethods("GET","POST","PUT","DELETE") // 设置允许的方式
            .allowedOriginPatterns("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] excludePath = {"/","/login/**","/wallpaper/wall_list/**","/category/getAll","/css/**",
                "/js/**","/layer/**","/laydate/**","/feedback/add",
                "/images/**","/fonts/**","/layui/**","/profileCategory/getAll","/profile/profile_list/**"};
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(excludePath);
    }
}
