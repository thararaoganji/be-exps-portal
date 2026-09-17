package portal.expenses.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
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
        response.setPageNumber(2);
        response.setPageSize(10);
        response.setTotalElements(50L);
        response.setTotalPages(5);
        response.setFirst(false);
        response.setLast(true);

        assertEquals(content, response.getContent());
        assertEquals(2, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(50L, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertFalse(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    void testAllArgsConstructor() {
        List<String> content = Arrays.asList("a", "b", "c");
        PageResponse<String> response = new PageResponse<>(content, 0, 3, 10L, 4, true, false);

        assertEquals(content, response.getContent());
        assertEquals(0, response.getPageNumber());
        assertEquals(3, response.getPageSize());
        assertEquals(10L, response.getTotalElements());
        assertEquals(4, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
    }
}