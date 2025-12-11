package org.example.mobilyecommerce.service;

import org.example.mobilyecommerce.model.Icons;
import java.util.List;

public interface IconService {

    Icons createIcon(Icons icons);

    Icons updateIcon(Long id, Icons icons);

    void deleteIcon(Long id);

    Icons getIconById(Long id);

    List<Icons> getAllIcons();
}
