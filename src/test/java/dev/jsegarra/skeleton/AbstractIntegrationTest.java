package dev.jsegarra.skeleton;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base for wired integration tests — boots the full application context against the H2 stand-in (PostgreSQL
 * compatibility mode). Concrete ITs extend this. Keep the container-free bulk of the pyramid (unit / slice /
 * {@code @DataJpaTest}) off this base; see springboot.md §10.
 */
@SpringBootTest
abstract class AbstractIntegrationTest {
}
