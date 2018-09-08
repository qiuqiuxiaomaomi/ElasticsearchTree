package config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.karakal.commons.uc.interceptor.AuthorizationInterceptor;
import com.karakal.commons.uc.resolvers.CurrentUserMethodArgumentResolver;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangmingquan on 2018/9/7.
 */
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index.html");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //registry.addInterceptor(new AuthorizationInterceptor()).addPathPatterns("/**/*.do").addPathPatterns("/**/*.json").addPathPatterns("/**/*.action");
        registry.addInterceptor(new AuthorizationInterceptor()).addPathPatterns("/**").excludePathPatterns("/swagger*/**").excludePathPatterns("/v2/**").excludePathPatterns("/webjars/**");
        super.addInterceptors(registry);
    }
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new CurrentUserMethodArgumentResolver());
        super.addArgumentResolvers(argumentResolvers);
    }


    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastJsonHttpMessageConverter.setSupportedMediaTypes(mediaTypes);
        //fastJsonHttpMessageConverter.getFastJsonConfig().setFeatures(Feature.DisableCircularReferenceDetect);//有了setSerializerFeatures，禁止循环应用失效
        fastJsonHttpMessageConverter.getFastJsonConfig().setSerializerFeatures(SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect);
        fastJsonHttpMessageConverter.getFastJsonConfig().setDateFormat("yyyy-MM-dd HH:mm:ss");//时间转换
        converters.add(fastJsonHttpMessageConverter);
        super.configureMessageConverters(converters);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder)
    {
        return restTemplateBuilder
                .setConnectTimeout(3000)
                .setReadTimeout(3000)
                .build();
    }
}
