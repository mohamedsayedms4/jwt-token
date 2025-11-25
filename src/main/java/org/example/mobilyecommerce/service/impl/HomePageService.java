package org.example.mobilyecommerce.service.impl;

import org.example.mobilyecommerce.model.HomePage;
import org.example.mobilyecommerce.repository.HomePageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomePageService {

    private final HomePageRepository homePageRepository;

    public HomePageService(HomePageRepository homePageRepository) {
        this.homePageRepository = homePageRepository;
    }

    public HomePage save(HomePage homePage) {
        return homePageRepository.save(homePage);
    }

    public List<HomePage> getAll() {
        return homePageRepository.findAll();
    }

    public HomePage getById(Long id) {
        return homePageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HomePage not found!"));
    }

    public HomePage update(Long id, HomePage updated) {
        HomePage old = getById(id);
        old.setImageUrl(updated.getImageUrl());
        old.setTitle(updated.getTitle());
        old.setHtmlUrl(updated.getHtmlUrl());
        return homePageRepository.save(old);
    }

    public void delete(Long id) {
        homePageRepository.deleteById(id);
    }
}
