package portal.expenses.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageResponseTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        PageResponse<String> response = new PageResponse<>();

        List<String> items = List.of("item1", "item2");
        response.setContent(items);
        response.setPageNumber(1);
        response.setPageSize(10);
        response.setTotalElements(25L);
        response.setTotalPages(3);
        response.setFirst(false);
        response.setLast(false);

        assertEquals(items, response.getContent());
        assertEquals(1, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(25L, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
    }

    @Test
    void testAllArgsConstructor() {
        List<String> items = List.of("a", "b", "c");
        PageResponse<String> response = new PageResponse<>(items, 0, 5, 3L, 1, true, true);

        assertEquals(items, response.getContent());
        assertEquals(0, response.getPageNumber());
        assertEquals(5, response.getPageSize());
        assertEquals(3L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }
}