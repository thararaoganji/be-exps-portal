package portal.expenses.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        PageResponse<String> response = new PageResponse<>();

        List<String> content = Arrays.asList("item1", "item2");
        response.setContent(content);
        response.setPageNumber(1);
        response.setPageSize(10);
        response.setTotalElements(50L);
        response.setTotalPages(5);
        response.setFirst(false);
        response.setLast(false);

        assertEquals(content, response.getContent());
        assertEquals(1, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(50L, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
    }

    @Test
    void testAllArgsConstructor() {
        List<Integer> content = Collections.singletonList(100);
        PageResponse<Integer> response = new PageResponse<>(
                content,
                0,
                20,
                1L,
                1,
                true,
                true
        );

        assertEquals(content, response.getContent());
        assertEquals(0, response.getPageNumber());
        assertEquals(20, response.getPageSize());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }
}