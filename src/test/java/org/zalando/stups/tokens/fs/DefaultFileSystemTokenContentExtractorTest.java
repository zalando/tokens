package org.zalando.stups.tokens.fs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;
import org.zalando.stups.tokens.AccessToken;

public class DefaultFileSystemTokenContentExtractorTest {

    private DefaultFileSystemTokenContentExtractor extractor = new DefaultFileSystemTokenContentExtractor();

    @Test
    public void testDefaultExtractor() {
        String token = UUID.randomUUID().toString();
        AccessToken at = extractor.extract(token, "Bearer");
        assertThat(at).isNotNull();
        assertThat(at.getToken()).isNotBlank();
        assertThat(at.getType()).isEqualTo("Bearer");
        assertThat(at.getToken()).isEqualTo(token);
    }

}
