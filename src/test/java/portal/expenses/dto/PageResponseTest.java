package portal.expenses.dto;

import org.junit.jupiter.api.Test;

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

        List<String> content = List.of("item1", "item2");
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

        response.setFirst(false);
        response.setLast(true);
        assertFalse(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    void testAllArgsConstructor() {
        List<Integer> content = List.of(1, 2, 3);
        PageResponse<Integer> response = new PageResponse<>(content, 0, 5, 3L, 1, true, true);

        assertEquals(content, response.getContent());
        assertEquals(0, response.getPageNumber());
        assertEquals(5, response.getPageSize());
        assertEquals(3L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }
}