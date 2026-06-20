package com.epam.lenda.gymapp.util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class PageUtil {
    // Ignores sorting for now
    public static <T> Page<T> streamToPage(@Nonnull Stream<T> stream, @Nonnull Class<T> tClass,
                                           @Nullable Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return new PageImpl<>(stream.toList());
        }

        final var stream2 = stream.skip(pageable.getOffset());
        final var iter = stream2.iterator();
        final var head = new ArrayList<T>();

        while (head.size() < pageable.getPageSize() && iter.hasNext()) {
            head.add(iter.next());
        }

        var remaining = 0;
        while (iter.hasNext()) {
            remaining++;
            iter.next();
        }

        return new PageImpl<>(head, pageable, pageable.getOffset() + head.size() + remaining);
    }
}
