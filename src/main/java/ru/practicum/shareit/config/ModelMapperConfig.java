package ru.practicum.shareit.config;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    /*    Converter<Integer, User> userIdToUserConverter = context -> {
            try {
                return userService.getUserById(context.getSource());
            } catch (NoContentException e) {
                throw new RuntimeException(e);
            }
        }; */
    }
}
