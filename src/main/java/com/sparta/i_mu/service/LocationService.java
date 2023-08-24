package com.sparta.i_mu.service;

import com.sparta.i_mu.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    public List<String> getTopLocations(){
        return locationRepository.findAllByTopLocations()
                .stream()
                .limit(10)
                .collect(Collectors.toList());
    }
}
