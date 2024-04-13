package ru.practicum.shareit.config;

import lombok.AllArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.shareit.item.comment.dto.ItemCommentResponseDto;
import ru.practicum.shareit.item.comment.model.ItemComment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@AllArgsConstructor
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        // mapper.getConfiguration().setSkipNullEnabled(true);
        mapper.addConverter(new Converter<User, Long>() {
            public Long convert(MappingContext<User, Long> context) {
                return context.getSource() == null ? null : context.getSource().getId();
            }
        });
        mapper.addConverter(new Converter<Item, Long>() {
            public Long convert(MappingContext<Item, Long> context) {
                return context.getSource() == null ? null : context.getSource().getId();
            }
        });

        mapper.addConverter(new Converter<LocalDateTime, String>() {
            public String convert(MappingContext<LocalDateTime, String> context) {
                DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                return context.getSource() == null ? null : dtFormatter.format(context.getSource());
            }
        });

        mapper.addConverter(new Converter<ItemComment, ItemCommentResponseDto>() {
            public ItemCommentResponseDto convert(MappingContext<ItemComment, ItemCommentResponseDto> context) {
                ItemComment src = context.getSource();
                return ItemCommentResponseDto.builder()
                        .id(src.getId())
                        .authorName(src.getAuthor().getName())
                        .created(src.getCreated())
                        .text(src.getText())
                        .build();
            }
        });
/*
        mapper.addMappings(new PropertyMap<ItemComment, ItemCommentResponseDto>() {
            @Override
            protected void configure() {
                skip(destination.getItemId());
                org.modelmapper.(destination.setAuthorName(source.getAuthor().getName());
            }
        });


        Converter<LocalDateTime, ZonedDateTime> zonedToLocalDateTime = new Converter<LocalDateTime, ZonedDateTime>() {
            public ZonedDateTime convert(MappingContext<LocalDateTime, ZonedDateTime> context) {
                TimeZone tz = TimeZone.getDefault();
                return context.getSource() == null ? null : context.getSource().atZone(tz.toZoneId());
            }
        };
        mapper.addConverter(zonedToLocalDateTime);

        Converter<ZonedDateTime, LocalDateTime> localToZonedDateTime = new Converter<ZonedDateTime, LocalDateTime>() {
            public LocalDateTime convert(MappingContext<ZonedDateTime, LocalDateTime> context) {
                TimeZone tz = TimeZone.getDefault();
                return context.getSource() == null ? null : context.getSource().toLocalDateTime();
            }
        };
        mapper.addConverter(localToZonedDateTime);

        Converter<ZonedDateTime, String> dateTimeZonedDateTime = new Converter<ZonedDateTime, String>() {
            public String convert(MappingContext<ZonedDateTime, String> context) {
                // DateTimeFormat.ISO.DATE_TIME
                DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                return context.getSource() == null ? null : dtFormatter.format(context.getSource());
            }
        };
        mapper.addConverter(dateTimeZonedDateTime); */

        return mapper;
    }

}
