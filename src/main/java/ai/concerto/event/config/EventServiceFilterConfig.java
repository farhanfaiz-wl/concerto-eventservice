package ai.concerto.event.config;

import ai.concerto.event.auth.filter.EventServiceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventServiceFilterConfig {

    @Bean
    public FilterRegistrationBean<EventServiceFilter> registrationBean(){
        final FilterRegistrationBean<EventServiceFilter> registrationBean = new FilterRegistrationBean<>();
        final EventServiceFilter log4jMDCFilter = new EventServiceFilter();
        registrationBean.setFilter(log4jMDCFilter);
        registrationBean.setOrder(2);
        return registrationBean;
    }
}
