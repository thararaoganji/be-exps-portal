package portal.expenses.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageResponseTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        PageResponse<String> pageResponse = new PageResponse<>();

        assertNull(pageResponse.getContent());
        assertEquals(0, pageResponse.getPageNumber());
        assertEquals(0, pageResponse.getPageSize());
        assertEquals(0L, pageResponse.getTotalElements());
        assertEquals(0, pageResponse.getTotalPages());
        assertFalse(pageResponse.isFirst());
        assertFalse(pageResponse.isLast());

        List<String> items = Arrays.asList("item1", "item2");
        pageResponse.setContent(items);
        pageResponse.setPageNumber(1);
        pageResponse.setPageSize(10);
        pageResponse.setTotalElements(25L);
        pageResponse.setTotalPages(3);
        pageResponse.setFirst(true);
        pageResponse.setLast(false);

        assertEquals(items, pageResponse.getContent());
        assertEquals(1, pageResponse.getPageNumber());
        assertEquals(10, pageResponse.getPageSize());
        assertEquals(25L, pageResponse.getTotalElements());
        assertEquals(3, pageResponse.getTotalPages());
        assertTrue(pageResponse.isFirst());
        assertFalse(pageResponse.isLast());

        pageResponse.setFirst(false);
        pageResponse.setLast(true);
        assertFalse(pageResponse.isFirst());
        assertTrue(pageResponse.isLast());
    }

    @Test
    void testAllArgsConstructor() {
        List<Integer> content = Collections.singletonList(42);
        PageResponse<Integer> pageResponse = new PageResponse<>(
                content,
                0,
                20,
                1L,
                1,
                true,
                true
        );

        assertEquals(content, pageResponse.getContent());
        assertEquals(0, pageResponse.getPageNumber());
        assertEquals(20, pageResponse.getPageSize());
        assertEquals(1L, pageResponse.getTotalElements());
        assertEquals(1, pageResponse.getTotalPages());
        assertTrue(pageResponse.isFirst());
        assertTrue(pageResponse.isLast());
    }
}