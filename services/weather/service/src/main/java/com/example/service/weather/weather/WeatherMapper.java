package com.example.service.weather.weather;

import com.example.client.weather.dto.CurrentWeatherResult;
import com.example.client.weather.dto.LocationDto;
import com.example.client.weather.dto.WeatherDto;
import com.example.service.weather.weather.api.dto.ApiCurrentWeatherResult;
import com.example.service.weather.weather.api.dto.ApiLocationDto;
import com.example.service.weather.weather.api.dto.ApiWeatherDto;
import org.mapstruct.Mapper;

@Mapper
public interface WeatherMapper {
  WeatherDto toWeatherDto(ApiWeatherDto src);

  LocationDto toLocationDto(ApiLocationDto src);

  default CurrentWeatherResult toCurrentWeatherResult(ApiCurrentWeatherResult src) {
    return new CurrentWeatherResult(toLocationDto(src.location()), toWeatherDto(src.current()));
  }
}
