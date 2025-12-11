package org.example.mobilyecommerce.service;

import org.example.mobilyecommerce.model.Icons;
import org.example.mobilyecommerce.repository.IconsRepository;
import org.example.mobilyecommerce.service.IconService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IconServiceImpl implements IconService {

    @Autowired
    private IconsRepository iconRepository;

    @Override
    public Icons createIcon(Icons icons) {
        return iconRepository.save(icons);
    }

    @Override
    public Icons updateIcon(Long id, Icons icons) {
        Optional<Icons> existing = iconRepository.findById(id);
        if (existing.isPresent()) {
            Icons iconToUpdate = existing.get();
            iconToUpdate.setIcons(icons.getIcons());
            return iconRepository.save(iconToUpdate);
        }
        throw new RuntimeException("Icon not found with id " + id);
    }

    @Override
    public void deleteIcon(Long id) {
        iconRepository.deleteById(id);
    }

    @Override
    public Icons getIconById(Long id) {
        return iconRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Icon not found with id " + id));
    }

    @Override
    public List<Icons> getAllIcons() {
        return iconRepository.findAll();
    }
}
