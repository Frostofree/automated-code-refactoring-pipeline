```java
package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dto.LocaleDto;
import com.sismics.reader.core.service.LocaleService;
import com.sismics.util.LocaleUtil;
import com.sismics.util.dto.LocaleDtoUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/locale")
public class LocaleResource {

    private final LocaleService localeService;

    public LocaleResource(LocaleService localeService) {
        this.localeService = localeService;
    }

    @GetMapping
    public List<LocaleDto> list() {
        return LocaleDtoUtil.convertAll(localeService.findAll());
    }
}
```