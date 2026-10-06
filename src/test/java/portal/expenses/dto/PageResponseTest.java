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
        PageResponse<String> response = new PageResponse<>();

        assertNull(response.getContent());
        assertEquals(0, response.getPageNumber());
        assertEquals(0, response.getPageSize());
        assertEquals(0L, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());

        List<String> content = Arrays.asList("item1", "item2");
        response.setContent(content);
        response.setPageNumber(1);
        response.setPageSize(10);
        response.setTotalElements(25L);
        response.setTotalPages(3);
        response.setFirst(true);
        response.setLast(false);

        assertEquals(content, response.getContent());
        assertEquals(1, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(25L, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
    }

    @Test
    void testAllArgsConstructor() {
        List<Integer> content = Collections.singletonList(42);
        PageResponse<Integer> response = new PageResponse<>(
                content,
                2,
                15,
                100L,
                7,
                false,
                true
        );

        assertEquals(content, response.getContent());
        assertEquals(2, response.getPageNumber());
        assertEquals(15, response.getPageSize());
        assertEquals(100L, response.getTotalElements());
        assertEquals(7, response.getTotalPages());
        assertFalse(response.isFirst());
        assertTrue(response.isLast());
    }
}